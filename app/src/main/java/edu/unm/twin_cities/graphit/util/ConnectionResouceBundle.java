package edu.unm.twin_cities.graphit.util;

import android.bluetooth.BluetoothSocket;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A resource bundle, which has fields for objects that need to maintained for
 * application's lifetime.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class ConnectionResouceBundle {
    /**
     * Outputstream object. See {@link RemoteConnectionResouceManager#RemoteConnectionResouceManager}
     * for more information.
     */
    ObjectOutputStream objectOutputStream;

    /**
     * Inputstream object. See {@link RemoteConnectionResouceManager#RemoteConnectionResouceManager}
     * for more information.
     */
    ObjectInputStream objectInputStream;

    /**
     * The bluetooth connectionn socket object.
     */
    BluetoothSocket bluetoothSocket;
}
