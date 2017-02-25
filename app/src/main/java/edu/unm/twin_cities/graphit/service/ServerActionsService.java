package edu.unm.twin_cities.graphit.service;

import android.app.IntentService;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.application.GraphItApplication;
import edu.unm.twin_cities.graphit.fragments.UpdateFragment;
import edu.unm.twin_cities.graphit.processor.dao.ReadingDao;
import edu.unm.twin_cities.graphit.processor.model.Reading;
import edu.unm.twin_cities.graphit.util.ConnectionResourceBundle;
import edu.unm.twin_cities.graphit.util.Measurement;
import edu.unm.twin_cities.graphit.util.RemoteConnectionResourceManager;
import edu.unm.twin_cities.graphit.util.ServerActionUtil;

/**
 * The service wraps around the methods from {@link edu.unm.twin_cities.graphit.util.ServerActionUtil}.
 * This is a necessity as sometimes the server actions are tightly coupled to the main UI threads
 * and need to be responsive, in the other cases it is fine if they are run from background and
 * complete at a later point of time.
 */
public class ServerActionsService extends IntentService {

    private static String TAG = DataService.class.getSimpleName();

    // TODO: Rename parameters
    public static final String ACTION_PING = "ping";
    public static final String ACTION_TRANSFER_FILE_AND_INSERT_DATA = "transfer_and_insert_file";

    public static final String PARAM_BLUETOOTH_DEVICE = "edu.unm.twin_cities.graphit.service.extra.PARAM_BLUETOOTH_DEVICE";
    public static final String PARAM_IS_PING_SUCCESSFUL = "is_ping_successful";
    public static final String ACTION_TRANSFER_FILE_AND_INSERT_DATA_STARTED = "transfer_and_insert_file_started";
    public static final String ACTION_TRANSFER_FILE_AND_INSERT_DATA_FINISHED = "transfer_and_insert_file_finished";
    public static final String ACTION_TRANSFER_FILE_AND_INSERT_DATA_INITIATED = "transfer_and_insert_file_initiated";
    public static final String ACTION_TRANSFER_FILE_AND_INSERT_DATA_ENDED = "transfer_and_insert_file_ended";

    public ServerActionsService() {
        super("ServerActionsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getExtras().get(PARAM_BLUETOOTH_DEVICE);

            GraphItApplication application = ((GraphItApplication) getApplicationContext());
            RemoteConnectionResourceManager connectionManager = application.getConnectionManager();

            switch (action) {
                case ACTION_PING:
                    handlePingAction(connectionManager, bluetoothDevice);
                    break;
                case ACTION_TRANSFER_FILE_AND_INSERT_DATA:
                    Map<String, List<String>> fileNameSensorInfo = (Map<String, List<String>>) intent.getExtras()
                            .getSerializable(UpdateFragment.PARAM_FILENAME_SENSOR_INFO);
                    handleActionTransferFileInsertData(connectionManager, bluetoothDevice, fileNameSensorInfo);
                    break;
                default:
                    throw new UnsupportedOperationException("This server action has not been implemented.");
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handlePingAction(RemoteConnectionResourceManager connectionManager, BluetoothDevice bluetoothDevice) {
        boolean ping = false;   //ping unsuccessful.
        try {
            ConnectionResourceBundle connectionResourceBundle = connectionManager.getConnectionResource(bluetoothDevice, this);

            ServerActionUtil serverActionUtil = new ServerActionUtil(connectionResourceBundle);
            ping = serverActionUtil.ping();
        } catch (IOException ioe) {
            //exception also means connection unsuccessful
        }
        Intent notificationIntent = new Intent(ACTION_PING);
        notificationIntent.putExtra(PARAM_IS_PING_SUCCESSFUL, ping);
        notificationIntent.putExtra(PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
        sendBroadcast(notificationIntent);
    }

    private void handleActionTransferFileInsertData(
            RemoteConnectionResourceManager connectionManager,
            BluetoothDevice bluetoothDevice,
            Map<String, List<String>> fileNameSensorInfo) {
        boolean isSuccessful = true;
        Intent notificationIntent = null;
        ConnectionResourceBundle connectionResourceBundle = null;
        ServerActionUtil serverActionUtil = null;
        try {
            // Refresh connection every time you connect. In some cases, the server might
            // shut down and leave the stream corrupted. Only in that case the refresh
            // would be required ideally, But since we would mostly be connecting to
            // a device only once, this should not be a issue as number of network calls
            // would remain the same. It is only the second time your call would be an
            // overhead, and in that case it might be the case of failure.
            connectionManager.refreshConnectionResource(bluetoothDevice,
                    getApplicationContext());
            connectionResourceBundle = connectionManager.
                    getConnectionResource(bluetoothDevice, getApplicationContext());
            serverActionUtil = new ServerActionUtil(connectionResourceBundle);
        } catch(IOException ioe) {
            isSuccessful = false;
            Log.e(TAG, "Could not get connection resources.", ioe);
            //exception also means connection unsuccessful
        }

        notificationIntent = new Intent(ACTION_TRANSFER_FILE_AND_INSERT_DATA_INITIATED);
        notificationIntent.putExtra(PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
        sendBroadcast(notificationIntent);

        //Iterate over all files from the device and transfer data.
        for (Map.Entry<String, List<String>> elem : fileNameSensorInfo.entrySet()) {
            String fileName = elem.getKey();
            List<String> sensorNames = elem.getValue();

            //Send notification.
            notificationIntent = new Intent(ACTION_TRANSFER_FILE_AND_INSERT_DATA_STARTED);
            notificationIntent.putExtra(PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
            notificationIntent.putExtra(UpdateFragment.PARAM_FILENAME, fileName);
            sendBroadcast(notificationIntent);
            try {
                List<Measurement<Long, Float>> data = serverActionUtil.transferFile(fileName);

                if (data != null) {
                    for (Measurement measurement : data) {
                        String sensorId = measurement.getSensorId();
                        String deviceId = measurement.getDeviceId();
                        List<Pair<Long, Float>> sensorReadings = measurement.getMeasurement();
                        ReadingDao readingDao = new ReadingDao(this);
                        for (Pair<Long, Float> pair : sensorReadings) {
                            readingDao.insert(new Reading(deviceId,
                                    sensorId, pair.second, pair.first));
                        }
                    }
                } else {
                    isSuccessful = false;
                }
            } catch (ClassNotFoundException| IOException e) {
                isSuccessful = false;
                Log.e(TAG, "Received an exception while transferring file.", e);
            }
            notificationIntent = new Intent(ACTION_TRANSFER_FILE_AND_INSERT_DATA_FINISHED);
            notificationIntent.putExtra(PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
            notificationIntent.putExtra(UpdateFragment.PARAM_OPERATION_SUCCESSFUL, isSuccessful);
            sendBroadcast(notificationIntent);
        }
        notificationIntent = new Intent(ACTION_TRANSFER_FILE_AND_INSERT_DATA_ENDED);
        notificationIntent.putExtra(PARAM_BLUETOOTH_DEVICE, bluetoothDevice);
        sendBroadcast(notificationIntent);
    }
}
