package edu.unm.twin_cities.graphit.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Pair;

import java.util.List;

import edu.unm.twin_cities.graphit.activity.FileBrowserActivity;
import edu.unm.twin_cities.graphit.processor.dao.SensorDao;
import edu.unm.twin_cities.graphit.processor.dao.ReadingDao;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.Reading;
import edu.unm.twin_cities.graphit.processor.model.SensorType;
import edu.unm.twin_cities.graphit.util.Measurement;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DataService extends IntentService {

    private static String TAG = DataService.class.getSimpleName();

    public static final String READINGS_DATA_INSERT_COMPLETE = "edu.unm.twin_cities.graphit.service.READINGS_DATA_INSERT_COMPLETE";
    public static final String PARAM_INSERT_COMPLETE = "insert_complete";

    public DataService() {
        super("DataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch(action) {
                case FileBrowserActivity.ACTION_INSERT_READINGS_DATA:
                    List<Measurement> data = (List<Measurement>) intent.getExtras().get(FileBrowserActivity.PARAM_SENSOR_DATA);
                    String deviceId = (String) intent.getExtras().get(FileBrowserActivity.PARAM_DEVICE_ID);
                    handleActionInsertReadingsData(data, deviceId);
                    break;
                default:
                    Log.i(TAG, "Unidentified action");
                    break;
            }
        }
    }

    private void handleActionInsertReadingsData(List<Measurement> data, String deviceId) {
        boolean isSuccessful = true;
        try {
            for (Measurement measurement : data) {
                String sensorId = measurement.getSensorIdentifier();
                List<Pair<Long, Float>> sensorReadings = measurement.getMeasurement();
                ReadingDao readingDao = new ReadingDao(this);
                for (Pair<Long, Float> pair : sensorReadings) {
                    throw new IllegalStateException("Should not be called");
                    //readingDao.insert(new Reading(sensorId, pair.second, pair.first));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while inserting readings data" ,e);
            isSuccessful = false;
        }
        //TODO: Send notification back to Activity that data is acquired.
        Intent notificationIntent = new Intent(READINGS_DATA_INSERT_COMPLETE);
        notificationIntent.putExtra(PARAM_INSERT_COMPLETE, isSuccessful);
        notificationIntent.putExtra(FileBrowserActivity.PARAM_DEVICE_ID, deviceId);
        sendBroadcast(notificationIntent);
    }
}
