package edu.unm.twin_cities.graphit.util;

import android.graphics.Color;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Implements color handling for the lines in the graph plotted.
 */
public class LineColor {

    private int current = 0;

    private final List<RGB> colorList = Arrays.asList(
            new RGB(0, 255, 0),         //green
            new RGB(0, 0, 255),         //blue.
            new RGB(0, 0, 0),           //black
            new RGB(255, 0, 0),         //red
            new RGB(255, 255, 0),       //yellow
            new RGB(255, 0, 255),       //magenta
            new RGB(0, 255, 255)       //cyan
    );

    public int getNextColor() {
        if (current < colorList.size()) {
            RGB currentColor = colorList.get(current);
            return Color.argb(255, currentColor.getRed(),
                    currentColor.getBlue(), currentColor.getGreen());
        } else {
            Random rnd = new Random();
            return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        }
    }

    @Data
    @AllArgsConstructor(suppressConstructorProperties = true)
    private class RGB {
        int red;
        int green;
        int blue;
    }
}
