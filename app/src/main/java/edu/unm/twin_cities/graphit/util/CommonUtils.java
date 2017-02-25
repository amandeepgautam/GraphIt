package edu.unm.twin_cities.graphit.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.common.collect.Maps;

import java.util.Map;

import edu.unm.twin_cities.graphit.service.ServerActionsService;

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

    static public IntentFilter getBluetoothIntentFilter() {
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);

        return filter;
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

        boolean startDiscovery = bluetoothAdapter.startDiscovery();
    }

/*    public static <K extends Parcelable, V extends Parcelable> void writeMapToParcel(Map<K, V> map, Parcel out, int flags) {
        out.writeInt(map.size());
        for(Map.Entry<K,V> entry : map.entrySet()){
            out.writeParcelable(entry.getKey(), 0);
            out.writeParcelable(entry.getValue(), 0);
        }
    }

    public static <K extends Parcelable, V extends Parcelable> Map<K, V> readMapFromParcel(Parcel in, int flags) {
        Map<K, V> map = Maps.newHashMap();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            K key = in.<K>readParcelable(key.getClass().getClassLoader());
            V value = in.readParcelable();
            map.put(key,value);

            return null;
    }*/
}