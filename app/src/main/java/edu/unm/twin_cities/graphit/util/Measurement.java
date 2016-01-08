package edu.unm.twin_cities.graphit.util;

import android.util.Pair;

import java.io.Serializable;
import java.util.List;

/**
 * Created by aman on 20/9/15.
 */
public interface Measurement<X, Y> {

    /**
     * Get a identifier for sensor.
     * @return returns the sensor identifier.
     */
    String getSensorIdentifier();

    /**
     * Readings would be tuple of (x-axis, yaxis) values.
     * @return list of all readings.
     */
    List<Pair<X, Y>> getMeasurement();

    /**
     * Add a (x, y) tuple to the existing readings.
     * @param measurement a measured tuple
     */
    void addMeasurement(Pair<X, Y> measurement);
}
