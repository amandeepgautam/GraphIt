package edu.unm.twin_cities.graphit.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by aman on 18/8/15.
 */

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class DeviceType {
    /**
     * Device Type
     */
    private String deviceTypeLabel;

    /**
     * Identifier for device type.
     */
    private DeviceTypeID deviceTypeId;

    /**
     * Any useful description for the device.
     */
    private String deviceTypeDescription;

    /**
     * This class is used for grouping the devices together for graph plotting.
     * Assume device type is SOIL_SENSOR, then it would make more sense to make comparitive
     * study between such sensors.
     */
    public enum DeviceTypeID {
        /**
         * All devices of this type would be grouped together for plotting.
         */
        DEFAULT;
    }
}
