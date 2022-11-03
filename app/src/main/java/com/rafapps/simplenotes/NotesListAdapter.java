package com.rafapps.simplenotes;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
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

    private List<File> fullList, filesList;
    private int colourText, colourBackground;

    NotesListAdapter(int colourText, int colourBackground) {
        filesList = new ArrayList<>();
        fullList = new ArrayList<>();
        this.colourText = colourText;
        this.colourBackground = colourBackground;
    }

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
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(inflatedView, colourText, colourBackground);
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

    void sortList(boolean sortAlphabetical) {
        if (sortAlphabetical) {
            sortAlphabetical(filesList);
        } else {
            sortDate(filesList);
        }
        DiffUtil.calculateDiff(new NotesDiffCallback(fullList, filesList)).dispatchUpdatesTo(this);
        fullList = new ArrayList<>(filesList);
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

    void deleteFile(int position) {
        File file = filesList.get(position);
        fullList.remove(file);
        filesList.remove(file);
        notifyItemRemoved(position);
        file.delete();
    }

    void cancelDelete(int position) {
        notifyItemChanged(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle, noteDate, noteTime;
        private String stringTitle;
        ConstraintLayout constraintLayout;

        ViewHolder(View view, int colourText, int colourBackground) {
            super(view);
            noteTitle = view.findViewById(R.id.tv_title);
            noteDate = view.findViewById(R.id.tv_date);
            noteTime = view.findViewById(R.id.tv_time);
            noteTitle.setTextColor(colourText);
            noteDate.setTextColor(colourText);
            noteTime.setTextColor(colourText);
            constraintLayout = view.findViewById(R.id.layout_constraint);
            constraintLayout.setBackgroundColor(colourBackground);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
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
