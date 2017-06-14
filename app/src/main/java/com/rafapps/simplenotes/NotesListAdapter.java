package com.rafapps.simplenotes;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private String[] notesList;
    private String[] datesList;

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle;
        private final TextView noteDate;
        private String stringTitle;

        ViewHolder(View v) {
            super(v);
            noteTitle = (TextView) v.findViewById(R.id.noteTitle);
            noteDate = (TextView) v.findViewById(R.id.noteDate);
            noteTitle.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt("colourFont", 0));
            noteDate.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt("colourFont", 0));
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent nextScreen = new Intent(itemView.getContext(), MainActivity.class);
            nextScreen.putExtra("noteTitle", stringTitle);
            itemView.getContext().startActivity(nextScreen);
        }

        void setData(String title, String date) {
            stringTitle = title;
            noteTitle.setText(title);
            noteDate.setText(date);
        }
    }

    NotesListAdapter(String[] notes, String[] dates) {
        notesList = notes;
        datesList = dates;
    }

    @Override
    public NotesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(NotesListAdapter.ViewHolder holder, int position) {
        holder.setData(notesList[position], datesList[position]);
    }

    @Override
    public int getItemCount() {
        return notesList.length;
    }

    void updateDataList(String[] files, String[] dates) {
        notesList = files;
        datesList = dates;
        notifyDataSetChanged();
    }

}
