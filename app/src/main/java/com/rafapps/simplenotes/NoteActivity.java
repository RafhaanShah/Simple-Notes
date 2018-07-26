package com.rafapps.simplenotes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
    private AlertDialog dialog;

    private @ColorInt
    int colourPrimary;
    private @ColorInt
    int colourFont;
    private @ColorInt
    int colourBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
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
                note = sharedText;
                title = "";
            }
        } else { // If activity started from notes list
            title = intent.getStringExtra("noteTitle");
            if (title == null) {
                title = "";
                note = "";
                noteText.requestFocus();
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle("New Note");
            } else {
                titleText.setText(title);
                note = openFile(title);
                noteText.setText(note);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(title);
            }
        }

        Log.v("verbose", "OPENED TITLE " + title);
        Log.v("verbose", "OPENED NOTE " + note);

        getColours(PreferenceManager.getDefaultSharedPreferences(NoteActivity.this));
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
        HelperUtils.applyColours(NoteActivity.this, colourPrimary);

        // Set text field underline colour
        noteText.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));
        titleText.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));

        // Set actionbar and background colour
        findViewById(R.id.scrollView).setBackgroundColor(colourBackground);
        if (getSupportActionBar() != null)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colourPrimary));

        // Set font colours
        titleText.setTextColor(colourFont);
        noteText.setTextColor(colourFont);

        // Set hint colours
        titleText.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120));
        noteText.setHintTextColor(ColorUtils.setAlphaComponent(colourFont, 120));
    }

    private void getColours(SharedPreferences preferences) {
        colourPrimary = preferences.getInt("colourPrimary", Color.parseColor("#ffc107"));
        colourFont = preferences.getInt("colourFont", Color.parseColor("#000000"));
        colourBackground = preferences.getInt("colourBackground", Color.parseColor("#FFFFFF"));
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
                startActivity(Intent.createChooser(sendIntent, "Share to:"));
                return (true);

            case R.id.deleteButton:
                dialog = new AlertDialog.Builder(NoteActivity.this, R.style.AlertDialogTheme)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (fileExists(title)) {
                                    deleteFile(title + ".txt");
                                }
                                title = "";
                                note = "";
                                titleText.setText(title);
                                noteText.setText(note);
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
                    dialog.getWindow().getDecorView().setBackgroundColor(colourPrimary);
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
            Toast.makeText(NoteActivity.this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
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
                Toast.makeText(NoteActivity.this, "Exception: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content.trim();
    }
}
