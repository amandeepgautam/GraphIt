package edu.unm.twin_cities.graphit.util;

import android.content.Context;

import com.google.common.collect.Maps;

import java.util.Map;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.dao.UserPrefDao;
import edu.unm.twin_cities.graphit.processor.model.SensorType;
import edu.unm.twin_cities.graphit.processor.model.UserPref;

/**
 * A utility class for preferences or other data collected during application
 * browsing which can be used to enhance user interaction.
 */
public final class UserPreferencesUtil {

    private static SensorType.SensorTypeID lastViewingSensorType;

    public static SensorType.SensorTypeID getLastViewingSensorType(Context context) {
        UserPrefDao userPrefDao = new UserPrefDao(context);
        Map<String, String> constraints = Maps.newHashMap();
        String key = DatabaseHelper.Fields.USER_PREF_KEY.getFieldName();
        String value = UserPref.UserPrefType.SENSOR_TYPE_VIEWING.getStr();
        constraints.put(key, value);

        UserPref userPref = userPrefDao.fetchWithConstraints(constraints);
        if(userPref == null) {
            return null;
        } else {
            return SensorType.SensorTypeID.fromString(userPref.getValue());
        }
    }
}
