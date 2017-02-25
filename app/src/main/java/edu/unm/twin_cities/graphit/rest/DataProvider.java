package edu.unm.twin_cities.graphit.rest;

import edu.unm.twin_cities.graphit.processor.model.PlotData;
import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;

/**
 * Created by aman on 10/8/15.
 */
public interface DataProvider {
    PlotData getData(SensorTypeID sensorTypeID);
}