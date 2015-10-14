package edu.unm.twin_cities.graphit.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import edu.unm.twin_cities.graphit.processor.model.DeviceType.DeviceTypeID;
/**
 * Created by aman on 10/8/15.
 */

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class DeviceInfo {
    /**
     * Unique identifier for the device.
     */
    private String deviceId;

    /**
     * Readable name for the device.
     */
    private String deviceLabel;

    /**
     * Description about the device.
     */
    private String deviceDescription;

    /**
     * Type of device.
     */
    private DeviceTypeID deviceTypeId;

    /**
     * Time stamp of creation for the entry.
     */
    private long createdAt;

    public DeviceInfo(String deviceLabel, DeviceTypeID deviceTypeId, long createdAt) {
        this.deviceLabel = deviceLabel;
        this.deviceTypeId = deviceTypeId;
        this.createdAt = createdAt;
    }
}
