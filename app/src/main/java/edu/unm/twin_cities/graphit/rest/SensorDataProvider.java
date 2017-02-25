package edu.unm.twin_cities.graphit.rest;

import android.content.Context;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper.Fields;
import edu.unm.twin_cities.graphit.processor.dao.SensorDao;
import edu.unm.twin_cities.graphit.processor.dao.ReadingDao;
import edu.unm.twin_cities.graphit.processor.model.Sensor;
import edu.unm.twin_cities.graphit.processor.model.Reading;
import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;
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
    public PlotData getData(SensorTypeID sensorTypeID) {
        Map<String, List<String>> constraints = Maps.newHashMap();
        constraints.put(Fields.SENSOR_TYPE_ID.getFieldName()
                , Arrays.asList(sensorTypeID.getId()));
        SensorDao sensorDao = new SensorDao(context);
        List<Sensor> sensors = sensorDao.fetchWithConstraints(constraints);

        constraints.clear();
        Set<String> sensorIds = Sets.newHashSet();
        for(Sensor sensor : sensors) {
            sensorIds.add(sensor.getSensorId());
        }
        ReadingDao readingDao = new ReadingDao(context);
        Map<Pair<String, String>, List<Reading>> deviceReadings = readingDao
                .getDeviceReadings(Lists.newArrayList(sensorIds));

        PlotData plotData = new PlotData(deviceReadings);
        return plotData;
    }
}
