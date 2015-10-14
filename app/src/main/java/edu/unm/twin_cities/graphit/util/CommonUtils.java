package edu.unm.twin_cities.graphit.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

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
}
