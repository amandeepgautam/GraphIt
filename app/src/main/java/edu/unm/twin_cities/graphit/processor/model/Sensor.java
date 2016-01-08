package edu.unm.twin_cities.graphit.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;
/**
 * Created by aman on 10/8/15.
 */

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class Sensor {

    /**
     * Unique identifier for the device.
     */
    private String deviceId;
    /**
     * Unique identifier for the device.
     */
    private String sensorId;

    /**
     * Readable name for the device.
     */
    private String sensorLabel;

    /**
     * Description about the device.
     */
    private String sensorDescription;

    /**
     * Type of device.
     */
    private SensorTypeID sensorTypeId;

    /**
     * Time stamp of creation for the entry.
     */
    private long createdAt;

    public Sensor(String deviceId, String sensorLabel, SensorTypeID sensorTypeId, long createdAt) {
        this.deviceId = deviceId;
        this.sensorId = null;
        this.sensorLabel = sensorLabel;
        this.sensorTypeId = sensorTypeId;
        this.createdAt = createdAt;
    }
}
