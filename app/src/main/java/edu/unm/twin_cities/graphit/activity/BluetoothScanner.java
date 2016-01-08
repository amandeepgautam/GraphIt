package edu.unm.twin_cities.graphit.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.umn.twin_cities.ServerAction;
import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.util.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

public class BluetoothScanner extends AppCompatActivity {

    public static final String PARAM_BLUETOOTH_DEVICE = "bluetooth_device";

    private final String TAG = this.getClass().getSimpleName();

    private ProgressBar progressBar;

    private CheckBoxInListViewAdapter checkBoxInListViewAdapter;

    // Create a BroadcastReceiver for ACTION_FOUND
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice foundBtDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    checkBoxInListViewAdapter.add(foundBtDevice);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        Log.i(TAG, "Paired state: " + state);
                    } else if(state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                        Log.i(TAG, "Unpaired state: " + state);
                    }
                    //TODO: Start new activity.
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scanner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.progress_spinner);

        ListView listView = (ListView) findViewById(R.id.new_discovered_device_list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item,
                                    int position, long id) {
                try {
                    BluetoothDevice bluetoothDevice = (BluetoothDevice) parent.getItemAtPosition(position);
                    if (BluetoothDevice.BOND_BONDED != bluetoothDevice.getBondState()) {
                        Class clazz = Class.forName("android.bluetooth.BluetoothDevice");
                        Method createBondMethod = clazz.getMethod("createBond");
                        createBondMethod.invoke(bluetoothDevice);
                    }
                    Intent intent = new Intent(getApplicationContext(), FileBrowserActivity.class);
                    intent.putExtra(PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
                    startActivity(intent);

                } catch (Exception e) { //java.io.IOException
                    Log.e(TAG, "We go an exception", e);
                }
            }
        });
        checkBoxInListViewAdapter = new CheckBoxInListViewAdapter(this, R.layout.available_device_info, new ArrayList<BluetoothDevice>());
        checkBoxInListViewAdapter.setNotifyOnChange(true);
        listView.setAdapter(checkBoxInListViewAdapter);

        final int REQUEST_ENABLE_BT = 1;
        BluetoothAdapter bluetoothAdapter = CommonUtils.getBluetoothAdapter(this, REQUEST_ENABLE_BT);

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //TODO: Change this to appropriate place, which is onclick listener.
        registerReceiver(broadcastReceiver, filter); // TODO: Don't forget to unregister during onDestroy
        bluetoothAdapter.startDiscovery();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_scanner, menu);
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
    protected void onDestroy() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException iae) {
            /* DO NOTHING. As per:
            http://stackoverflow.com/questions/6165070/receiver-not-registered-exception-error */
        }
        super.onDestroy();
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
        TextView deviceName;
        TextView deviceAddress;
        TextView pairedInfo;
    }

    /**
     * Class contains the logic to convert data items to view items.
     */
    @Data
    protected class CheckBoxInListViewAdapter extends ArrayAdapter<BluetoothDevice> {
        /**
         * List of items.
         */
        private List<BluetoothDevice> rows;

        public CheckBoxInListViewAdapter(Context context, int resourceId,
                                         List<BluetoothDevice> rows) {
            super(context, resourceId, rows);
            this.rows = rows;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // The child views in each row.
            ViewHolder viewHolder;
            TextView deviceNameTextView;
            TextView deviceAddressTextView;
            TextView pairedInfoTextView;

            // Creates a new row view.
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.available_device_info, parent, false);

                //Find the child views.
                deviceNameTextView = (TextView) convertView.findViewById(R.id.name_available_device);
                deviceAddressTextView = (TextView) convertView.findViewById(R.id.address_available_device);
                pairedInfoTextView = (TextView) convertView.findViewById(R.id.paired_info);

                viewHolder = new ViewHolder(deviceNameTextView, deviceAddressTextView, pairedInfoTextView);
                // Tag the row with it's child view for future use.
                convertView.setTag(viewHolder);

            } else { // reuse the existing row. Hence optimization by tagging the object.
                viewHolder = (ViewHolder) convertView.getTag();
                deviceNameTextView = viewHolder.getDeviceName();
                deviceAddressTextView = viewHolder.getDeviceAddress();
                pairedInfoTextView = viewHolder.getPairedInfo();
            }

            // Retrieve the appropriate  item to display from data source.
            BluetoothDevice bluetoothDevice = rows.get(position);

            // Get data for display.
            deviceNameTextView.setText(bluetoothDevice.getName());
            deviceAddressTextView.setText(bluetoothDevice.getAddress());
            pairedInfoTextView.setText(getBondState(bluetoothDevice));
            return convertView;
        }
    }

    public String getBondState(BluetoothDevice bluetoothDevice) {
        int bondedState = bluetoothDevice.getBondState();
        if(bondedState == BluetoothDevice.BOND_BONDED) {
            return getResources().getString(R.string.paired);
        } else if (bondedState == BluetoothDevice.BOND_BONDING) {
            return getResources().getString(R.string.pairing);
        } else if (bondedState == BluetoothDevice.BOND_NONE) {
            return getResources().getString(R.string.unpaired);
        }
        throw new IllegalStateException("Unknown/Illegal connection state of the remote device.");
    }
}
