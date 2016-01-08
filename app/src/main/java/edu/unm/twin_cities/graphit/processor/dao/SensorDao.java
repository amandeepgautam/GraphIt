package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Table;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;

/**
 * Created by aman on 19/8/15.
 */
public class SensorDao extends AbstractDao {

    public SensorDao(final Context context) {
        super(context, SensorDao.class.getSimpleName(), Table.SENSOR.getTableName());
    }

    public long insert(final Sensor sensor) {
        ContentValues args = new ContentValues();

        args.put(Fields.DEVICE_ID.getFieldName(), sensor.getDeviceId());

        String sensorId = sensor.getSensorId();
        if (sensorId == null)
            sensorId = sensor.getSensorId();
        args.put(Fields.SENSOR_ID.getFieldName(), sensorId);

        args.put(Fields.SENSOR_LABEL.getFieldName(), sensor.getSensorLabel());
        args.put(Fields.SENSOR_TYPE_ID.getFieldName(), sensor.getSensorTypeId().toString());
        args.put(Fields.CREATED_AT.getFieldName(), sensor.getCreatedAt());

        String deviceDescription = sensor.getSensorDescription();
        if (deviceDescription != null)
            args.put(Fields.SENSOR_DESCRIPTION.getFieldName(), deviceDescription);
        else
            args.putNull(Fields.SENSOR_DESCRIPTION.getFieldName());

        open();
        long rowNum = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();

        return rowNum;
    }

    public List<Sensor> fetchAll() {
        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, null, null, null, null, null);
        List<Sensor> response = parseResponse(cursor);
        close();
        return response;
    }

    /**
     *
     * @param keyValue values will be used to construct the where clause with
     *                 map's keys as column names and map's value as thier required value
     * @return
     */
    public List<Sensor> fetchWithConstraints(Map<String, String> keyValue) {
        String separator = "' and ";
        StringBuilder stringBuilder = new StringBuilder();
        for(Entry element : keyValue.entrySet() ) {
            stringBuilder.append(element.getKey());
            stringBuilder.append("= '");
            stringBuilder.append(element.getValue());
            stringBuilder.append(separator);
        }
        stringBuilder.setLength(Math.max(stringBuilder.length() - separator.length() + 1, 0));
        String whereClause = stringBuilder.toString();

        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, whereClause, null, null, null, null);
        List<Sensor> response = parseResponse(cursor);
        close();
        return response;
    }

    /**
     * Parses the reponse from the cursor.
     * @param cursor
     * @return
     */
    private List<Sensor> parseResponse(Cursor cursor) {
        List<Sensor> sensors = Lists.newArrayList();
        while (cursor.moveToNext()) {
            String deviceId = cursor.getString(cursor.getColumnIndex(Fields.DEVICE_ID.getFieldName()));
            String sensorId = cursor.getString(cursor.getColumnIndex(Fields.SENSOR_ID.getFieldName()));
            String sensorLabel = cursor.getString(cursor.getColumnIndex(Fields.SENSOR_LABEL.getFieldName()));
            String sensorDescription = cursor.getString(cursor.getColumnIndex(Fields.SENSOR_DESCRIPTION.getFieldName()));
            SensorTypeID sensorTypeID = SensorTypeID.valueOf(cursor.getString(cursor.getColumnIndex(Fields.SENSOR_TYPE_ID.getFieldName())));
            long createdAt = cursor.getLong(cursor.getColumnIndex(Fields.CREATED_AT.getFieldName()));
            sensors.add(new Sensor(deviceId, sensorId, sensorLabel, sensorDescription, sensorTypeID, createdAt));
        }
        return sensors;
    }
}
