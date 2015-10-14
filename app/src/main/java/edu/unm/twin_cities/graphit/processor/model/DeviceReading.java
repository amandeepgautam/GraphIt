package edu.unm.twin_cities.graphit.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by aman on 20/8/15.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class DeviceReading {
    /**
     * The identifier for device.
     */
    private String deviceId;

    /**
     * Reading/measurement.
     */
    private float reading;

    /**
     * Unit of measurement.
     */
    private String measurementUnit;

    /**
     * Time of the reading.
     */
    private long timestamp;

    /**
     * Constructor excluding the auto inc. id
     */
    public DeviceReading(String deviceId, float reading, long timestamp) {
        this(deviceId, reading, null, timestamp);
    }
}
