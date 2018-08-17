package com.rafapps.simplenotes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
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

public class NotesListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static String PREFERENCE_SORT_ALPHABETICAL = "sortAlphabetical";

    private TextView emptyText;
    private NotesListAdapter notesListAdapter;
    private FloatingActionButton fab;
    private boolean colourNavbar;
    private boolean sortAlphabetical;
    private SharedPreferences preferences;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(NotesListActivity.this);
        getSettings(preferences);

        fab = findViewById(R.id.fab);
        emptyText = findViewById(R.id.tv_empty);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotesListActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration itemDecorator = new DividerItemDecoration(NotesListActivity.this, DividerItemDecoration.VERTICAL);
        Drawable divider = getDrawable(R.drawable.divider);
        if (divider != null) {
            divider.setTint(colourPrimary);
            itemDecorator.setDrawable(divider);
            recyclerView.addItemDecoration(itemDecorator);
        }

        notesListAdapter = new NotesListAdapter();
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

        setItemTouchHelper(recyclerView);
        applySettings();
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
        notesListAdapter.updateList(getFiles(), sortAlphabetical);

        // If the list is empty, show message
        if (notesListAdapter.getItemCount() == 0) {
            emptyText.setVisibility(View.VISIBLE);
        } else if (emptyText.getVisibility() == View.VISIBLE) {
            emptyText.setVisibility(View.GONE);
        }

        findViewById(R.id.layout_coordinator).clearFocus();
    }

    @Override
    public void onPause() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREFERENCE_SORT_ALPHABETICAL, sortAlphabetical);
        editor.apply();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notes_list, menu);
        final MenuItem searchItem = menu.findItem(R.id.btn_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(NotesListActivity.this);
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        if (sortAlphabetical)
            menu.findItem(R.id.btn_sort).setIcon(R.drawable.ic_sort_alphabetical);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_settings:
                startActivity(new Intent(NotesListActivity.this, SettingsActivity.class));
                return (true);
            case R.id.btn_sort:
                if (sortAlphabetical) {
                    item.setIcon(R.drawable.alphabetical_to_numerical);
                    sortAlphabetical = false;
                } else {
                    item.setIcon(R.drawable.numeric_to_alphabetical);
                    sortAlphabetical = true;
                }
                notesListAdapter.sortList(sortAlphabetical);
                Drawable drawable = item.getIcon();
                if (drawable instanceof Animatable)
                    ((Animatable) drawable).start();
            case R.id.btn_search:
                return (true);
        }
        return (super.onOptionsItemSelected(item));
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

    @Override
    public boolean onQueryTextChange(String query) {
        notesListAdapter.filterList(query.toLowerCase());
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private void getSettings(SharedPreferences preferences) {
        colourPrimary = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_PRIMARY, ContextCompat.getColor(NotesListActivity.this, R.color.colorPrimary));
        colourFont = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK);
        colourBackground = preferences.getInt(HelperUtils.PREFERENCE_COLOUR_BACKGROUND, Color.WHITE);
        colourNavbar = preferences.getBoolean(HelperUtils.PREFERENCE_COLOUR_NAVBAR, false);
        sortAlphabetical = preferences.getBoolean(PREFERENCE_SORT_ALPHABETICAL, false);
    }

    private void applySettings() {
        HelperUtils.applyColours(NotesListActivity.this, colourPrimary, colourNavbar);
        findViewById(R.id.layout_coordinator).setBackgroundColor(colourBackground);
        emptyText.setTextColor(colourFont);
        fab.setBackgroundTintList(ColorStateList.valueOf(colourPrimary));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(colourPrimary));
        }
    }

    private void setItemTouchHelper(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (viewHolder != null) {
                    final View foregroundView = ((NotesListAdapter.ViewHolder) viewHolder).constraintLayout;
                    getDefaultUIUtil().onSelected(foregroundView);
                }
            }

            @Override
            public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                        int actionState, boolean isCurrentlyActive) {
                final View foregroundView = ((NotesListAdapter.ViewHolder) viewHolder).constraintLayout;
                getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final View foregroundView = ((NotesListAdapter.ViewHolder) viewHolder).constraintLayout;
                getDefaultUIUtil().clearView(foregroundView);
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                final View foregroundView = ((NotesListAdapter.ViewHolder) viewHolder).constraintLayout;

                getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                        actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                notesListAdapter.deleteFile(viewHolder.getAdapterPosition());
            }
        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    public void newNote(View view) {
        startActivity(NoteActivity.getStartIntent(NotesListActivity.this, ""));
    }

    private ArrayList<File> getFiles() {
        File[] files = getFilesDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(HelperUtils.TEXT_FILE_EXTENSION);
            }
        });
        return new ArrayList<>(Arrays.asList(files));
    }
}
