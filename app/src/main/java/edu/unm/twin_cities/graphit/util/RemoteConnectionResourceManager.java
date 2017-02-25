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
public class RemoteConnectionResourceManager {

    private final String TAG = this.getClass().getSimpleName();
    private final UUID SERVICE_ID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    final Map<String, ConnectionResourceBundle> resourceBundleMap = Maps.newConcurrentMap();

    public ConnectionResourceBundle getConnectionResource(BluetoothDevice bluetoothDevice, Context context) throws IOException {
        synchronized (resourceBundleMap) {
            ConnectionResourceBundle resourceBundle = resourceBundleMap.get(bluetoothDevice.getAddress());
            if(resourceBundle != null) {
                BluetoothSocket bluetoothSocket = resourceBundle.getBluetoothSocket();
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    return resourceBundle;
                }
            }
            BluetoothSocket bluetoothSocket = getSocket(bluetoothDevice, context);
            return getResourceBundle(bluetoothDevice, bluetoothSocket);
        }
    }

    /**
     * Update the connection resources when they are useless. For example an IOException on server
     * will render the stream unusable. Create new stream objects for use in
     * application.
     * @param bluetoothDevice
     * @param context
     * @return
     * @throws NoSuchMethodException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public ConnectionResourceBundle refreshConnectionResource(BluetoothDevice bluetoothDevice, Context context) throws IOException {
        try {
            synchronized (resourceBundleMap) {
                resourceBundleMap.remove(bluetoothDevice.getAddress());
                BluetoothSocket bluetoothSocket = getSocket(bluetoothDevice, context);
                return getResourceBundle(bluetoothDevice, bluetoothSocket);
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not get bluetooth to connectAndCreateStreams.", e);
            throw e;
            //TODO: close the application or something
        }
    }

    private BluetoothSocket getSocket(BluetoothDevice bluetoothDevice, Context context) throws IOException {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            }

            BluetoothSocket bluetoothSocket = null;
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(SERVICE_ID);
                bluetoothSocket.connect();
            } catch (IOException e) {
                Log.d(TAG, "Connection attempt using service discovery failed. Falling back to Brute force connection strategy.", e);
                Method m = bluetoothDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 2);
                bluetoothSocket.connect();
            }
            return bluetoothSocket;
        } catch (IOException ioe) {
            Log.e(TAG, "Brute force Strategy failed. Could not connect to the device.", ioe);
            throw ioe;
            //TODO: notification for failed conneection.
        } catch (NoSuchMethodException nsme) {
            Log.e(TAG, "The interface used for connecting seems inappropriate.", nsme);
        } catch (InvocationTargetException ite) {
            Log.e(TAG, "Invoked target does not exist.", ite);
        } catch (IllegalAccessException iae) {
            Log.e(TAG, "Cannot invoke createInsecureRfcommSocket function.", iae);
        }
        return null;
    }

    private ConnectionResourceBundle getResourceBundle(BluetoothDevice bluetoothDevice, BluetoothSocket bluetoothSocket) throws IOException {
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(bluetoothSocket.getOutputStream());
        } catch (IOException ioe) {
            Log.e(TAG, "Error creating output stream from the socket.", ioe);
            throw ioe;
        }

        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(bluetoothSocket.getInputStream());
        } catch (IOException ioe) {
            Log.e(TAG, "Error creating input stream from the socket.", ioe);
            throw ioe;
        }
        ConnectionResourceBundle resourceBundle =
                new ConnectionResourceBundle(bluetoothDevice.getAddress(),
                        objectOutputStream, objectInputStream, bluetoothSocket);

        //add the connection for future use.
        resourceBundleMap.put(resourceBundle.getResourceIdentifier(), resourceBundle);
        return resourceBundle;
    }
}
