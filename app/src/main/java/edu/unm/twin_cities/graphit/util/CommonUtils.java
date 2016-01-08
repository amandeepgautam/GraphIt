package edu.unm.twin_cities.graphit.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by aman on 23/8/15.
 */
public class CommonUtils {

    static public BluetoothAdapter getBluetoothAdapter(final Activity activity,final int REQUEST_ENABLE_BT) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //TODO: Error handling. Throw error, do not return as part of application using the code
            //TODO: does not checks for null.
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        return bluetoothAdapter;
    }

    /**
     * A convenience function with set of pre-registered listeners.
     * Do not forget to unregister in onDestroy method.
     * @param activity
     * @param REQUEST_ENABLE_BT
     * @param bReceiver
     */
    static public void startBluetoothDiscovery(final Activity activity,final int REQUEST_ENABLE_BT, BroadcastReceiver bReceiver) {
        BluetoothAdapter bluetoothAdapter = CommonUtils.getBluetoothAdapter(activity, REQUEST_ENABLE_BT);

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        activity.registerReceiver(bReceiver, filter); // TODO: Don't forget to unregister during onDestroy
        bluetoothAdapter.startDiscovery();
    }
}