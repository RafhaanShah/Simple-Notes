package com.rafapps.simplenotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    private RecyclerView recyclerView;
    private TextView emptyText;
    private NotesListAdapter notesListAdapter;
    private FloatingActionButton fab;

    private @ColorInt
    int colourPrimary;
    private @ColorInt
    int colourFont;
    private @ColorInt
    int colourBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getFiles();

        fab = findViewById(R.id.fab);
        emptyText = findViewById(R.id.emptyText);
        recyclerView = findViewById(R.id.recyclerView);
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

        getColours(PreferenceManager.getDefaultSharedPreferences(NotesListActivity.this));
        applyColours();
    }

    private void applyColours() {
        HelperUtils.applyColours(NotesListActivity.this, colourPrimary);
        findViewById(R.id.toolbar).setBackgroundColor(colourPrimary);
        findViewById(R.id.constraintLayout).setBackgroundColor(colourBackground);
        emptyText.setTextColor(colourFont);
        fab.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));
    }

    private void getColours(SharedPreferences preferences) {
        colourPrimary = preferences.getInt("colourPrimary", Color.parseColor("#ffc107"));
        colourFont = preferences.getInt("colourFont", Color.parseColor("#000000"));
        colourBackground = preferences.getInt("colourBackground", Color.parseColor("#FFFFFF"));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Hide keyboard
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        // Close search
        SearchView searchView = findViewById(R.id.searchButton);
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

        findViewById(R.id.coordinatorLayout).clearFocus();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.searchButton);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(NotesListActivity.this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settingsButton:
                Intent settingsScreen = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsScreen);
                return (true);
            case R.id.searchButton:
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

        /*
        recyclerView.setLayoutManager(new LinearLayoutManager(NotesListActivity.this));
        notesListAdapter = new NotesListAdapter(filteredList.toArray(new String[0]), filteredList2.toArray(new Long[0]));
        recyclerView.setAdapter(notesListAdapter);
        notesListAdapter.notifyDataSetChanged();
        */
        notesListAdapter.updateDataList(filteredList.toArray(new String[0]), filteredList2.toArray(new Long[0]));

        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public void newNote(View view) {
        Intent nextScreen = new Intent(getApplicationContext(), NoteActivity.class);
        startActivity(nextScreen);
    }

    private void getFiles() {
        File[] files = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                Log.v("verbose", name);
                return name.toLowerCase().endsWith(".txt");
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
        SearchView searchView = findViewById(R.id.searchButton);
        if (!searchView.isIconified()) {
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }
}
