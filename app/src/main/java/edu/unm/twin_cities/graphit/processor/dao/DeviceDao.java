package edu.unm.twin_cities.graphit.processor.dao;

import android.app.admin.DeviceAdminReceiver;
import android.bluetooth.BluetoothClass;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.ContactsContract;

import com.google.common.collect.Lists;

import java.util.List;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.model.Device;
import edu.unm.twin_cities.graphit.processor.model.DeviceSensorMap;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.SensorType;

/**
 * Created by aman on 30/11/15.
 */
public class DeviceDao extends AbstractDao {

    public DeviceDao(final Context context) {
        super(context, DeviceDao.class.getSimpleName(), DatabaseHelper.Table.DEVICE.getTableName());
    }

    public long insert(final Device device) {
        ContentValues args = new ContentValues();
        args.put(DatabaseHelper.Fields.DEVICE_ID.getFieldName(), device.getDeviceId());
        args.put(DatabaseHelper.Fields.DEVICE_LABEL.getFieldName(), device.getDeviceLabel());

        open();
        long rowNum = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();
        return rowNum;
    }

    public List<Device> fetchAll() {
        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, null, null, null, null, null);
        List<Device> response = parseResponse(cursor);
        close();
        return response;
    }

    private List<Device> parseResponse(Cursor cursor) {
        List<Device> devices = Lists.newArrayList();
        while (cursor.moveToNext()) {
            String deviceId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.Fields.DEVICE_ID.getFieldName()));
            String deviceLabel = cursor.getString(cursor.getColumnIndex(DatabaseHelper.Fields.DEVICE_LABEL.getFieldName()));
            devices.add(new Device(deviceId, deviceLabel));
        }
        return devices;
    }
}
