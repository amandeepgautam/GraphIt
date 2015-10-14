package edu.unm.twin_cities.graphit.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

import edu.unm.twin_cities.graphit.activity.FileBrowserActivity;
import edu.unm.twin_cities.graphit.processor.dao.DeviceInfoDao;
import edu.unm.twin_cities.graphit.processor.dao.DeviceReadingDao;
import edu.unm.twin_cities.graphit.processor.model.DeviceInfo;
import edu.unm.twin_cities.graphit.processor.model.DeviceReading;
import edu.unm.twin_cities.graphit.processor.model.DeviceType;
import edu.unm.twin_cities.graphit.processor.model.SensorReading;
import edu.unm.twin_cities.graphit.processor.model.SensorReadings;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DataService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    public static final String RECEIVE_DATA_INSERT_COMPLETE = "edu.unm.twin_cities.graphit.service.RECEIVE_DATA_INSERT_COMPLETE";
    public static final String INSERT_COMPLETE = "insert_complete";

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "edu.unm.twin_cities.graphit.service.action.FOO";
    private static final String ACTION_BAZ = "edu.unm.twin_cities.graphit.service.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "edu.unm.twin_cities.graphit.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "edu.unm.twin_cities.graphit.service.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public DataService() {
        super("DataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            List<SensorReading> data = (List <SensorReading>) intent.getExtras().get(FileBrowserActivity.PARAM_SENSOR_DATA);
            for (SensorReading sensorReading : data) {
                String sensorId = sensorReading.getSensorIdentifier();
                List<Pair<Long, Float>> sensorReadings = sensorReading.getReadings();
                DeviceInfo deviceInfo = new DeviceInfo(sensorId, DeviceType.DeviceTypeID.DEFAULT, System.currentTimeMillis());
                //TODO: Make this a transaction.
                DeviceInfoDao deviceInfoDao = new DeviceInfoDao(this);
                deviceInfoDao.insert(deviceInfo);
                DeviceReadingDao deviceReadingDao = new DeviceReadingDao(this);
                for(Pair<Long, Float> pair: sensorReadings) {
                    deviceReadingDao.insert(new DeviceReading(sensorId, pair.second, pair.first));
                }
            }
            //TODO: Send notification back to Activity that data is acquired.
            Intent notificationIntent = new Intent(RECEIVE_DATA_INSERT_COMPLETE);
            notificationIntent.putExtra(INSERT_COMPLETE, true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(notificationIntent);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
