package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Table;
import edu.unm.twin_cities.graphit.processor.model.DeviceReading;

/**
 * Created by aman on 20/8/15.
 */
public class DeviceReadingDao extends AbstractDao {

    public DeviceReadingDao(final Context context) {
        super(context, DeviceReadingDao.class.getSimpleName(), Table.DEVICE_READING.getTableName());
    }

    public long insert(final DeviceReading deviceReading) {
        ContentValues args = new ContentValues();
        args.put(Fields.DEVICE_ID.getFieldName(), deviceReading.getDeviceId());
        args.put(Fields.READING.getFieldName(), deviceReading.getReading());
        args.put(Fields.TIMESTAMP.getFieldName(), deviceReading.getTimestamp());
        String measurementUnit = deviceReading.getMeasurementUnit();
        if (measurementUnit != null)
            args.put(Fields.MEASUREMENT_UNIT.getFieldName(), measurementUnit);
        else
            args.putNull(Fields.MEASUREMENT_UNIT.getFieldName());

        open();//writable database queries are cached, hence no performance issues.
        long rowNum = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();  //look into performance issue because of closing of db.
        return rowNum;
    }

    public DeviceReading getDeviceReadings(String deviceId) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Map<String, List<DeviceReading>> getDeviceReadings(List<String> deviceIds) {
        String devices = Joiner.on("', '").skipNulls().join(deviceIds);
        String whereClause = Fields.DEVICE_ID.getFieldName() + " IN ('" + devices + "')";

        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, whereClause, null, null, null, null);
        Map<String, List<DeviceReading>> response = parseAllFieldsResponse(cursor);
        close();
        return response;
    }

    private Map<String, List<DeviceReading>> parseAllFieldsResponse(Cursor cursor) {
        Map<String, List<DeviceReading>> allDeviceReadings = Maps.newHashMap();
        while(cursor.moveToNext()) {
            String deviceId = cursor.getString(cursor.getColumnIndex(Fields.DEVICE_ID.getFieldName()));
            float reading = cursor.getFloat(cursor.getColumnIndex(Fields.READING.getFieldName()));
            String measurementUnit = cursor.getString(cursor.getColumnIndex(Fields.MEASUREMENT_UNIT.getFieldName()));
            long timestamp = cursor.getLong(cursor.getColumnIndex(Fields.TIMESTAMP.getFieldName()));

            List<DeviceReading> deviceReadings = allDeviceReadings.get(deviceId);
            if (deviceReadings == null) {
                deviceReadings = Lists.newArrayList();
                allDeviceReadings.put(deviceId, deviceReadings);
            }
            deviceReadings.add(new DeviceReading(deviceId, reading, measurementUnit, timestamp));
        }
        return allDeviceReadings;
    }
}
