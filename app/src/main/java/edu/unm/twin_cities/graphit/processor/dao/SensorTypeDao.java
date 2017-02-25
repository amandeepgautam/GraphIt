package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.collect.Lists;

import java.util.List;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Table;
import edu.unm.twin_cities.graphit.processor.model.Device;
import edu.unm.twin_cities.graphit.processor.model.SensorType;

/**
 * Created by aman on 19/8/15.
 */
public class SensorTypeDao extends AbstractDao {

    public SensorTypeDao(final Context context) {
        super(context, SensorTypeDao.class.getSimpleName(), Table.SENSOR_TYPE.getTableName());
    }

    public long insert(final SensorType sensorType) {
        ContentValues args = prepareArguments(sensorType);
        open();
        long rowNUm = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();
        return rowNUm;
    }

    public ContentValues prepareArguments(final SensorType sensorType) {
        ContentValues args = new ContentValues();

        args.put(Fields.SENSOR_TYPE_ID.getFieldName(), sensorType.getSensorTypeId().name());

        String value = sensorType.getSensorTypeDescription();
        if(value == null)
            args.putNull(Fields.SENSOR_TYPE_DESCRIPTION.getFieldName());
        else
            args.put(Fields.SENSOR_TYPE_DESCRIPTION.getFieldName(), value);

        value = sensorType.getSensorTypeLabel();
        if(value == null)
            args.putNull(Fields.SENSOR_TYPE_LABEL.getFieldName());
        else
            args.put(Fields.SENSOR_TYPE_LABEL.getFieldName(), value);

        value = sensorType.getMeasurementUnit().getUnit();    ///Measurement unit should not be null.
        args.put(Fields.MEASUREMENT_UNIT.getFieldName(), value);
        return args;
    }

    public List<ContentValues> initializeSensorType() {
        List<ContentValues> sensorTypes = Lists.newArrayList();

        SensorType sensorType = new SensorType("Default sensor type", SensorType.SensorTypeID.DEFAULT,
                "Sensors with no specifications belong to this type", SensorType.MeasurementUnit.NONE);
        sensorTypes.add(prepareArguments(sensorType));

        sensorType = new SensorType("Soil moisture sensor", SensorType.SensorTypeID.SOIL_MOISTURE,
                "Soil moisture sensor", SensorType.MeasurementUnit.SOIL_MOISTURE_MEASUREMENT_UNIT);
        sensorTypes.add(prepareArguments(sensorType));

        return sensorTypes;       //return number of successful insertions, mostly useless.
    }

    public List<SensorType> fetchAll() {
        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, null, null, null, null, null);
        List<SensorType> response = parseResponse(cursor);
        close();
        return response;
    }

    private List<SensorType> parseResponse(Cursor cursor) {
        List<SensorType> sensorTypes = Lists.newArrayList();
        while (cursor.moveToNext()) {
            String sensorTypeLabel = cursor.getString(cursor.getColumnIndex(Fields.SENSOR_TYPE_LABEL.getFieldName()));
            SensorType.SensorTypeID sensorTypeId = SensorType.SensorTypeID.fromString(
                    cursor.getString(cursor.getColumnIndex(Fields.SENSOR_TYPE_ID.getFieldName())));
            String sensorTypeDescription = cursor.getString(
                    cursor.getColumnIndex(Fields.SENSOR_TYPE_DESCRIPTION.getFieldName()));
            SensorType.MeasurementUnit measurementUnit = SensorType.MeasurementUnit.fromString(
                    cursor.getString(cursor.getColumnIndex(Fields.MEASUREMENT_UNIT.getFieldName())));

            sensorTypes.add(new SensorType(sensorTypeLabel, sensorTypeId, sensorTypeDescription, measurementUnit));
        }
        return sensorTypes;
    }

}
