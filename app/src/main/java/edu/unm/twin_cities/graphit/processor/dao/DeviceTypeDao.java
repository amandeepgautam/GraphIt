package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Table;
import edu.unm.twin_cities.graphit.processor.model.DeviceType;

/**
 * Created by aman on 19/8/15.
 */
public class DeviceTypeDao extends AbstractDao {

    public DeviceTypeDao(final Context context) {
        super(context, DeviceReadingDao.class.getSimpleName(), Table.DEVICE_TYPE.getTableName());
    }

    public long insert(final DeviceType deviceType) {
        ContentValues args = new ContentValues();

        args.put(Fields.DEVICE_TYPE_ID.getFieldName(), deviceType.getDeviceTypeId().name());

        String value = deviceType.getDeviceTypeDescription();
        if(value == null)
            args.putNull(Fields.DEVICE_TYPE_DESCRIPTION.getFieldName());
        else
            args.put(Fields.DEVICE_TYPE_DESCRIPTION.getFieldName(), value);

        value = deviceType.getDeviceTypeLabel();
        if(value == null)
            args.putNull(Fields.DEVICE_TYPE_LABEL.getFieldName());
        else
            args.put(Fields.DEVICE_TYPE_LABEL.getFieldName(), value);

        open();
        long rowNUm = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();
        return rowNUm;
    }
}
