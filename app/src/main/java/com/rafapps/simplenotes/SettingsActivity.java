package com.rafapps.simplenotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enrico.colorpicker.colorDialog;

public class SettingsActivity extends AppCompatActivity implements colorDialog.ColorSelectedListener {

    private ImageView imageAccent;
    private ImageView imageFont;
    private ImageView imageBackground;
    private CheckBox navBox;
    private boolean colourNavbar;
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
        imageAccent = findViewById(R.id.image_accent);
        imageFont = findViewById(R.id.image_font);
        imageBackground = findViewById(R.id.image_background);

        getSettings(preferences);
        applySettings();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void applySettings() {
        HelperUtils.applyColours(SettingsActivity.this, colourPrimary, colourNavbar);

        // Set action bar colour
        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colourPrimary));
            getSupportActionBar().setTitle(getString(R.string.settings));
        }

        // Set background colour
        findViewById(R.id.layout_constraint).setBackgroundColor(colourBackground);

        // Set colour of indicator circles
        imageAccent.setColorFilter(colourPrimary);
        imageFont.setColorFilter(colourFont);
        imageBackground.setColorFilter(colourBackground);

        // Set colour of the background of the circles
        imageAccent.getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);
        imageFont.getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);
        imageBackground.getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);

        // Set font colours
        ((TextView) findViewById(R.id.tv_accent)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.tv_font)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.tv_background)).setTextColor(colourFont);
        ((TextView) findViewById(R.id.tv_navigationbar)).setTextColor(colourFont);

        // Set divider and button colours
        ((LinearLayout) findViewById(R.id.settingsLayout)).getDividerDrawable().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);
        findViewById(R.id.btn_apply).getBackground().setColorFilter(colourPrimary, PorterDuff.Mode.SRC_ATOP);

        // Set switch setting
        navBox = findViewById(R.id.checkbox_navigationbar);
        navBox.setChecked(colourNavbar);
        CompoundButtonCompat.setButtonTintList(navBox, ColorStateList.valueOf(colourPrimary));
    }

    private void getSettings(SharedPreferences preferences) {
        colourPrimary = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_PRIMARY, ContextCompat.getColor(SettingsActivity.this, R.color.colorPrimary));
        colourFont = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK);
        colourBackground = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_BACKGROUND, Color.WHITE);
        colourNavbar = preferences.getBoolean(HelperUtils.PREFERENCE_COLOUR_NAVBAR, false);
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

    public void toggleCheckBox(View view) {
        navBox.toggle();
    }

    @Override
    public void onColorSelection(DialogFragment dialogFragment, @ColorInt int selectedColor) {

        int tag = Integer.valueOf(dialogFragment.getTag());

        switch (tag) {
            case 1:
                imageAccent.setColorFilter(selectedColor);
                if (Color.alpha(selectedColor) != 255) {
                    Toast t = Toast.makeText(getApplicationContext(), getString(R.string.error_appbar_colour), Toast.LENGTH_LONG);
                    TextView tv = t.getView().findViewById(android.R.id.message);
                    if (tv != null) {
                        tv.setGravity(Gravity.CENTER);
                    }
                    t.show();
                }
                colourPrimary = ColorUtils.setAlphaComponent(selectedColor, 255);
                break;

            case 2:
                imageFont.setColorFilter(selectedColor);
                colourFont = selectedColor;
                break;

            case 3:
                imageBackground.setColorFilter(selectedColor);
                colourBackground = selectedColor;
                break;
        }
    }

    public void saveSettings(View view) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(HelperUtils.PREFERENCE_COLOUR_PRIMARY, colourPrimary);
        editor.putInt(HelperUtils.PREFERENCE_COLOUR_FONT, colourFont);
        editor.putInt(HelperUtils.PREFERENCE_COLOUR_BACKGROUND, colourBackground);
        editor.putBoolean(HelperUtils.PREFERENCE_COLOUR_NAVBAR, navBox.isChecked());
        editor.apply();

        startActivity(new Intent(SettingsActivity.this, NotesListActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }
}
