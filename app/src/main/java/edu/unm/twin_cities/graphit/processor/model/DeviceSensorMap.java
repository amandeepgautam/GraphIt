package edu.unm.twin_cities.graphit.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * The object encapsulates the relation between the device which
 * the sensors are attached to.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class DeviceSensorMap {
    /**
     * The identifier for device. It would most likely be
     * the bluetooth MAC address.
     */
    private String deviceId;

    /**
     * Sensor identifier.
     */
    private String sensorId;

    /**
     * File location for sensor data.
     */
    private String sensorFileLocation;
}
