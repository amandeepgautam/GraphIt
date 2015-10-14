package edu.unm.twin_cities.graphit.util;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import edu.unm.twin_cities.graphit.processor.model.SensorReading;

/**
 * Created by aman on 18/9/15.
 */
public interface FileParser {
    List<SensorReading<Long, Float>> parse(byte [] input) throws IOException;
}
