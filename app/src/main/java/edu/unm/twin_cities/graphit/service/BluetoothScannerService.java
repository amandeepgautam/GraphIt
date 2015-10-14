package edu.unm.twin_cities.graphit.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BluetoothScannerService extends Service {
    public BluetoothScannerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
