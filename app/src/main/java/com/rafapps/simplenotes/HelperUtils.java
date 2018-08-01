package com.rafapps.simplenotes;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;

public class HelperUtils {

    public static String TEXT_FILE_EXTENSION = ".txt";
    public static String PREFERENCE_COLOUR_PRIMARY = "colourPrimary";
    public static String PREFERENCE_COLOUR_FONT = "colourFont";
    public static String PREFERENCE_COLOUR_BACKGROUND = "colourBackground";
    public static String PREFERENCE_COLOUR_NAVBAR = "colourNavbar";

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

    public static void applyColours(Activity activity, int colourPrimary, boolean colourNavbar) {
        //Get the activity window
        Window window = activity.getWindow();

        // Draw over the navigation bar
        if (colourNavbar)
            window.setNavigationBarColor(colourPrimary);

        // Colour the status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(HelperUtils.darkenColor(colourPrimary, 0.2));

        // Set task description, colour and icon for the app switcher
        activity.setTaskDescription(new ActivityManager.TaskDescription(activity.getString(R.string.app_name),
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_note), colourPrimary));
    }
}
