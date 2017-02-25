package edu.unm.twin_cities.graphit.processor.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.base.Objects;

import edu.unm.twin_cities.graphit.processor.DatabaseHelper;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by aman on 30/11/15.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class Device implements Parcelable {

    /**
     * Identifier for the device. Bluetooth MAC address would be used. See {@link #hashCode()} and
     * {@link #equals(Object)}
     */
    private String deviceId;

    /**
     * Label for the device.
     */
    private String deviceLabel;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(DatabaseHelper.Fields.DEVICE_ID.getFieldName(), deviceId);
        bundle.putString(DatabaseHelper.Fields.DEVICE_LABEL.getFieldName(), deviceLabel);

        out.writeBundle(bundle);
    }

    public static final Parcelable.Creator<Device> CREATOR = new Creator<Device>() {

        @Override
        public Device createFromParcel(Parcel in) {
            Bundle bundle = in.readBundle();

            String deviceId = bundle.getString(DatabaseHelper.Fields.DEVICE_ID.getFieldName());
            String deviceLabel = bundle.getString(DatabaseHelper.Fields.DEVICE_LABEL.getFieldName());
            return new Device(deviceId, deviceLabel);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }

    };

    @Override
    public int hashCode() {
        return Objects.hashCode(deviceId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Device){
            final Device other = (Device) obj;
            return Objects.equal(deviceId, other.deviceId);
        } else{
            return false;
        }
    }
}
