package edu.unm.twin_cities.graphit.activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.umn.twin_cities.FileAdapter;
import edu.umn.twin_cities.FileAdapter.ResourceType;
import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.fragments.FileBrowserFragment;
import edu.unm.twin_cities.graphit.util.Measurement;
import edu.unm.twin_cities.graphit.service.DataService;
import edu.unm.twin_cities.graphit.util.ConnectionResourceBundle;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import edu.unm.twin_cities.graphit.util.ImageLoader;
import edu.unm.twin_cities.graphit.util.ServerActionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class FileBrowserActivity extends AppCompatActivity {

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DataService.READINGS_DATA_INSERT_COMPLETE)) {
                boolean insertSuccessful = (boolean) intent.getExtras().get(DataService.PARAM_INSERT_COMPLETE);
                if (insertSuccessful) {
                    Intent activityIntent = new Intent(getApplicationContext(), PlotActivity.class);
                    startActivity(activityIntent);
                } else {
                    //TODO: Graceful failure.
                }
            }
        }
    };

    public static final String PARAM_SENSOR_DATA = "sensor_data";
    public static final String PARAM_DEVICE_ID = "device_id";
    public static final String ACTION_INSERT_READINGS_DATA = "insert_readings_data";

    private final int CACHE_SIZE = 10;
    private final String TAG = this.getClass().getSimpleName();

    private ServerActionUtil serverActionUtil = null;
    private FileArrayAdapter fileArrayAdapter;

    LoadingCache<String, List<FileBrowserFragment.FileInfo>> browserCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(
                    new CacheLoader<String, List<FileBrowserFragment.FileInfo>>() {
                        @Override
                        public List<FileBrowserFragment.FileInfo> load(String key) throws Exception {
                            return serverActionUtil.listFiles(key);
                        }
                    }
            );

    Deque<String> parentStack = new ArrayDeque<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        Toolbar toolbar = (Toolbar) findViewById(R.id.image_view_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayShowTitleEnabled(false);    //remove the application name from toolbar.

        final TextView toolBarTitle = (TextView) toolbar.findViewById(R.id.image_view_toolbar_title);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentStack.size() > 1) {
                    try {
                        parentStack.pop();
                        List<FileBrowserFragment.FileInfo> files = browserCache.get(parentStack.peek());
                        fileArrayAdapter.clear();
                        fileArrayAdapter.addAll(files);
                    } catch (ExecutionException ee) {
                        Log.e(TAG, "Exception while accessing cache.", ee);
                    }
                }
            }
        });

        final BluetoothDevice bluetoothDevice = (BluetoothDevice) getIntent().getExtras().get(BluetoothScanner.PARAM_BLUETOOTH_DEVICE);
        try {
            GraphItApplication application = ((GraphItApplication)getApplicationContext());
            RemoteConnectionResourceManager connectionManager = application.getConnectionManager();

            ConnectionResourceBundle connectionResourceBundle = connectionManager.getConnectionResource(bluetoothDevice, getApplicationContext());

            serverActionUtil = new ServerActionUtil(connectionResourceBundle);

            ListView listView = (ListView) findViewById(R.id.files);
            fileArrayAdapter = new FileArrayAdapter(this, R.layout.resource_info, new ArrayList<FileBrowserFragment.FileInfo>());
            fileArrayAdapter.setNotifyOnChange(true);
            listView.setAdapter(fileArrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View item,
                                        int position, long id) {
                    try {
                        FileAdapter file = (FileAdapter) parent.getItemAtPosition(position);
                        if (file.getResourceType() == ResourceType.DIRECTORY) {
                            String path = file.getPath();
                            List<FileBrowserFragment.FileInfo> files = browserCache.get(path);
                            parentStack.push(path);
                            fileArrayAdapter.clear();
                            fileArrayAdapter.addAll(files);
                            toolBarTitle.setText(path);
                        } else if (file.getResourceType() == ResourceType.FILE) {
                            List<Measurement<Long, Float>> collection = serverActionUtil.transferFile(file.getPath());
                            if (collection != null) {
                                Intent intent = new Intent(FileBrowserActivity.this, DataService.class);
                                intent.setAction(ACTION_INSERT_READINGS_DATA);
                                intent.putExtra(PARAM_SENSOR_DATA, (Serializable)collection);
                                intent.putExtra(PARAM_DEVICE_ID, bluetoothDevice.getAddress());     //wierd that it can throw NPE here.

                                //register a local broadcast.
                                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(FileBrowserActivity.this);
                                IntentFilter intentFilter = new IntentFilter();
                                intentFilter.addAction(DataService.READINGS_DATA_INSERT_COMPLETE);
                                localBroadcastManager.registerReceiver(bReceiver, intentFilter);

                                startService(intent);
                            } else {
                                //TODO: See what has to be done in case there is 0 length of input.
                                //TODO: Probably a notification that there is no new data.
                                Intent activityIntent = new Intent(getApplicationContext(), PlotActivity.class);
                                startActivity(activityIntent);
                            }
                        } else if (file.getResourceType() == ResourceType.UNKNOWN){
                            //TODO: DO error handling. Send some notifications etc.
                            Log.e(TAG, "CANNOT READ FILE");
                        }
                    } catch (ExecutionException ee) {
                        Log.e(TAG, "Exception while accessing cache.", ee);
                    } catch (Exception e) { //java.io.IOException
                        //TODO: DO error handling.
                        Log.e(TAG, "We go an exception", e);
                    }
                }
            });

            //Send a empty path. Server should return the default accessible location.
            String path = "/";
            List<FileBrowserFragment.FileInfo> files = browserCache.get(path);
            parentStack.push(path);
            toolBarTitle.setText(path);

            fileArrayAdapter.addAll(files);
        } catch (Exception e) {
            Log.e(TAG, "We go an exception", e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_browser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item= menu.findItem(R.id.action_settings);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class ViewHolder {
        /**
         * Icon for differentiating a file or a folder.
         */
        ImageView icon;
        /**
         * Name of the resource.
         */
        TextView name;
        /**
         * Last modified date, from the OS.
         */
        TextView lastModified;
    }

    /**
     * A custom adapter to browse files from other device.
     */
    private class FileArrayAdapter extends ArrayAdapter<FileBrowserFragment.FileInfo> {

        private List<FileBrowserFragment.FileInfo> rows;

        FileArrayAdapter(Context context, int resourceId,
                         List<FileBrowserFragment.FileInfo> rows) {
            super(context, resourceId, rows);
            this.rows = rows;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView resourceDescriptorImage;
            TextView resourceName;
            TextView lastModified;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.resource_info, parent, false);
                resourceDescriptorImage = (ImageView) convertView.findViewById(R.id.image_descriptor_resource);
                resourceName = (TextView) convertView.findViewById(R.id.name_resource);
                lastModified = (TextView) convertView.findViewById(R.id.resource_last_modified);

                ViewHolder viewHolder = new ViewHolder(resourceDescriptorImage,
                        resourceName, lastModified);
                convertView.setTag(viewHolder);   //optimization: use in future.
            } else {
                ViewHolder viewHolder = (ViewHolder) convertView.getTag();
                resourceDescriptorImage = viewHolder.getIcon();
                resourceName = viewHolder.getName();
                lastModified = viewHolder.getLastModified();
            }

            //change the data to reflect the new row.
            FileBrowserFragment.FileInfo file = rows.get(position);
            resourceName.setText(file.getName());
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy MM dd HH:mm:ss");
            lastModified.setText(simpleDateFormat.format(new Date(file.getLastModified())));
            resourceDescriptorImage.setImageResource(ImageLoader.getFileTypeIcon(file.getResourceType()));
            return convertView;
        }
    }
}
