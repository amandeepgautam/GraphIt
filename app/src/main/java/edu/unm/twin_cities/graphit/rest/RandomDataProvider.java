package edu.unm.twin_cities.graphit.rest;

import android.content.Context;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.Reading;
import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;
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


    public PlotData getData(SensorTypeID sensorTypeID) {
        String actualDevice = "MAC_ADDRESS";

        String deviceIdOne = "Location-XYZ";
        String deviceIdTwo = "Location-PQR";
        String sensorId = "Me";
        Sensor sensor = new Sensor(actualDevice, deviceIdOne, "Test DeviceSensorMap", "It is a dummy device", "dummy/file/path", SensorType.SensorTypeID.DEFAULT, 123456L);
        List<Sensor> sensors = Lists.newArrayList(sensor);

        Map<Pair<String, String>, List<Reading>> deviceReadings = Maps.newHashMap();
        List<Reading> deviceReading = Lists.newArrayList();

        deviceReading.add(new Reading(deviceIdOne, sensorId, 56, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12345656L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 59, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12345555L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 62, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12346666L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 65, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12347777L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 68, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12348888L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 71, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12349999L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 74, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12351111L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 77, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12352222L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 80, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12353333L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 83, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12354444L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 86, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12355555L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 89, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12356666L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 92, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12357777L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 95, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12358888L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 98, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12359999L));
        deviceReading.add(new Reading(deviceIdOne, sensorId, 101, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12361111L));


        deviceReadings.put(new Pair<>(deviceIdOne, sensorId), deviceReading);

        deviceReading = Lists.newArrayList();
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 101, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12345656L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 98, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12345555L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 95, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12346666L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 92, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12347777L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 89, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12348888L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 86, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12349999L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 83, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12351111L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 80, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12352222L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 77, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12353333L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 74, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12354444L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 71, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12355555L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 68, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12356666L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 65, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12357777L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 62, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12358888L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 59, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12359999L));
        deviceReading.add(new Reading(deviceIdTwo, sensorId, 56, SensorType.MeasurementUnit.TEMPRATURE_MEASUREMENT_UNIT, 12361111L));


        deviceReadings.put(new Pair<>(deviceIdTwo, sensorId), deviceReading);

        PlotData plotData = new PlotData(deviceReadings);
        return plotData;

    }
}
