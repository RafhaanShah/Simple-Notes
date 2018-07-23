package com.rafapps.simplenotes;

import android.graphics.Color;

public class HelperUtils {

    public static int darkenColor(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darken(red, fraction);
        green = darken(green, fraction);
        blue = darken(blue, fraction);
        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    private static int darken(int color, double fraction) {
        return (int) Math.max(color - (color * fraction), 0);
    }

}
