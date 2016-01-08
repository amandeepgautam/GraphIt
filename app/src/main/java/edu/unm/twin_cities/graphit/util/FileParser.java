package edu.unm.twin_cities.graphit.util;

import java.io.IOException;
import java.util.List;

/**
 * Created by aman on 18/9/15.
 */
public interface FileParser {
    List<Measurement<Long, Float>> parse(byte [] input) throws IOException;
}
