package edu.unm.twin_cities.graphit.processor.model;

import android.util.Pair;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aman on 20/9/15.
 */
public interface SensorReading<X, Y> {
    /**
     * Get a identifier for sensor.
     * @return returns the sensor identifier.
     */
    String getSensorIdentifier();

    /**
     * Readings would be tuple of (x-axis, yaxis) values.
     * @return list of all readings.
     */
    List<Pair<X, Y>> getReadings();

    /**
     * Add a (x, y) tuple to the existing readings.
     * @param reading a measured tuple
     */
    void addReading(Pair<X, Y> reading);
}
