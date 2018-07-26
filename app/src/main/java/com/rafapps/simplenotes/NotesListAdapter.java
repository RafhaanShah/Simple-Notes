package com.rafapps.simplenotes;

import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;

class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private String[] notesList;
    private Long[] datesList;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle;
        private final TextView noteDate;
        private String stringTitle;

        ViewHolder(View v) {
            super(v);
            noteTitle = v.findViewById(R.id.noteTitle);
            noteDate = v.findViewById(R.id.noteDate);
            noteTitle.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt("colourFont", Color.parseColor("#000000")));
            noteDate.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt("colourFont", Color.parseColor("#000000")));
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent nextScreen = new Intent(itemView.getContext(), NoteActivity.class);
            nextScreen.putExtra("noteTitle", stringTitle);
            itemView.getContext().startActivity(nextScreen);
        }

        void setData(String title, Long date) {
            stringTitle = title;
            noteTitle.setText(title);
            String dateText = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date) + "\n" + DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
            noteDate.setText(dateText);
        }
    }

    NotesListAdapter(String[] notes, Long[] dates) {
        notesList = notes;
        datesList = dates;
    }

    @NonNull
    @Override
    public NotesListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesListAdapter.ViewHolder holder, int position) {
        holder.setData(notesList[position], datesList[position]);
    }

    @Override
    public int getItemCount() {
        return notesList.length;
    }

    void updateDataList(String[] files, Long[] dates) {
        notesList = files;
        datesList = dates;
        notifyDataSetChanged();
    }
}
