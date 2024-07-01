package de.dlyt.yanndroid.notifer.utils;

import android.graphics.Color;

import java.util.Arrays;

public class ColorUtil {

    public static String toRGB(int color) {
        Color rgb = Color.valueOf(color);
        return Arrays.toString(new int[]{(int) (rgb.red() * 255), (int) (rgb.green() * 255), (int) (rgb.blue() * 255)});
    }

    public static String toHSV(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return Arrays.toString(hsv);
    }

    public static String toHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

}
