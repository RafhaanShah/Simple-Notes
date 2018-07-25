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
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
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

public class NoteActivity extends AppCompatActivity {

    private EditText noteText;
    private EditText titleText;
    private String title;
    private String note;
    private SharedPreferences preferences;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        titleText = findViewById(R.id.titleText);
        noteText = findViewById(R.id.editText);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // If activity started from a share intent
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                noteText.setText(sharedText);
            }
        } else { // If activity started from notes list
            title = intent.getStringExtra("noteTitle");

            if (title == null) {
                title = "";
                note = "";
                noteText.requestFocus();
                getSupportActionBar().setTitle("New Note");
            } else {
                titleText.setText(title);
                note = openFile(title);
                noteText.setText(note);
                getSupportActionBar().setTitle(title);
            }
        }

        Log.v("verbose", "OPENED TITLE " + title);
        Log.v("verbose", "OPENED NOTE " + note);

        applyColours();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        note = noteText.getText().toString().trim();
        Log.v("verbose", "RESTARTED");
    }

    @Override
    public void onPause() {
        Log.v("verbose", "PAUSE");
        if (!isChangingConfigurations()) {
            Log.v("verbose", "NO CONFIG CHANGE");
            saveFile();
        }
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void applyColours() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(HelperUtils.darkenColor(preferences.getInt("colourPrimary", 0), 0.2));
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription("Simple Notes",
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), preferences.getInt("colourPrimary", 0));
            setTaskDescription(tDesc);
            noteText.setBackgroundTintList(ColorStateList.valueOf(preferences.getInt("colourPrimary", 0)));
            titleText.setBackgroundTintList(ColorStateList.valueOf(preferences.getInt("colourPrimary", 0)));
            window.setNavigationBarColor(preferences.getInt("colourPrimary", 0));
        }

        findViewById(R.id.toolbar).setBackgroundColor(preferences.getInt("colourPrimary", 0));
        findViewById(R.id.scrollView).setBackgroundColor(preferences.getInt("colourBackground", 0));

        titleText.setTextColor(preferences.getInt("colourFont", 0));
        noteText.setTextColor(preferences.getInt("colourFont", 0));

        titleText.setHintTextColor(ColorUtils.setAlphaComponent(preferences.getInt("colourFont", 0), 120));
        noteText.setHintTextColor(ColorUtils.setAlphaComponent(preferences.getInt("colourFont", 0), 120));
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
                noteText.setText(note);
                noteText.setSelection(noteText.getText().length());
                return (true);

            case R.id.shareButton:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, noteText.getText().toString());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                return (true);

            case R.id.deleteButton:
                dialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFile(title + ".txt");
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_delete_white_24dp))
                        .show();
                if (dialog.getWindow() != null) {
                    dialog.getWindow().getDecorView().setBackgroundColor(preferences.getInt("colourPrimary", 0));
                }
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    private void saveFile() {
        // Get current title and note
        String newTitle = titleText.getText().toString().trim().replace("/", " ");
        String newNote = noteText.getText().toString().trim();

        Log.v("verbose", "NEW TITLE " + newTitle);
        Log.v("verbose", "NEW TEXT " + newNote);

        // Check if title and note are empty
        if (TextUtils.isEmpty(newTitle) && TextUtils.isEmpty(newNote)) {
            Log.v("verbose", "EMPTY TITLE AND NOTE");
            return;
        }

        // Check if title and note are unchanged
        if (newTitle.equals(title) && newNote.equals(note)) {
            Log.v("verbose", "NOTHING CHANGED");
            return;
        }

        // Get file name to be saved if the title has changed or it is empty
        if (!title.equals(newTitle) || TextUtils.isEmpty(newTitle)) {
            newTitle = newFileName(newTitle);
            titleText.setText(newTitle);
            Log.v("verbose", "SAVED FILE " + newTitle);
        }

        // Save the file with the new file name and content
        writeFile(newTitle, newNote);

        // If the title is not empty and the file name has changed then delete the old file
        if (!TextUtils.isEmpty(title) && !newTitle.equals(title)) {
            Log.v("verbose", "DELETED FILE " + title);
            deleteFile(title + ".txt");
        }

        // Set the title to be the new saved title for when the home button is pressed
        title = newTitle;

    }

    private String newFileName(String name) {
        // If it is empty, give it a default title of "Note"
        if (TextUtils.isEmpty(name)) {
            name = "Note";
        }
        // If the name already exists, append a number to it
        if (fileExists(name)) {
            int i = 1;
            while (true) {
                if (!fileExists(name + " (" + i + ")") || title.equals(name + " (" + i + ")")) {
                    name = (name + " (" + i + ")");
                    break;
                }
                i++;
            }
        }
        return name;
    }

    private void writeFile(String fileName, String fileContent) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput(fileName + ".txt", 0));
            out.write(fileContent);
            out.close();
        } catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean fileExists(String fileName) {
        File file = getBaseContext().getFileStreamPath(fileName + ".txt");
        return file.exists();
    }

    private String openFile(String fileName) {
        String content = "";
        if (fileExists(fileName)) {
            try {
                InputStream in = openFileInput(fileName + ".txt");
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
        return content.trim();
    }

}
