package edu.unm.twin_cities.graphit.processor.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.UserPref;

/**
 * Collection of uitlity functions to interact with table UserPref.
 */
public class UserPrefDao extends AbstractDao {

    public UserPrefDao(final Context context) {
        super(context, SensorDao.class.getSimpleName(), DatabaseHelper.Table.USER_PREF.getTableName());
    }

    /**
     * Insert or replace a record into the database
     * @param userPref
     * @return
     */
    public long insertOrReplace(UserPref userPref) {
        ContentValues args = new ContentValues();
        args.put(DatabaseHelper.Fields.USER_PREF_KEY.getFieldName(), userPref.getKeyStr());
        args.put(DatabaseHelper.Fields.USER_PREF_VALUE.getFieldName(), userPref.getValue());

        open();
        long rowNum = getSqLiteDatabase().replace(getTableName(), null, args);
        close();

        return rowNum;
    }

    public Map<String, UserPref> fetchAll() {
        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, null, null, null, null, null);
        List<UserPref> response = parseResponse(cursor);
        close();
        Map<String, UserPref> mappedResponse = Maps.newHashMap();
        for(UserPref userPref: response) {
            mappedResponse.put(userPref.getKeyStr(), userPref);
        }
        return mappedResponse;
    }

    public UserPref fetchWithConstraints(Map<String, String> keyValue) {
        Preconditions.checkArgument(keyValue.size() != 0);

        String separator = " and ";
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, String> element : keyValue.entrySet()) {
            stringBuilder.append(element.getKey());
            stringBuilder.append("= ");
            stringBuilder.append("'");
            stringBuilder.append(element.getValue());
            stringBuilder.append("'");
            stringBuilder.append(separator);
        }
        stringBuilder.setLength(Math.max(stringBuilder.length() - separator.length() + 1, 0));
        String whereClause = stringBuilder.toString();

        open();
        Cursor cursor = getSqLiteDatabase().query(getTableName(), null, whereClause, null, null, null, null);
        List<UserPref> response = parseResponse(cursor);
        close();
        if (response.size() == 0)  {
            return null;
        } else if(response.size() == 1) {
            return response.get(0);
        } else {
            throw new IllegalStateException("The query  should have returned only one element.");
        }
    }

    private List<UserPref> parseResponse(Cursor cursor) {
        List<UserPref> response = Lists.newArrayList();
        while (cursor.moveToNext()) {
            String key = cursor.getString(cursor.getColumnIndex(
                    DatabaseHelper.Fields.USER_PREF_KEY.getFieldName()));
            String value = cursor.getString(cursor.getColumnIndex(
                    DatabaseHelper.Fields.USER_PREF_VALUE.getFieldName()));

            response.add(new UserPref(key, value));
        }
        return response;
    }
}
