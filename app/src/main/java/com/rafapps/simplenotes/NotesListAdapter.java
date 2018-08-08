package com.rafapps.simplenotes;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;

class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.ViewHolder> {

    private ArrayList<File> filesList;
    private Context context;
    private RecyclerView recyclerView;

    NotesListAdapter(ArrayList<File> files, Context getContext, RecyclerView getRecyclerView) {
        filesList = files;
        context = getContext;
        recyclerView = getRecyclerView;
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
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    void updateDataList(ArrayList<File> files, boolean animate) {
        filesList = files;
        if (animate) {
            final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down);
            recyclerView.setLayoutAnimation(controller);
            notifyDataSetChanged();
            recyclerView.scheduleLayoutAnimation();
        } else {
            notifyDataSetChanged();
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView noteTitle;
        private final TextView noteDate;
        private final TextView noteTime;
        private String stringTitle;

        ViewHolder(View v) {
            super(v);
            noteTitle = v.findViewById(R.id.tv_title);
            noteDate = v.findViewById(R.id.tv_date);
            noteTime = v.findViewById(R.id.tv_time);
            noteTitle.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            noteDate.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            noteTime.setTextColor(PreferenceManager.getDefaultSharedPreferences(itemView.getContext()).getInt(HelperUtils.PREFERENCE_COLOUR_FONT, Color.BLACK));
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.v("Click", "Viewholder");
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
