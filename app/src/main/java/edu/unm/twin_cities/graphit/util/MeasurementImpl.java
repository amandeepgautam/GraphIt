package edu.unm.twin_cities.graphit.util;

import android.util.Pair;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * Created by aman on 18/9/15.
 */
@Data
public class MeasurementImpl implements Measurement<Long, Float>, Serializable {

    private static final long serialVersionUID = -2518143671167959230L;

    /**
     * Location of the sensor, while it took the reading.
     */
    private String deviceId;

    private String sensorId;

    /**
     * Map of Sensor name and the readings it took
     */
    private List<Pair<Long, Float>> timeStampReadingTuple;

    public MeasurementImpl(String deviceId, String sensorName) {
        this.deviceId = deviceId;
        this.sensorId = sensorName;
        timeStampReadingTuple = Lists.newArrayList();
    }

    public void addMeasurement(Pair<Long, Float> measurement) {
        timeStampReadingTuple.add(measurement);
    }

    public List<Pair<Long, Float>> getMeasurement() {
        return timeStampReadingTuple;
    }

    @Override
    public String getSensorIdentifier() {
        return sensorId + "(" + deviceId + ")";
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        deviceId = (String) in.readObject();
        sensorId = (String) in.readObject();
        int size = (int) in.readObject();
        timeStampReadingTuple = Lists.newArrayListWithCapacity(size);
        for(int i=0; i<size; ++i) {
            long timeStamp = (long) in.readObject();
            float reading = (float) in.readObject();
            timeStampReadingTuple.add(new Pair<Long, Float>(timeStamp, reading));
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(deviceId);
        out.writeObject(sensorId);
        out.writeObject(timeStampReadingTuple.size());
        for(Pair<Long, Float> pair : timeStampReadingTuple) {
            out.writeObject(pair.first);
            out.writeObject(pair.second);
        }
    }
}
