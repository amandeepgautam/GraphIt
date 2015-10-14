package edu.unm.twin_cities.graphit.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import edu.umn.twin_cities.ErrorCode;
import edu.umn.twin_cities.FileAdapter;
import edu.umn.twin_cities.FileAdapter.ResourceType;
import edu.umn.twin_cities.ServerAction;
import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.processor.model.SensorReading;
import edu.unm.twin_cities.graphit.processor.model.SensorReadings;
import edu.unm.twin_cities.graphit.service.DataService;
import edu.unm.twin_cities.graphit.util.FileParser;
import edu.unm.twin_cities.graphit.util.FileParserImpl;
import lombok.AllArgsConstructor;
import lombok.Data;

public class FileBrowserActivity extends AppCompatActivity {

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DataService.RECEIVE_DATA_INSERT_COMPLETE )) {
                boolean insertSuccessful = (boolean) intent.getExtras().get(DataService.INSERT_COMPLETE);
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

    public static final String PARAM_FILE_PATH = "path";

    private static final UUID SERVICE_ID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final int CACHE_SIZE = 10;
    private final String TAG = this.getClass().getSimpleName();

    private BluetoothSocket bluetoothSocket = null;

    private ObjectInputStream objectInputStream = null;

    private ObjectOutputStream objectOutputStream = null;

    private FileArrayAdapter fileArrayAdapter;

    LoadingCache<String, List<FileAdapter>> browserCache = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(
                    new CacheLoader<String, List<FileAdapter>>() {
                        @Override
                        public List<FileAdapter> load(String key) throws Exception {
                            return listFiles(key);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        BluetoothDevice bluetoothDevice = (BluetoothDevice) getIntent().getExtras().get(BluetoothScanner.PARAM_BLUETOOTH_DEVICE);
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SERVICE_ID);
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
            try {
                bluetoothSocket.connect();
            } catch(IOException e) {
                Log.e(TAG, "First connection attempt failed.", e);
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 2); //-> not working
                //bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothSocket.connect();
            }
            //Open the input and output stream
            objectOutputStream = new ObjectOutputStream(bluetoothSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(bluetoothSocket.getInputStream());

            ListView listView = (ListView) findViewById(R.id.files);
            fileArrayAdapter = new FileArrayAdapter(this, R.layout.resource_info, new ArrayList<FileAdapter>());
            fileArrayAdapter.setNotifyOnChange(true);
            listView.setAdapter(fileArrayAdapter);
            Context context = this;

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View item,
                                        int position, long id) {
                    try {
                        FileAdapter file = (FileAdapter) parent.getItemAtPosition(position);
                        if (file.getResourceType() == ResourceType.DIRECTORY) {
                            List<FileAdapter> files = browserCache.get(file.getPath());
                            fileArrayAdapter.clear();
                            fileArrayAdapter.addAll(files);
                        } else if (file.getResourceType() == ResourceType.FILE) {
                            byte [] fileContents = transferFile(file.getPath());
                            FileParser parser = new FileParserImpl();
                            List<SensorReading<Long, Float>> collection = parser.parse(fileContents);
                            Intent intent = new Intent(FileBrowserActivity.this, DataService.class);
                            intent.putExtra(PARAM_SENSOR_DATA, (Serializable)collection);

                            //register a local broadcast.
                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(FileBrowserActivity.this);
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(DataService.RECEIVE_DATA_INSERT_COMPLETE);
                            localBroadcastManager.registerReceiver(bReceiver, intentFilter);

                            startService(intent);
                        } else if (file.getResourceType() == ResourceType.UNKNOWN){
                            //TODO: DO error handling. Send some notifications etc.
                            Log.e(TAG, "CANNOT READ FILE");
                        }
                    } catch (Exception e) { //java.io.IOException
                        //TODO: DO error handling.
                        Log.e(TAG, "We go an exception", e);
                    }
                }
            });

            //Send a empty path. Server should return the default accessible location.
            String path = "/home/pi/Adafruit-Raspberry-Pi-Python-Code/Adafruit_ADS1x15/testing";
            List<FileAdapter> files = listFiles(path);
            fileArrayAdapter.addAll(files);
        } catch (Exception e) {
            Log.e(TAG, "We go an exception", e);
        }
    }

    private List<FileAdapter> listFiles(String path) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(ServerAction.LIST_FILES_IN_DIR);
        objectOutputStream.writeObject(path);
        objectOutputStream.flush();
        Object object = objectInputStream.readObject();
        FileAdapter[] files = null;
        if (object instanceof ErrorCode) {
            ErrorCode errorCode = (ErrorCode) object;
            throw new IllegalArgumentException(errorCode.getErrorMsg());
        } else if (object instanceof FileAdapter[]) {
            files = (FileAdapter[]) object;
        } else {
            throw new IllegalStateException("Unrecognized object sent by the server.");
        }
        return Arrays.asList(files);
    }

    private byte[] transferFile(String path) throws IOException, ClassNotFoundException {
        objectOutputStream.writeObject(ServerAction.TRANSFER_FILE);
        objectOutputStream.writeObject(path);
        objectOutputStream.flush();
        Object object = objectInputStream.readObject();
        String receivedMd5Sum = null;
        if (object instanceof ErrorCode) {
            ErrorCode errorCode = (ErrorCode) object;
            throw new IllegalArgumentException(errorCode.getErrorMsg());
        } else if (object instanceof String) {
            receivedMd5Sum = (String) object;
        } else {
            throw new IllegalStateException("Unrecognized object sent by server");
        }

        byte [] fileContents;
        object = objectInputStream.readObject();
        if (object instanceof ErrorCode) {
            //TODO: test this bit.
            ErrorCode errorCode = (ErrorCode) object;
            throw new IllegalArgumentException(errorCode.getErrorMsg());
        } else if (object.getClass().isArray()
                && byte.class.isAssignableFrom(object.getClass().getComponentType())) {
            fileContents = (byte []) object;
        } else {
            throw new IllegalStateException("Unrecognized object sent by server");
        }
        //inefficient as input is parsed twice but fail fast.
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            String calculatedMd5Sum = new String(Hex.encodeHex(messageDigest.digest(fileContents)));
            if (!calculatedMd5Sum.equals(receivedMd5Sum)) {
                throw new IllegalStateException("Checksum verification failed. Please download again");
            }
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("Could not get Algorithm to calcuate checksum");
        }
        return fileContents;
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
    public void onDestroy() {
        try {
            if(bluetoothSocket != null) {
                //Release the resources.
                if (objectInputStream != null)
                    objectInputStream.close();
                if (objectOutputStream != null)
                    objectOutputStream.close();
                bluetoothSocket.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unable to close resources", e);
        }
        super.onDestroy();
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
    private class FileArrayAdapter extends ArrayAdapter<FileAdapter> {

        private List<FileAdapter> rows;

        FileArrayAdapter(Context context, int resourceId,
                         List<FileAdapter> rows) {
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
            FileAdapter file = rows.get(position);
            resourceName.setText(file.getName());
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy MM dd HH:mm:ss");
            lastModified.setText(simpleDateFormat.format(new Date(file.getLastModified())));
            return convertView;
        }
    }
}
