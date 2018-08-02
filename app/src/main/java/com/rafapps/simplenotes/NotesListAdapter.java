package com.rafapps.simplenotes;

import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;

class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private ArrayList<File> filesList;

    NotesListAdapter(ArrayList<File> files) {
        filesList = files;
    }

    @Override
    public void onBindViewHolder(@NonNull NotesListAdapter.ViewHolder holder, int position) {
        File file = filesList.get(position);
        String fileName = file.getName().substring(0, file.getName().length() - 4);
        String fileDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(file.lastModified()) + "\n" + DateFormat.getTimeInstance(DateFormat.SHORT).format(file.lastModified());
        holder.setData(fileName, fileDate);
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

    void updateDataList(ArrayList<File> files) {
        filesList = files;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle;
        private final TextView noteDate;
        private String stringTitle;

        ViewHolder(View v) {
            super(v);
            noteTitle = v.findViewById(R.id.tv_title);
            noteDate = v.findViewById(R.id.tv_date);
            noteTitle.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            noteDate.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemView.getContext().startActivity(NoteActivity.getStartIntent(itemView.getContext(), stringTitle));
        }

        void setData(String title, String date) {
            stringTitle = title;
            noteTitle.setText(title);
            noteDate.setText(date);
        }
    }
}
