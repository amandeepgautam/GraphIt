package edu.unm.twin_cities.graphit.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.util.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

public class PairedDeviceSelection extends CheckBoxInListViewActivity {

    private CheckBoxInListViewAdapter checkBoxInListViewAdapter;

    private final String TAG = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_device_selection);

        final int REQUEST_ENABLE_BT = 2;
        BluetoothAdapter bluetoothAdapter = CommonUtils.getBluetoothAdapter(this, REQUEST_ENABLE_BT);

        //TODO: Confirm that getBlueToothAdapter does not returns null in any case. Use exceptions.
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        ListView listView = (ListView) findViewById(R.id.paired_device_list);

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
                    intent.putExtra(BluetoothScanner.PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
                    startActivity(intent);

                } catch (Exception e) { //java.io.IOException
                    Log.e(TAG, "We go an exception", e);
                }
            }
        });

        new ArrayList<BluetoothDevice>(pairedDevices);
        checkBoxInListViewAdapter = new CheckBoxInListViewAdapter(this, R.layout.paired_device_info, new ArrayList<BluetoothDevice>(pairedDevices));
        checkBoxInListViewAdapter.setNotifyOnChange(true);
        listView.setAdapter(checkBoxInListViewAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_paired_device_selection, menu);
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

    /**
     * Start the bluetooth scanner activity.
     * @param view
     */
    public void startBluethoothScannerActivity(View view) {
        Intent intent = new Intent(this, BluetoothScanner.class);
        startActivity(intent);
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
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

            // Creates a new row view.
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.paired_device_info, parent, false);

                //Find the child views.
                deviceNameTextView = (TextView) convertView.findViewById(R.id.name_device);
                deviceAddressTextView = (TextView) convertView.findViewById(R.id.address_device);

                viewHolder = new ViewHolder(deviceNameTextView, deviceAddressTextView);
                // Tag the row with it's child view for future use.
                convertView.setTag(viewHolder);

            } else { // reuse the existing row. Hence optimization by tagging the object.
                viewHolder = (ViewHolder) convertView.getTag();
                deviceNameTextView = viewHolder.getDeviceName();
                deviceAddressTextView = viewHolder.getDeviceAddress();
            }

            // Retrieve the appropriate  item to display from data source.
            BluetoothDevice bluetoothDevice = rows.get(position);

            // Get data for display.
            deviceNameTextView.setText(bluetoothDevice.getName());
            deviceAddressTextView.setText(bluetoothDevice.getAddress());
            return convertView;
        }
    }
}