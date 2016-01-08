package edu.unm.twin_cities.graphit.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by aman on 30/11/15.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class Device {

    /**
     * Identifier for the device. Bluetooth MAC address would be used..
     */
    private String deviceId;

    /**
     * Label for the device.
     */
    private String deviceLabel;
}
