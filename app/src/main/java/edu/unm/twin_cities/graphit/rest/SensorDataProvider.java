package edu.unm.twin_cities.graphit.rest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.dao.DeviceInfoDao;
import edu.unm.twin_cities.graphit.processor.dao.DeviceReadingDao;
import edu.unm.twin_cities.graphit.processor.model.DeviceInfo;
import edu.unm.twin_cities.graphit.processor.model.DeviceReading;
import edu.unm.twin_cities.graphit.processor.model.DeviceType.DeviceTypeID;
import edu.unm.twin_cities.graphit.processor.model.PlotData;

/**
 * Created by aman on 24/9/15.
 */
public class SensorDataProvider implements DataProvider {
    Context context;

    public SensorDataProvider(Context context) {
        this.context = context;
    }

    @Override
    public PlotData getData() {
        Map<String, String> constraints = Maps.newHashMap();
        constraints.put(Fields.DEVICE_TYPE_ID.getFieldName(), DeviceTypeID.DEFAULT.name());
        DeviceInfoDao deviceInfoDao = new DeviceInfoDao(context);
        List<DeviceInfo> deviceInfos = deviceInfoDao.fetchWithConstraints(constraints);

        constraints.clear();
        List<String> deviceIds = Lists.newArrayList();
        for(DeviceInfo deviceInfo : deviceInfos) {
            deviceIds.add(deviceInfo.getDeviceId());
        }
        DeviceReadingDao deviceReadingDao = new DeviceReadingDao(context);
        Map<String, List<DeviceReading>> deviceReadings = deviceReadingDao.getDeviceReadings(deviceIds);

        PlotData plotData = new PlotData(deviceReadings);
        return plotData;
    }
}
