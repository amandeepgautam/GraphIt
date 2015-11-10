package edu.unm.twin_cities.graphit.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Bluetooth connection needs to be reused across multiple activities. If the bluetooth connection
 * object is local, its gets destroyed when the activity destroyed and causes a glitch in
 * application, even if you are willing to accept another connection at the server. Hence bluetooth
 * connection is stored throughout the lifetime of the application. Along with bluetooth connection,
 * we need to have only one ObjectInputStream and ObjectOutputStream thorughout the application.
 * Creation of new ObjectInputStream or ObjectOutputStream creates problems with writing multiple
 * headers. Hence these objects are also maintained for the application lifetime. More details on
 * this error can be found at
 * http://inaved-momin.blogspot.com/2012/08/understanding-javaiostreamcorruptedexce.html
 * and here is another solution to the problem:
 * http://stackoverflow.com/questions/1194656/appending-to-an-objectoutputstream/1195078#1195078
 */
@Data
public class RemoteConnectionResouceManager {

    private final String TAG = this.getClass().getSimpleName();
    private final UUID SERVICE_ID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    final Map<String, ConnectionResouceBundle> resourceBundleMap = Maps.newHashMap();

    public ConnectionResouceBundle getConnectionResource(BluetoothDevice bluetoothDevice, Context context) throws NoSuchMethodException, IOException, InvocationTargetException, IllegalAccessException {
        try {
            synchronized (resourceBundleMap) {
                ConnectionResouceBundle resourceBundle = resourceBundleMap.get(bluetoothDevice.getAddress());
                if(resourceBundle != null) {
                    BluetoothSocket bluetoothSocket = resourceBundle.getBluetoothSocket();
                    if (bluetoothSocket != null && bluetoothSocket.isConnected())
                        return resourceBundle;
                }

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(enableBtIntent);
                }

                BluetoothSocket bluetoothSocket;
                try {
                    Method m = bluetoothDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                    bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 2);
                    bluetoothSocket.connect();
                } catch (IOException e) {
                    Log.e(TAG, "Connection attempt using service discovery failed. Falling back to Brute force connection strategy.", e);
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SERVICE_ID);
                    bluetoothSocket.connect();
                }

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(bluetoothSocket.getOutputStream());
                ObjectInputStream objectInputStream = new ObjectInputStream(bluetoothSocket.getInputStream());
                resourceBundle = new ConnectionResouceBundle(objectOutputStream, objectInputStream, bluetoothSocket);

                //add the connection for future use.
                resourceBundleMap.put(bluetoothDevice.getAddress(), resourceBundle);
                return resourceBundle;
            }
        } catch (IOException | NoSuchMethodException| InvocationTargetException | IllegalAccessException e) {
            Log.e(TAG, "Could not get bluetooth to connect.", e);
            throw e;
            //TODO: close the application or something
        }
    }
}
