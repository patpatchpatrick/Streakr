package com.example.android.lifetrackerlite;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lifetrackerlite.data.LTContract;

import java.text.SimpleDateFormat;

public class StreakDataRecyclerAdapter extends RecyclerView.Adapter<StreakDataRecyclerAdapter.ViewHolder> {

    Cursor dataCursor;
    Context context;

    @Override
    public StreakDataRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View streakView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_streak_item, parent, false);
        return new StreakDataRecyclerAdapter.ViewHolder(streakView);
    }

    public StreakDataRecyclerAdapter(Activity mContext, Cursor cursor) {

        dataCursor = cursor;
        context = mContext;


    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        dataCursor.moveToPosition(position);

        String streakNotes = dataCursor.getString(dataCursor.getColumnIndexOrThrow(LTContract.StreaksEntry.COLUMN_STREAK_NOTES));

        //Convert unix start date to string and add to details
        long startDateMillis = dataCursor.getLong(dataCursor.getColumnIndexOrThrow(LTContract.StreaksEntry.COLUMN_STREAK_START_DATE)) * 1000;
        SimpleDateFormat startSdf = new SimpleDateFormat("MMMM d, yyyy");
        String startDateString = startSdf.format(startDateMillis);

        //Convert unix end date to string and add to details
        long endDateMillis = dataCursor.getLong(dataCursor.getColumnIndexOrThrow(LTContract.StreaksEntry.COLUMN_STREAK_END_DATE)) * 1000;
        SimpleDateFormat endSdf = new SimpleDateFormat("MMMM d, yyyy");
        String endDateString = endSdf.format(endDateMillis);

        //Convert unix fail date to string and add to details
        long failDateMillis = dataCursor.getLong(dataCursor.getColumnIndexOrThrow(LTContract.StreaksEntry.COLUMN_STREAK_FAIL_DATE)) * 1000;
        SimpleDateFormat failSdf = new SimpleDateFormat("MMMM d, yyyy");
        String failDateString = failSdf.format(failDateMillis);

        //Get data for streak length and percent
        long streakLengthMillis = failDateMillis - startDateMillis;
        long streakLengthDays = streakLengthMillis / (1000 * 60 * 60 * 24);
        long totalGoalLengthMillis = endDateMillis - startDateMillis;
        long totalGoalLengthDays = totalGoalLengthMillis / (1000 * 60 * 60 * 24);
        int streakCompletionPercent = (int) Math.round(((double) streakLengthDays / (double) totalGoalLengthDays) * 100);

        //Set streak date strings
        holder.streakStartDate.setText(startDateString);
        holder.streakFailDate.setText(failDateString);

        //Set streak length string
        holder.streakLength.setText(Long.toString(streakLengthDays) + " days" + "\n");

        if (streakNotes.trim().isEmpty()) {
            holder.noteIcon.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_streak_blank_note));
        } else {
            holder.noteIcon.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_streak_notesvg));
        }


    }


    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }



    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView streakStartDate;
        public TextView streakFailDate;
        public TextView streakLength;
        public Button noteIcon;


        public ViewHolder(View view) {
            super(view);

            streakStartDate = (TextView) view.findViewById(R.id.streak_list_start_date);
            streakFailDate = (TextView) view.findViewById(R.id.streak_list_fail_date);
            streakLength = (TextView) view.findViewById(R.id.streak_list_streak_length);
            noteIcon = (Button) view.findViewById(R.id.streak_list_note_icon);

        }
    }

    public Cursor swapCursor(Cursor cursor) {

        if (dataCursor == cursor) {
            return null;
        }

        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;

        if (cursor != null) {
            this.notifyDataSetChanged();

        }
        return oldCursor;
    }
}
