package com.rafapps.simplenotes;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;

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

        // Set task description, colour and icon for the app switcher (TaskDescription constructor deprecated in API 28)
        activity.setTaskDescription(new ActivityManager.TaskDescription(activity.getString(R.string.app_name),
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_note), colourPrimary));
    }

    public static ArrayList<File> getFiles(Context context) {
        File[] files = context.getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(HelperUtils.TEXT_FILE_EXTENSION);
            }
        });
        if(files == null) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(files));
    }

    public static boolean fileExists(Context context, String fileName) {
        File file = context.getFileStreamPath(fileName + HelperUtils.TEXT_FILE_EXTENSION);
        return file.exists();
    }

    public static void writeFile(Context context, String fileName, String fileContent) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(context.openFileOutput(fileName + HelperUtils.TEXT_FILE_EXTENSION, 0));
            out.write(fileContent);
            out.close();
        } catch (Throwable t) {
            Toast.makeText(context, context.getString(R.string.exception) + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static String readFile(Context context, String fileName) {
        String content = "";
        if (fileExists(context, fileName)) {
            try {
                InputStream in = context.openFileInput(fileName + HelperUtils.TEXT_FILE_EXTENSION);
                if (in != null) {
                    InputStreamReader tmp = new InputStreamReader(in);
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;
                    StringBuilder buf = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        buf.append(str).append("\n");
                    }
                    in.close();
                    content = buf.toString();
                }
            } catch (Exception e) {
                Toast.makeText(context, context.getString(R.string.exception) + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content.trim();
    }

}
