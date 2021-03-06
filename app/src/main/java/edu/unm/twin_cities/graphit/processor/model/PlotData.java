package edu.unm.twin_cities.graphit.processor.model;

import android.util.Pair;

import com.github.mikephil.charting.data.Entry;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by aman on 21/8/15.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
@NoArgsConstructor
public class PlotData {
    /**
     * The device from which data will be plotted.
     */
    private Map<Pair<String, String>, List<Entry>> data = Maps.newHashMap();

    List<String> xValues = Lists.newArrayList();

    public PlotData(Map<Pair<String, String>, List<Reading>> allDeviceReadings) {
        //Preparing the set of values for x axis due to a limitation from library.
        SortedSet<Long> sortedSet = Sets.newTreeSet();
        for (Map.Entry<Pair<String, String>, List<Reading>> entry : allDeviceReadings.entrySet()) {
            List<Reading> readings = entry.getValue();
            for (Reading reading : readings) {
                long xValue = reading.getTimestamp();  //time stamp of the reading.
                sortedSet.add(xValue);
            }
        }

        Map<Long, Integer> sortedIndexMap = Maps.newHashMap();
        int i = 0;
        for (Long value : sortedSet) {
            sortedIndexMap.put(value, i++);
            Date date=new Date(value);
            SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
            xValues.add(df2.format(date));
        }

        // preparing the data structure of it could be plotted.
        for (Map.Entry<Pair<String, String>, List<Reading>> entry : allDeviceReadings.entrySet()) {
            Pair<String, String> deviceId = entry.getKey();
            List<Reading> readings = entry.getValue();
            for (Reading reading : readings) {
                float yValue = reading.getReading();
                long xValue = reading.getTimestamp();  //time stamp of the reading.
                List<Entry> readingEntry = data.get(deviceId);
                if (readingEntry == null) {
                    readingEntry = Lists.newArrayList();
                    data.put(deviceId, readingEntry);
                }
                readingEntry.add(new Entry(yValue, sortedIndexMap.get(xValue)));
            }
        }
    }

    public int getDeviceCount() {
        return data.size();
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }
}
