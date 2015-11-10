package edu.unm.twin_cities.graphit.activity;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Set;

import edu.unm.twin_cities.graphit.R;
import edu.unm.twin_cities.graphit.util.CommonUtils;

public class PairedDeviceSelection extends CheckBoxInListViewActivity {

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
                BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);
                //TODO: Add code for on click.
            }
        });
        new ArrayList<BluetoothDevice>(pairedDevices);
        setCheckBoxInListViewAdapter(new CheckBoxInListViewAdapter(this, R.layout.paired_device_info, new ArrayList<BluetoothDevice>(pairedDevices)));
        listView.setAdapter(getCheckBoxInListViewAdapter());
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
}