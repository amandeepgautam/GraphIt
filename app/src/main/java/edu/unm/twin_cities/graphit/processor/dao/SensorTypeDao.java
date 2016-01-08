package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Table;
import edu.unm.twin_cities.graphit.processor.model.SensorType;

/**
 * Created by aman on 19/8/15.
 */
public class SensorTypeDao extends AbstractDao {

    public SensorTypeDao(final Context context) {
        super(context, SensorTypeDao.class.getSimpleName(), Table.SENSOR_TYPE.getTableName());
    }

    public long insert(final SensorType sensorType) {
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

        open();
        long rowNUm = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();
        return rowNUm;
    }
}
