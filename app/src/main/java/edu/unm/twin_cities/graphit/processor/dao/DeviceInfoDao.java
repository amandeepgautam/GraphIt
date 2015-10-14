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
import edu.unm.twin_cities.graphit.processor.model.DeviceInfo;
import edu.unm.twin_cities.graphit.processor.model.DeviceType.DeviceTypeID;

/**
 * Created by aman on 19/8/15.
 */
public class DeviceInfoDao extends AbstractDao {

    public DeviceInfoDao(final Context context) {
        super(context, DeviceInfoDao.class.getSimpleName(), Table.DEVICE_INFO.getTableName());
    }

    public long insert(final DeviceInfo deviceInfo) {
        ContentValues args = new ContentValues();
        args.put(Fields.DEVICE_ID.getFieldName(), deviceInfo.getDeviceLabel());
        args.put(Fields.DEVICE_LABEL.getFieldName(), deviceInfo.getDeviceLabel());
        args.put(Fields.DEVICE_TYPE_ID.getFieldName(), deviceInfo.getDeviceTypeId().toString());
        args.put(Fields.CREATED_AT.getFieldName(), deviceInfo.getCreatedAt());
        String deviceDescription = deviceInfo.getDeviceDescription();
        if (deviceDescription != null)
            args.put(Fields.DEVICE_DESCRIPTION.getFieldName(), deviceDescription);
        else
            args.putNull(Fields.DEVICE_DESCRIPTION.getFieldName());

        open();
        long rowNum = getSqLiteDatabase().insertWithOnConflict(getTableName(), null, args, SQLiteDatabase.CONFLICT_IGNORE);
        close();
        return rowNum;
    }

    public List<DeviceInfo> fetchAll() {
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, null, null, null, null, null);
        return parseResponse(cursor);
    }

    /**
     *
     * @param keyValue values will be used to construct the where clause with
     *                 map's keys as column names and map's value as thier required value
     * @return
     */
    public List<DeviceInfo> fetchWithConstraints(Map<String, String> keyValue) {
        String separator = "' and ";
        StringBuffer stringBuffer = new StringBuffer();
        for(Entry element : keyValue.entrySet() ) {
            stringBuffer.append(element.getKey() + "= '" + element.getValue() + separator);
        }
        stringBuffer.setLength(Math.max(stringBuffer.length() - separator.length() + 1, 0));
        String whereClause = stringBuffer.toString();

        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, whereClause, null, null, null, null);
        List<DeviceInfo> response = parseResponse(cursor);
        close();
        return response;
    }

    /**
     * Parses the reponse from the cursor.
     * @param cursor
     * @return
     */
    private List<DeviceInfo> parseResponse(Cursor cursor) {
        List<DeviceInfo> deviceInfos = Lists.newArrayList();
        while (cursor.moveToNext()) {
            String deviceId = cursor.getString(cursor.getColumnIndex(Fields.DEVICE_ID.getFieldName()));
            String deviceLabel = cursor.getString(cursor.getColumnIndex(Fields.DEVICE_LABEL.getFieldName()));
            String deviceDescription = cursor.getString(cursor.getColumnIndex(Fields.DEVICE_DESCRIPTION.getFieldName()));
            DeviceTypeID deviceTypeID = DeviceTypeID.valueOf(cursor.getString(cursor.getColumnIndex(Fields.DEVICE_TYPE_ID.getFieldName())));
            long createdAt = cursor.getLong(cursor.getColumnIndex(Fields.CREATED_AT.getFieldName()));
            deviceInfos.add(new DeviceInfo(deviceId, deviceLabel, deviceDescription, deviceTypeID, createdAt));
        }
        return deviceInfos;
    }
}
