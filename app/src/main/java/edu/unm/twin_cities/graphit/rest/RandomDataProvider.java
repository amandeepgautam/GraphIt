package edu.unm.twin_cities.graphit.rest;

import android.content.Context;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.Reading;
import edu.unm.twin_cities.graphit.processor.model.SensorType;
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
        String actualDevice = "MAC_ADDRESS";

        String deviceIdOne = "ME@Location-XYZ";
        String deviceIdTwo = "ME@Location-PQR";
        Sensor sensor = new Sensor(actualDevice, deviceIdOne, "Test DeviceSensorMap", "It is a dummy device", SensorType.SensorTypeID.DEFAULT, 123456L);
        List<Sensor> sensors = Lists.newArrayList(sensor);

        Map<String, List<Reading>> deviceReadings = Maps.newHashMap();
        List<Reading> deviceReading = Lists.newArrayList();
        deviceReading.add(new Reading(deviceIdOne, 56, "cm", 12345656L));
        deviceReading.add(new Reading(deviceIdOne, 59, "cm", 12345555L));
        deviceReading.add(new Reading(deviceIdOne, 62, "cm", 12346666L));
        deviceReading.add(new Reading(deviceIdOne, 65, "cm", 12347777L));
        deviceReading.add(new Reading(deviceIdOne, 68, "cm", 12348888L));
        deviceReading.add(new Reading(deviceIdOne, 71, "cm", 12349999L));
        deviceReading.add(new Reading(deviceIdOne, 74, "cm", 12351111L));
        deviceReading.add(new Reading(deviceIdOne, 77, "cm", 12352222L));
        deviceReading.add(new Reading(deviceIdOne, 80, "cm", 12353333L));
        deviceReading.add(new Reading(deviceIdOne, 83, "cm", 12354444L));
        deviceReading.add(new Reading(deviceIdOne, 86, "cm", 12355555L));
        deviceReading.add(new Reading(deviceIdOne, 89, "cm", 12356666L));
        deviceReading.add(new Reading(deviceIdOne, 92, "cm", 12357777L));
        deviceReading.add(new Reading(deviceIdOne, 95, "cm", 12358888L));
        deviceReading.add(new Reading(deviceIdOne, 98, "cm", 12359999L));
        deviceReading.add(new Reading(deviceIdOne, 101, "cm", 12361111L));


        deviceReadings.put(deviceIdOne, deviceReading);

        deviceReading = Lists.newArrayList();
        deviceReading.add(new Reading(deviceIdTwo, 101, "cm", 12345656L));
        deviceReading.add(new Reading(deviceIdTwo, 98, "cm", 12345555L));
        deviceReading.add(new Reading(deviceIdTwo, 95, "cm", 12346666L));
        deviceReading.add(new Reading(deviceIdTwo, 92, "cm", 12347777L));
        deviceReading.add(new Reading(deviceIdTwo, 89, "cm", 12348888L));
        deviceReading.add(new Reading(deviceIdTwo, 86, "cm", 12349999L));
        deviceReading.add(new Reading(deviceIdTwo, 83, "cm", 12351111L));
        deviceReading.add(new Reading(deviceIdTwo, 80, "cm", 12352222L));
        deviceReading.add(new Reading(deviceIdTwo, 77, "cm", 12353333L));
        deviceReading.add(new Reading(deviceIdTwo, 74, "cm", 12354444L));
        deviceReading.add(new Reading(deviceIdTwo, 71, "cm", 12355555L));
        deviceReading.add(new Reading(deviceIdTwo, 68, "cm", 12356666L));
        deviceReading.add(new Reading(deviceIdTwo, 65, "cm", 12357777L));
        deviceReading.add(new Reading(deviceIdTwo, 62, "cm", 12358888L));
        deviceReading.add(new Reading(deviceIdTwo, 59, "cm", 12359999L));
        deviceReading.add(new Reading(deviceIdTwo, 56, "cm", 12361111L));


        deviceReadings.put(deviceIdTwo, deviceReading);

        PlotData plotData = new PlotData(deviceReadings);
        return plotData;

    }
}
