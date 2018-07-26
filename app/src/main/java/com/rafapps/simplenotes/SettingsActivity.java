package com.rafapps.simplenotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enrico.colorpicker.colorDialog;

public class SettingsActivity extends AppCompatActivity implements colorDialog.ColorSelectedListener {

    private ImageView imageAccent;
    private ImageView imageFont;
    private ImageView imageBackground;
    private SharedPreferences preferences;

    private @ColorInt
    int colourPrimary;
    private @ColorInt
    int colourFont;
    private @ColorInt
    int colourBackground;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        imageAccent = findViewById(R.id.imageAccent);
        imageFont = findViewById(R.id.imageFont);
        imageBackground = findViewById(R.id.imageBackground);

        getColours(preferences);
        applyColours();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void applyColours() {
        //TODO: Make applying colours more efficient, remove SDK check, clean up themes
        HelperUtils.applyColours(SettingsActivity.this, colourPrimary);

        // Set action bar colour
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colourPrimary));
            getSupportActionBar().setTitle("Settings");
        }

        // Set background colour
        findViewById(R.id.constraintLayout).setBackgroundColor(colourBackground);

        // Set colour of indicator circles
        imageAccent.setColorFilter(colourPrimary);
        imageFont.setColorFilter(colourFont);
        imageBackground.setColorFilter(colourBackground);

        // Set colour of the background of the circles
        imageAccent.getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);
        imageFont.getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);
        imageBackground.getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);

        // Set font colours
        ((TextView) findViewById(R.id.textAccent)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.textFont)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.textBackground)).setTextColor(colourFont);

        // Set divider and button colours
        ((LinearLayout) findViewById(R.id.settingsLayout)).getDividerDrawable().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);
        findViewById(R.id.buttonApply).getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);
    }

    private void getColours(SharedPreferences preferences) {
        colourPrimary = preferences.getInt("colourPrimary", Color.parseColor("#ffc107"));
        colourFont = preferences.getInt("colourFont", Color.parseColor("#000000"));
        colourBackground = preferences.getInt("colourBackground", Color.parseColor("#FFFFFF"));
    }

    public void showPicker1(View view) {
        colorDialog.setPickerColor(SettingsActivity.this, 1, colourPrimary);
        colorDialog.showColorPicker(SettingsActivity.this, 1);
    }

    public void showPicker2(View view) {
        colorDialog.setPickerColor(SettingsActivity.this, 2, colourFont);
        colorDialog.showColorPicker(SettingsActivity.this, 2);
    }

    public void showPicker3(View view) {
        colorDialog.setPickerColor(SettingsActivity.this, 3, colourBackground);
        colorDialog.showColorPicker(SettingsActivity.this, 3);
    }

    @Override
    public void onColorSelection(DialogFragment dialogFragment, @ColorInt int selectedColor) {

        int tag = Integer.valueOf(dialogFragment.getTag());

        /*
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(selectedColor);
        */

        switch (tag) {
            case 1:
                //imageAccent.setImageDrawable(gd);
                imageAccent.setColorFilter(selectedColor);
                if (Color.alpha(selectedColor) != 255) {
                    Toast t = Toast.makeText(getApplicationContext(), "App bar colour cannot have any transparency", Toast.LENGTH_LONG);
                    TextView tv = t.getView().findViewById(android.R.id.message);
                    if (tv != null) {
                        tv.setGravity(Gravity.CENTER);
                    }
                    t.show();
                }
                colourPrimary = ColorUtils.setAlphaComponent(selectedColor, 255);
                //gd.setColor(colourPrimary);
                break;

            case 2:
                //imageFont.setImageDrawable(gd);
                imageFont.setColorFilter(selectedColor);
                colourFont = selectedColor;
                break;

            case 3:
                //imageBackground.setImageDrawable(gd);
                imageBackground.setColorFilter(selectedColor);
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

        Intent nextScreen = new Intent(SettingsActivity.this, NotesListActivity.class);
        nextScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(nextScreen);
        finish();
    }
}
