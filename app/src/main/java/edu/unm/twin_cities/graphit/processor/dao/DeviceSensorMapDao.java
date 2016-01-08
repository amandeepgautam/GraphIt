package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.model.Device;
import edu.unm.twin_cities.graphit.processor.model.DeviceSensorMap;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.SensorType;

/**
 * Created by aman on 30/11/15.
 */
public class DeviceSensorMapDao extends AbstractDao {

    public DeviceSensorMapDao(final Context context) {
        super(context, DeviceSensorMapDao.class.getSimpleName(), DatabaseHelper.Table.DEVICE_SENSOR.getTableName());
    }

    public long insert(final DeviceSensorMap deviceSensorMap) {
        ContentValues args = new ContentValues();
        args.put(DatabaseHelper.Fields.DEVICE_ID.getFieldName(), deviceSensorMap.getDeviceId());
        args.put(DatabaseHelper.Fields.SENSOR_ID.getFieldName(), deviceSensorMap.getSensorId());
        String fileLocation = deviceSensorMap.getSensorFileLocation();
        if (fileLocation == null)
            args.putNull(DatabaseHelper.Fields.SENSOR_FILE_LOC.getFieldName());
        else
            args.put(DatabaseHelper.Fields.SENSOR_FILE_LOC.getFieldName(), fileLocation);

        open();
        long rowNum = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();
        return rowNum;
    }

    /**
     *
     * @param keyValue values will be used to construct the where clause with
     *                 map's keys as column names and map's value as thier required value
     * @return
     */
    public List<DeviceSensorMap> fetchWithConstraints(Map<String, String> keyValue) {
        String separator = "' and ";
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry element : keyValue.entrySet() ) {
            stringBuilder.append(element.getKey());
            stringBuilder.append("= '");
            stringBuilder.append(element.getValue());
            stringBuilder.append(separator);
        }
        stringBuilder.setLength(Math.max(stringBuilder.length() - separator.length() + 1, 0));
        String whereClause = stringBuilder.toString();

        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, whereClause, null, null, null, null);
        List<DeviceSensorMap> response = parseResponse(cursor);
        close();
        return response;
    }

    /**
     * Parses the reponse from the cursor.
     * @param cursor
     * @return
     */
    private List<DeviceSensorMap> parseResponse(Cursor cursor) {
        List<DeviceSensorMap> response = Lists.newArrayList();
        while (cursor.moveToNext()) {
            String deviceId = cursor.getString(cursor.getColumnIndex(Fields.DEVICE_ID.getFieldName()));
            String sensorId = cursor.getString(cursor.getColumnIndex(Fields.SENSOR_ID.getFieldName()));
            String fileLoc = cursor.getString(cursor.getColumnIndex(Fields.SENSOR_FILE_LOC.getFieldName()));
            response.add(new DeviceSensorMap(deviceId, sensorId, fileLoc));
        }
        return response;
    }
}
