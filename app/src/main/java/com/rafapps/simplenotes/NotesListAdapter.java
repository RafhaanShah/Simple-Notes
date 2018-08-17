package com.rafapps.simplenotes;

import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private List<File> fullList;
    private List<File> filesList;

    NotesListAdapter() {
        filesList = new ArrayList<>();
        fullList = new ArrayList<>();
    }

    //TODO: Change "View v" to "View view", supply colours to view holder, add delete popup

    @Override
    public void onBindViewHolder(@NonNull NotesListAdapter.ViewHolder holder, int position) {
        File file = filesList.get(position);
        String fileName = file.getName().substring(0, file.getName().length() - 4);
        String fileDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(file.lastModified());
        String fileTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(file.lastModified());
        holder.setData(fileName, fileDate, fileTime);
    }

    @NonNull
    @Override
    public NotesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    void updateList(List<File> files, boolean sortAlphabetical) {
        filesList = files;
        sortList(sortAlphabetical);
        fullList = new ArrayList<>(filesList);
    }

    void deleteFile(int position) {
        File file = filesList.get(position);
        fullList.remove(file);
        filesList.remove(file);
        notifyItemRemoved(position);
        file.delete();
    }

    void sortList(boolean sortAlphabetical) {
        if (sortAlphabetical) {
            sortAlphabetical(filesList);
        } else {
            sortDate(filesList);
        }
        DiffUtil.calculateDiff(new NotesDiffCallback(fullList, filesList)).dispatchUpdatesTo(this);
        fullList = new ArrayList<>(filesList);
    }

    void filterList(String query) {
        if (TextUtils.isEmpty(query)) {
            DiffUtil.calculateDiff(new NotesDiffCallback(filesList, fullList)).dispatchUpdatesTo(this);
            filesList = new ArrayList<>(fullList);
        } else {
            filesList.clear();
            for (int i = 0; i < fullList.size(); i++) {
                final File file = fullList.get(i);
                final String fileName = file.getName().substring(0, file.getName().length() - 4).toLowerCase();
                if (fileName.contains(query)) {
                    filesList.add(fullList.get(i));
                }
            }
            DiffUtil.calculateDiff(new NotesDiffCallback(fullList, filesList)).dispatchUpdatesTo(this);
        }
    }

    private void sortAlphabetical(List<File> files) {
        Collections.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return (f1.getName().compareTo(f2.getName()));
            }
        });
    }

    private void sortDate(List<File> files) {
        Collections.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle;
        private final TextView noteDate;
        private final TextView noteTime;
        private String stringTitle;
        public ConstraintLayout constraintLayout;

        ViewHolder(View v) {
            super(v);
            noteTitle = v.findViewById(R.id.tv_title);
            noteDate = v.findViewById(R.id.tv_date);
            noteTime = v.findViewById(R.id.tv_time);
            noteTitle.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            noteDate.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            noteTime.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            constraintLayout = v.findViewById(R.id.layout_constraint);
            constraintLayout.setBackgroundColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_BACKGROUND, Color.WHITE));
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemView.getContext().startActivity(NoteActivity.getStartIntent(itemView.getContext(), stringTitle));
        }

        void setData(String title, String date, String time) {
            stringTitle = title;
            noteTitle.setText(title);
            noteDate.setText(date);
            noteTime.setText(time);
        }
    }
}
