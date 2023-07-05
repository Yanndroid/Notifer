package de.dlyt.yanndroid.notifer.utils;

import android.graphics.Color;

import java.util.Arrays;

public class ColorUtil {

    public enum ColorFormat {
        HEX, RGB, HSV, INT
    }

    public static String convertColor(int color, ColorFormat format) {
        switch (format) {
            case HEX:
                return String.format("#%06X", (0xFFFFFF & color));
            case RGB:
                Color rgb = Color.valueOf(color);
                return Arrays.toString(new int[]{(int) (rgb.red() * 255), (int) (rgb.green() * 255), (int) (rgb.blue() * 255)});
            case HSV:
                float[] hsv = new float[3];
                Color.colorToHSV(color, hsv);
                return Arrays.toString(hsv);
            case INT:
                return String.valueOf(color);
        }
        return "";
    }

}
