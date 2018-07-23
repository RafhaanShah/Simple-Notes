package com.rafapps.simplenotes;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotesList extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private String[] filesList;
    private String[] datesList;
    private RecyclerView recyclerView;
    private NotesListAdapter notesListAdapter;
    private FloatingActionButton fab;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        getFiles();

        fab = (FloatingActionButton) findViewById(R.id.fab);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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

        applyColours();
    }

    private void applyColours() {
        checkColours();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(preferences.getInt("colourPrimary", 0));
            ActivityManager.TaskDescription tDesc = new ActivityManager.TaskDescription("Simple Notes",
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), preferences.getInt("colourPrimary", 0));
            setTaskDescription(tDesc);
            window.setNavigationBarColor(preferences.getInt("colourPrimary", 0));
        }

        findViewById(R.id.toolbar).setBackgroundColor(preferences.getInt("colourPrimary", 0));
        findViewById(R.id.constraintLayout).setBackgroundColor(preferences.getInt("colourBackground", 0));

        fab.setBackgroundTintList(ColorStateList.valueOf(preferences.getInt("colourPrimary", 0)));

    }

    private void checkColours() {
        if (preferences.getInt("colourPrimary", 0) == 0) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("colourPrimary", Color.parseColor("#ffc107"));
            editor.putInt("colourFont", Color.parseColor("#000000"));
            editor.putInt("colourBackground", Color.parseColor("#FFFFFF"));
            editor.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );


        SearchView searchView = (SearchView) findViewById(R.id.searchButton);
        if (searchView != null) {
            if (!searchView.isIconified()) {
                searchView.onActionViewCollapsed();
            }
        }

        getFiles();
        notesListAdapter.updateDataList(filesList, datesList);
        recyclerView.invalidate();

        findViewById(R.id.coordinatorLayout).clearFocus();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes_list, menu);

        final MenuItem searchItem = menu.findItem(R.id.searchButton);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
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
        final List<String> filteredList2 = new ArrayList<>();

        for (int i = 0; i < filesList.length; i++) {

            final String text = filesList[i].toLowerCase();
            if (text.contains(query)) {
                filteredList.add(filesList[i]);
                filteredList2.add(datesList[i]);
            }
        }


        recyclerView.setLayoutManager(new LinearLayoutManager(NotesList.this));
        notesListAdapter = new NotesListAdapter(filteredList.toArray(new String[0]), filteredList2.toArray(new String[0]));
        recyclerView.setAdapter(notesListAdapter);
        notesListAdapter.notifyDataSetChanged();

        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    public void newNote(View view) {
        Intent nextScreen = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(nextScreen);
    }

    private void getFiles() {
        File[] files = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".txt");
            }
        });

        filesList = new String[files.length];
        datesList = new String[files.length];

        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        for (int i = 0; i < files.length; i++) {
            filesList[i] = (files[i].getName().substring(0, files[i].getName().length() - 4));
            Date d = new Date(files[i].lastModified());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm" + "\n" + "dd/MM/yyyy", Locale.getDefault());
            datesList[i] = sdf.format(d);
        }
    }

    @Override
    public void onBackPressed() {
        SearchView searchView = (SearchView) findViewById(R.id.searchButton);

        if (!searchView.isIconified()) {
            searchView.onActionViewCollapsed();
        } else {
            super.onBackPressed();
        }
    }
}
