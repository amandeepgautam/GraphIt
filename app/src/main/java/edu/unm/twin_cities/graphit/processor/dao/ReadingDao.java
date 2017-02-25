package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Table;
import edu.unm.twin_cities.graphit.processor.model.Reading;
import edu.unm.twin_cities.graphit.processor.model.SensorType;

/**
 * Created by aman on 20/8/15.
 */
public class ReadingDao extends AbstractDao {

    public ReadingDao(final Context context) {
        super(context, ReadingDao.class.getSimpleName(), Table.READING.getTableName());
    }

    public long insert(final Reading reading) {
        ContentValues args = new ContentValues();
        args.put(Fields.DEVICE_ID.getFieldName(), reading.getDeviceId());
        args.put(Fields.SENSOR_ID.getFieldName(), reading.getSensorId());
        args.put(Fields.READING.getFieldName(), reading.getReading());
        args.put(Fields.TIMESTAMP.getFieldName(), reading.getTimestamp());
        //String measurementUnit = reading.getMeasurementUnit().getUnit();
        String measurementUnit = null;
        if (measurementUnit != null)
            args.put(Fields.MEASUREMENT_UNIT.getFieldName(), measurementUnit);
        else
            args.putNull(Fields.MEASUREMENT_UNIT.getFieldName());

        open();//writable database queries are cached, hence no performance issues.
        long rowNum = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();  //look into performance issue because of closing of db.
        return rowNum;
    }

    public Map<Pair<String, String>, List<Reading>> getDeviceReadings(List<String> sensorIds) {
        String devices = Joiner.on("', '").skipNulls().join(sensorIds);
        String whereClause = Fields.SENSOR_ID.getFieldName() + " IN ('" + devices + "')";

        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, whereClause, null, null, null, null);
        //Cursor cursor = getSqLiteDatabase().query(getTableName(), null, null, null, null, null, null);
        Map<Pair<String, String>, List<Reading>> response = parseAllFieldsResponse(cursor);
        close();

        return response;
    }

    private Map<Pair<String, String>, List<Reading>> parseAllFieldsResponse(Cursor cursor) {
        Map<Pair<String, String>, List<Reading>> allDeviceReadings = Maps.newHashMap();
        while(cursor.moveToNext()) {
            String deviceId = cursor.getString(cursor.getColumnIndex(Fields.DEVICE_ID.getFieldName()));
            String sensorId = cursor.getString(cursor.getColumnIndex(Fields.SENSOR_ID.getFieldName()));
            float reading = cursor.getFloat(cursor.getColumnIndex(Fields.READING.getFieldName()));
            String measurementUnit = cursor.getString(cursor.getColumnIndex(Fields.MEASUREMENT_UNIT.getFieldName()));
            long timestamp = cursor.getLong(cursor.getColumnIndex(Fields.TIMESTAMP.getFieldName()));

            Pair<String, String> key = new Pair<String, String>(deviceId, sensorId);
            List<Reading> deviceReadings = allDeviceReadings.get(key);
            if (deviceReadings == null) {
                deviceReadings = Lists.newArrayList();
                allDeviceReadings.put(key, deviceReadings);
            }
            deviceReadings.add(new Reading(deviceId, sensorId, reading,
                    SensorType.MeasurementUnit.fromString(measurementUnit), timestamp));
        }
        return allDeviceReadings;
    }
}
