package com.rafapps.simplenotes;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private EditText editText;
    private EditText title;
    private String oldTitle;
    private boolean textChanged;
    private boolean titleChanged;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String noteTitle = intent.getStringExtra("noteTitle");

        title = (EditText) findViewById(R.id.title);
        editText = (EditText) findViewById(R.id.editText);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                editText.setText(sharedText);
            }
        } else {
            title.setText(noteTitle);
            editText.setText(openFile(noteTitle));
        }

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                textChanged = true;
            }
        });

        title.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                titleChanged = true;
            }
        });

        if (noteTitle == null) {
            oldTitle = "";
            editText.requestFocus();
        } else {
            oldTitle = noteTitle;
        }
        applyColours();
    }

    private void applyColours() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(preferences.getInt("colourPrimary", 0));
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription("Simple Notes",
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), preferences.getInt("colourPrimary", 0));
            setTaskDescription(tDesc);
            editText.setBackgroundTintList(ColorStateList.valueOf(preferences.getInt("colourPrimary", 0)));
            title.setBackgroundTintList(ColorStateList.valueOf(preferences.getInt("colourPrimary", 0)));
            window.setNavigationBarColor(preferences.getInt("colourPrimary", 0));
        }

        findViewById(R.id.toolbar).setBackgroundColor(preferences.getInt("colourPrimary", 0));
        findViewById(R.id.scrollView).setBackgroundColor(preferences.getInt("colourBackground", 0));

        title.setTextColor(preferences.getInt("colourFont", 0));
        editText.setTextColor(preferences.getInt("colourFont", 0));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undoButton:
                editText.setText(openFile(oldTitle));
                editText.setSelection(editText.getText().length());
                return (true);

            case R.id.shareButton:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return (true);

            case R.id.deleteButton:
                AlertDialog ad = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (titleChanged) {
                                    deleteFile(oldTitle + ".txt");
                                } else {
                                    deleteFile(title.getText().toString() + ".txt");
                                }
                                textChanged = false;
                                titleChanged = false;
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete_white_24dp))
                        .show();
                if (ad.getWindow() != null) {
                    ad.getWindow().getDecorView().setBackgroundColor(preferences.getInt("colourPrimary", 0));
                }
                ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Window window = ((AlertDialog) dialog).getWindow();
                        if (window != null)
                            window.getDecorView().setBackgroundResource(R.color.red);
                    }
                });
                ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    private void saveFile() {
        String localTitle = title.getText().toString();
        String theText = editText.getText().toString();

        if (localTitle.isEmpty() && theText.isEmpty()) {
            return;
        }

        if (localTitle.isEmpty()) {
            if (oldTitle.equals("")) {
                localTitle = "Note";
                titleChanged = true;
            } else {
                localTitle = oldTitle;
                titleChanged = false;
            }
        }

        if (oldTitle.equals(localTitle)) {
            titleChanged = false;
        }

        if (textChanged || titleChanged) {
            if (titleChanged) {
                if (fileExists(localTitle)) {
                    int i = 1;
                    while (true) {
                        if (!fileExists(localTitle + " (" + i + ")")) {
                            localTitle = (localTitle + " (" + i + ")");
                            break;
                        }
                        i++;
                    }
                }
                deleteFile(oldTitle + ".txt");
            }
            writeFile(localTitle);
            oldTitle = localTitle;
        }
    }

    private void writeFile(String fileName) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(fileName + ".txt", 0));
            out.write(editText.getText().toString());
            out.close();
        } catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }

        titleChanged = false;
        textChanged = false;
    }

    private boolean fileExists(String fileName) {
        File file = getBaseContext().getFileStreamPath(fileName + ".txt");
        return file.exists();
    }

    private String openFile(String file) {
        String content = "";
        if (fileExists(file)) {
            try {
                InputStream in = openFileInput(file + ".txt");
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
                Toast.makeText(this, "Exception: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content;
    }

    @Override
    public void onPause() {
        saveFile();
        super.onPause();
    }
}
