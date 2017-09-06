package com.rafapps.simplenotes;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.enrico.colorpicker.colorDialog;

public class SettingsActivity extends AppCompatActivity implements colorDialog.ColorSelectedListener {

    private ImageView img1;
    private ImageView img2;
    private ImageView img3;

    private SharedPreferences preferences;

    private
    @ColorInt
    int colourPrimary;
    private
    @ColorInt
    int colourFont;
    private
    @ColorInt
    int colourBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        colourPrimary = preferences.getInt("colourPrimary", 0);
        colourFont = preferences.getInt("colourFont", 0);
        colourBackground = preferences.getInt("colourBackground", 0);

        GradientDrawable gd1 = new GradientDrawable();
        gd1.setShape(GradientDrawable.OVAL);
        gd1.setColor(colourPrimary);
        gd1.setStroke(2, Color.BLACK);
        GradientDrawable gd2 = new GradientDrawable();
        gd2.setShape(GradientDrawable.OVAL);
        gd2.setStroke(2, Color.BLACK);
        gd2.setColor(colourFont);
        GradientDrawable gd3 = new GradientDrawable();
        gd3.setShape(GradientDrawable.OVAL);
        gd3.setStroke(2, Color.BLACK);
        gd3.setColor(colourBackground);

        img1 = (ImageView) findViewById(R.id.imageView1);
        img2 = (ImageView) findViewById(R.id.imageView2);
        img3 = (ImageView) findViewById(R.id.imageView3);

        img1.setImageDrawable(gd1);
        img2.setImageDrawable(gd2);
        img3.setImageDrawable(gd3);

        applyColours();
    }

    private void applyColours() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(preferences.getInt("colourPrimary", 0));
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription("Simple Notes",
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), preferences.getInt("colourPrimary", 0));
            setTaskDescription(tDesc);
            window.setNavigationBarColor(preferences.getInt("colourPrimary", 0));
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable((preferences.getInt("colourPrimary", 0))));
        }
        findViewById(R.id.constraintLayout).setBackgroundColor(preferences.getInt("colourBackground", 0));

        findViewById(R.id.button1).setBackgroundColor(preferences.getInt("colourPrimary", 0));
        findViewById(R.id.button2).setBackgroundColor(preferences.getInt("colourPrimary", 0));
        findViewById(R.id.button3).setBackgroundColor(preferences.getInt("colourPrimary", 0));
        findViewById(R.id.button4).setBackgroundColor(preferences.getInt("colourPrimary", 0));

    }

    public void showPicker1(View view) {
        colorDialog.setPickerColor(this, 1, colourPrimary);
        colorDialog.showColorPicker(this, 1);
    }

    public void showPicker2(View view) {
        colorDialog.setPickerColor(this, 2, colourFont);
        colorDialog.showColorPicker(this, 2);
    }

    public void showPicker3(View view) {
        colorDialog.setPickerColor(this, 3, colourBackground);
        colorDialog.showColorPicker(this, 3);
    }

    @Override
    public void onColorSelection(DialogFragment dialogFragment, @ColorInt int selectedColor) {

        int tag = Integer.valueOf(dialogFragment.getTag());
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(selectedColor);
        switch (tag) {

            case 1:
                img1.setImageDrawable(gd);
                if (Color.alpha(selectedColor) != 255) {
                    Toast t = Toast.makeText(getApplicationContext(), "App bar colour cannot have any transparency", Toast.LENGTH_LONG);
                    TextView tv = (TextView) t.getView().findViewById(android.R.id.message);
                    if( tv != null) {
                        tv.setGravity(Gravity.CENTER);
                    }
                    t.show();
                }
                colourPrimary = ColorUtils.setAlphaComponent(selectedColor, 255);
                gd.setColor(colourPrimary);
                break;

            case 2:
                img2.setImageDrawable(gd);
                colourFont = selectedColor;
                break;

            case 3:
                img3.setImageDrawable(gd);
                colourBackground = selectedColor;
                break;
        }
    }

    public void saveColours(View view) {

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("colourPrimary", colourPrimary);
        editor.putInt("colourFont", colourFont);
        editor.putInt("colourBackground", colourBackground);
        editor.apply();

        Intent nextScreen = new Intent(this, NotesList.class);
        nextScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(nextScreen);
        finish();

    }
}
