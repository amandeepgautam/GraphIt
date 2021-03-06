package edu.unm.twin_cities.graphit.util;

import android.util.Log;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

/**
 * Created by aman on 18/9/15.
 */
public class FileParserImpl implements FileParser {

    private static final String TAG = FileParserImpl.class.getSimpleName();

    public List<Measurement<Long, Float>> parseAndPrepareReadingRecords(byte[] input, String deviceId) throws IOException {

        Map<Pair<String, String>, Measurement<Long, Float>> sensorData = Maps.newHashMap();
        if (input.length != 0) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(input);

            /**
             * The CSV is assumed to be of following format.
             * Location/TimeStamp/Sensor1 Name/Sensor2  Name/....
             * Loc     / stamp  /   Val       /   Val       /....
             */

            CSVParser csvParser = new CSVParser(new InputStreamReader(inputStream), CSVFormat.DEFAULT.withHeader());
            Set<String> keys = csvParser.getHeaderMap().keySet();

            //validate the columns and extract the sensor names.
            if (keys.contains(MandatoryHeaderFields.TIMESTAMP.toString())) {
                keys.remove(MandatoryHeaderFields.TIMESTAMP.toString());
            } else {
                //TODO: see the application do not crash with this exception.
                throw new IllegalStateException("Invalid header format.");
            }

            try {
                for (CSVRecord record : csvParser) {
                    String timeStamp = record.get(MandatoryHeaderFields.TIMESTAMP);
                    SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = f.parse(timeStamp);
                    long milliseconds = date.getTime();
                    for (String sensorId : keys) {
                        Pair<String, String> pair = new Pair<>(deviceId, sensorId);
                        Measurement<Long, Float> measurement = sensorData.get(pair);
                        if (measurement == null) {
                            measurement = new MeasurementImpl(deviceId, sensorId);
                            sensorData.put(pair, measurement);
                        }
                        String reading = record.get(sensorId);
                        measurement.addMeasurement(new Pair<>(milliseconds, Float.valueOf(reading)));
                    }
                }
            } catch (ParseException parseException) {
                throw new IllegalStateException("Unknown format for date.");
            } catch (Exception e) {
                Log.e(TAG, "Unable to parse the file", e);
            }
        }
        //TODO: check how .values() behave in case the map is empty.
        return Lists.newArrayList(sensorData.values());
    }

    private enum MandatoryHeaderFields {
        TIMESTAMP("TimeStamp");

        @Getter
        private String header;

        MandatoryHeaderFields(String header) {
            this.header = header;
        }

        @Override
        public String toString() {
            return header;
        }
    }
}
