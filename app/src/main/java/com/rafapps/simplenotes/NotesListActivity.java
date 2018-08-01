package com.rafapps.simplenotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class NotesListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private String[] filesList;
    private Long[] datesList;
    private TextView emptyText;
    private NotesListAdapter notesListAdapter;
    private FloatingActionButton fab;
    private boolean colourNavbar;

    private @ColorInt
    int colourPrimary;
    private @ColorInt
    int colourFont;
    private @ColorInt
    int colourBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        getFiles();

        fab = findViewById(R.id.fab);
        emptyText = findViewById(R.id.tv_empty);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotesListActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        notesListAdapter = new NotesListAdapter(filesList, datesList);
        recyclerView.setAdapter(notesListAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        getSettings(PreferenceManager.getDefaultSharedPreferences(NotesListActivity.this));
        applySettings();
    }

    private void applySettings() {
        HelperUtils.applyColours(NotesListActivity.this, colourPrimary, colourNavbar);
        findViewById(R.id.layout_coordinator).setBackgroundColor(colourBackground);
        emptyText.setTextColor(colourFont);
        fab.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));
        if (getSupportActionBar() != null)
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(colourPrimary));
    }

    private void getSettings(SharedPreferences preferences) {
        colourPrimary = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_PRIMARY, ContextCompat.getColor(NotesListActivity.this, R.color.colorPrimary));
        colourFont = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK);
        colourBackground = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_BACKGROUND, Color.WHITE);
        colourNavbar = preferences.getBoolean(HelperUtils.PREFERENCE_COLOUR_NAVBAR, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Hide keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        // Close search
        SearchView searchView = findViewById(R.id.btn_search);
        if (searchView != null) {
            if (!searchView.isIconified()) {
                searchView.onActionViewCollapsed();
            }
        }

        // Update the list
        getFiles();
        notesListAdapter.updateDataList(filesList, datesList);
        //recyclerView.invalidate();

        // If the list is empty, show message
        if (notesListAdapter.getItemCount() == 0) {
            emptyText.setVisibility(View.VISIBLE);
        } else if (emptyText.getVisibility() == View.VISIBLE) {
            emptyText.setVisibility(View.GONE);
        }

        findViewById(R.id.layout_coordinator).clearFocus();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.btn_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(NotesListActivity.this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_settings:
                startActivity(new Intent(NotesListActivity.this, SettingsActivity.class));
                return (true);
            case R.id.btn_search:
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }


    @Override
    public boolean onQueryTextChange(String query) {
        query = query.toLowerCase();
        final List<String> filteredList = new ArrayList<>();
        final List<Long> filteredList2 = new ArrayList<>();

        for (int i = 0; i < filesList.length; i++) {

            final String text = filesList[i].toLowerCase();
            if (text.contains(query)) {
                filteredList.add(filesList[i]);
                filteredList2.add(datesList[i]);
            }
        }

        notesListAdapter.updateDataList(filteredList.toArray(new String[0]), filteredList2.toArray(new Long[0]));

        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public void newNote(View view) {
        startActivity(NoteActivity.getStartIntent(NotesListActivity.this, ""));
    }

    private void getFiles() {
        File[] files = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(HelperUtils.TEXT_FILE_EXTENSION);
            }
        });

        filesList = new String[files.length];
        datesList = new Long[files.length];

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });

        for (int i = 0; i < files.length; i++) {
            filesList[i] = (files[i].getName().substring(0, files[i].getName().length() - 4));
            datesList[i] = files[i].lastModified();
        }
    }

    @Override
    public void onBackPressed() {
        SearchView searchView = findViewById(R.id.btn_search);
        if (!searchView.isIconified()) {
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }
}
