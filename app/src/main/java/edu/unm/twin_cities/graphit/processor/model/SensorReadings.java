package edu.unm.twin_cities.graphit.processor.model;

import android.util.Pair;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.Serializable;
import java.nio.DoubleBuffer;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by aman on 18/9/15.
 */
@Data
public class SensorReadings implements SensorReading<Long, Float>, Serializable {

    private static final long serialVersionUID = -2518143671167959230L;

    /**
     * location of the sensor, while it took the reading.
     */
    String location;

    String sensorName;

    public SensorReadings(String location, String sensorName) {
        this.location = location;
        this.sensorName = sensorName;
        timeStampReadingTuple = Lists.newArrayList();
    }

    /**
     * Map of Sensor name and the readings it took
     */
    List<Pair<Long, Float>> timeStampReadingTuple;

    public void addReading(Pair<Long, Float> pair) {
        timeStampReadingTuple.add(pair);
    }

    public List<Pair<Long, Float>> getReadings() {
        return timeStampReadingTuple;
    }

    @Override
    public String getSensorIdentifier() {
        return sensorName + "@ " + location;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        location = (String) in.readObject();
        sensorName = (String) in.readObject();
        int size = (int) in.readObject();
        timeStampReadingTuple = Lists.newArrayListWithCapacity(size);
        for(int i=0; i<size; ++i) {
            long timeStamp = (long) in.readObject();
            float reading = (float) in.readObject();
            timeStampReadingTuple.add(new Pair<Long, Float>(timeStamp, reading));
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(location);
        out.writeObject(sensorName);
        out.writeObject(timeStampReadingTuple.size());
        for(Pair<Long, Float> pair : timeStampReadingTuple) {
            out.writeObject(pair.first);
            out.writeObject(pair.second);
        }
    }
}
