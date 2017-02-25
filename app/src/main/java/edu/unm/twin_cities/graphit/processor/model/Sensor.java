package edu.unm.twin_cities.graphit.processor.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import edu.unm.twin_cities.graphit.processor.model.SensorType.SensorTypeID;
/**
 * Created by aman on 10/8/15.
 */

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class Sensor implements Parcelable {

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
     * The location at which the sensor stores its data.
     */
    private String sensorDataFilePath;

    /**
     * Type of device.
     */
    private SensorTypeID sensorTypeId;

    /**
     * Time stamp of creation for the entry.
     */
    private long createdAt;

    public Sensor(String deviceId, String sensorLabel, String sensorDataFilePath, SensorTypeID sensorTypeId, long createdAt) {
        this.deviceId = deviceId;
        this.sensorId = null;
        this.sensorLabel = sensorLabel;
        this.sensorDataFilePath = sensorDataFilePath;
        this.sensorTypeId = sensorTypeId;
        this.createdAt = createdAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(DatabaseHelper.Fields.DEVICE_ID.getFieldName(), this.deviceId);
        bundle.putString(DatabaseHelper.Fields.SENSOR_ID.getFieldName(), this.sensorId);
        bundle.putString(DatabaseHelper.Fields.SENSOR_LABEL.getFieldName(), this.sensorLabel);
        bundle.putString(DatabaseHelper.Fields.SENSOR_DESCRIPTION.getFieldName(), this.sensorDescription);
        bundle.putString(DatabaseHelper.Fields.SENSOR_DATA_FILE_PATH.getFieldName(), this.sensorDataFilePath);
        bundle.putString(DatabaseHelper.Fields.SENSOR_TYPE_ID.getFieldName(), this.sensorTypeId.getId());
        bundle.putLong(DatabaseHelper.Fields.CREATED_AT.getFieldName(), this.createdAt);

        out.writeBundle(bundle);
    }

    public static final Parcelable.Creator<Sensor> CREATOR = new Creator<Sensor>() {

        @Override
        public Sensor createFromParcel(Parcel in) {
            Bundle bundle = in.readBundle();

            String deviceId = bundle.getString(DatabaseHelper.Fields.DEVICE_ID.getFieldName());
            String sensorId = bundle.getString(DatabaseHelper.Fields.SENSOR_ID.getFieldName());
            String sensorLabel = bundle.getString(DatabaseHelper.Fields.SENSOR_LABEL.getFieldName());
            String sensorDescription = bundle.getString(DatabaseHelper.Fields.SENSOR_DESCRIPTION.getFieldName());
            String sensorDataFilePath = bundle.getString(DatabaseHelper.Fields.SENSOR_DATA_FILE_PATH.getFieldName());
            SensorTypeID sensorTypeId = SensorTypeID.fromString(
                    bundle.getString(DatabaseHelper.Fields.SENSOR_TYPE_ID.getFieldName()));
            Long createdAt = bundle.getLong(DatabaseHelper.Fields.CREATED_AT.getFieldName());

            return new Sensor(deviceId, sensorId, sensorLabel, sensorDescription,
                    sensorDataFilePath, sensorTypeId, createdAt);
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }

    };
}
