package edu.unm.twin_cities.graphit.processor.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import edu.unm.twin_cities.graphit.util.Measurement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * Created by aman on 18/8/15.
 */

@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class SensorType implements Parcelable {
    /**
     * DeviceSensorMap Type
     */
    private String sensorTypeLabel;

    /**
     * Identifier for device type.
     */
    private SensorTypeID sensorTypeId;

    /**
     * Any useful description for the device.
     */
    private String sensorTypeDescription;

    private MeasurementUnit measurementUnit;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();

        bundle.putString(DatabaseHelper.Fields.SENSOR_TYPE_LABEL.getFieldName(), sensorTypeLabel);
        bundle.putString(DatabaseHelper.Fields.SENSOR_DESCRIPTION.getFieldName(), sensorTypeDescription);

        bundle.putInt(DatabaseHelper.Fields.SENSOR_TYPE_ID.getFieldName(), sensorTypeId.ordinal());
        bundle.putInt(DatabaseHelper.Fields.MEASUREMENT_UNIT.getFieldName(), measurementUnit.ordinal());

        out.writeBundle(bundle);
    }

    public static final Parcelable.Creator<SensorType> CREATOR = new Creator<SensorType>() {
        @Override
        public SensorType createFromParcel(Parcel in) {
            Bundle bundle = in.readBundle();

            String sensorTypeLabel = bundle
                    .getString(DatabaseHelper.Fields.SENSOR_TYPE_LABEL.getFieldName());
            String sensorDescription = bundle
                    .getString(DatabaseHelper.Fields.SENSOR_DESCRIPTION.getFieldName());

            MeasurementUnit measurementUnit = MeasurementUnit.values()[bundle
                    .getInt(DatabaseHelper.Fields.MEASUREMENT_UNIT.getFieldName())];
            SensorTypeID sensorTypeID = SensorTypeID.values()[bundle.
                    getInt(DatabaseHelper.Fields.SENSOR_TYPE_ID.getFieldName())];

            return new SensorType(sensorTypeLabel, sensorTypeID,
                    sensorDescription, measurementUnit);
        }

        @Override
        public SensorType[] newArray(int size) {
            return new SensorType[size];
        }
    };

        /**
     * This class is used for grouping the devices together for graph plotting.
     * Assume device type is SOIL_SENSOR, then it would make more sense to make comparitive
     * study between such sensors.
     */
    public enum SensorTypeID {
        /**
         * All devices of this type would be grouped together for plotting.
         */
        DEFAULT ("DEFAULT"),
        SOIL_MOISTURE("SOIL_MOISTURE");

        @Getter
        private final String id;

        private SensorTypeID(final String id) {
            this.id = id;
        }

        public static SensorTypeID fromString(String enumStr){
            for(SensorTypeID sensorTypeID : values()){
                if(sensorTypeID.getId().equals(enumStr)){
                    return sensorTypeID;
                }
            }
            return null;
        }
    }

    public enum MeasurementUnit {
        NONE(""),
        TEMPRATURE_MEASUREMENT_UNIT("celsuis"),
        SOIL_MOISTURE_MEASUREMENT_UNIT("m / kg");

        @Getter
        private final String unit;

        private MeasurementUnit(final String unit) {
            this.unit = unit;
        }

        public static MeasurementUnit fromString(String enumStr){
            for(MeasurementUnit measurementUnit : values()){
                if(measurementUnit.getUnit().equals(enumStr)){
                    return measurementUnit;
                }
            }
            return null;
        }
    }
}
