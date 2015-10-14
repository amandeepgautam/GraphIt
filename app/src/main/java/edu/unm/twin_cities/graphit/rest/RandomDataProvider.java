package edu.unm.twin_cities.graphit.rest;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.processor.dao.DeviceInfoDao;
import edu.unm.twin_cities.graphit.processor.dao.DeviceReadingDao;
import edu.unm.twin_cities.graphit.processor.model.DeviceInfo;
import edu.unm.twin_cities.graphit.processor.model.DeviceReading;
import edu.unm.twin_cities.graphit.processor.model.DeviceType;
import edu.unm.twin_cities.graphit.processor.model.PlotData;

/**
 * Created by aman on 10/8/15.
 */
public class RandomDataProvider implements DataProvider {

    Context context;

    public RandomDataProvider(Context context) {
        this.context = context;
    }


    public PlotData getData() {

        String deviceIdOne = "ME@Location-XYZ";
        String deviceIdTwo = "ME@Location-PQR";
        DeviceInfo deviceInfo = new DeviceInfo(deviceIdOne, "Test Device", "It is a dummy device", DeviceType.DeviceTypeID.DEFAULT, 123456L);
        List<DeviceInfo> deviceInfos = Lists.newArrayList(deviceInfo);

        Map<String, List<DeviceReading>> deviceReadings = Maps.newHashMap();
        List<DeviceReading> deviceReading = Lists.newArrayList();
        deviceReading.add(new DeviceReading(deviceIdOne, 56, "cm", 12345656L));
        deviceReading.add(new DeviceReading(deviceIdOne, 59, "cm", 22345556L));
        deviceReading.add(new DeviceReading(deviceIdOne, 9, "cm", 33333333L));
        deviceReading.add(new DeviceReading(deviceIdOne, 5, "cm", 44444444L));
        deviceReading.add(new DeviceReading(deviceIdOne, 90, "cm", 55555555L));
        deviceReading.add(new DeviceReading(deviceIdOne, 9, "cm", 66666666L));
        deviceReading.add(new DeviceReading(deviceIdOne, 56, "cm", 77777777L));
        deviceReading.add(new DeviceReading(deviceIdOne, 59, "cm", 88888888L));

        deviceReadings.put(deviceIdOne, deviceReading);

        deviceReading = Lists.newArrayList();
        deviceReading.add(new DeviceReading(deviceIdTwo, 24, "cm", 12345656L));
        deviceReading.add(new DeviceReading(deviceIdTwo, 5, "cm", 22345556L));
        deviceReading.add(new DeviceReading(deviceIdTwo, 19, "cm", 22345678L));
        deviceReading.add(new DeviceReading(deviceIdTwo, 67, "cm", 44444444L));
        deviceReading.add(new DeviceReading(deviceIdTwo, 12, "cm", 55555555L));
        deviceReading.add(new DeviceReading(deviceIdTwo, 90, "cm", 66666666L));
        deviceReading.add(new DeviceReading(deviceIdTwo, 56, "cm", 77777777L));
        deviceReading.add(new DeviceReading(deviceIdTwo, 73.6F, "cm", 88888888L));


        deviceReadings.put(deviceIdTwo, deviceReading);

        PlotData plotData = new PlotData(deviceReadings);
        return plotData;

    }
}
