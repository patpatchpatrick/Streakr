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
import java.util.HashMap;

public class StreakDataRecyclerAdapter extends RecyclerView.Adapter<StreakDataRecyclerAdapter.ViewHolder> {

    Cursor dataCursor;
    Context context;
    HashMap<Integer, Integer> mStreakPositionAndID;

    // OnClickListener for items in the recyclerView
    final private StreakListItemClickListener mStreakOnClickListener;

    @Override
    public StreakDataRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View streakView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_streak_item, parent, false);
        return new StreakDataRecyclerAdapter.ViewHolder(streakView);
    }

    public StreakDataRecyclerAdapter(Activity mContext, Cursor cursor, StreakListItemClickListener listener) {

        dataCursor = cursor;
        context = mContext;
        mStreakOnClickListener = listener;
        mStreakPositionAndID = new HashMap<>();


    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        dataCursor.moveToPosition(position);

        //Get the streak ID and add to HashMap mapping the position and the streak ID
        int streakID = dataCursor.getInt(dataCursor.getColumnIndexOrThrow(LTContract.StreaksEntry._ID));
        mStreakPositionAndID.put(position, streakID);

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
            holder.noteIcon.setTag("");
        } else {
            holder.noteIcon.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.ic_streak_notesvg));
            holder.noteIcon.setTag(streakNotes);
        }



    }


    @Override
    public int getItemCount() {
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }

    // ListItemClickListener with onListItemClick callback for streakDetailsActivity to know which list item was clicked
    public interface StreakListItemClickListener {
        void onStreakListItemClick(int clickedStreakPosition, int clickedStreakID, String note);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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
            noteIcon.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {

            int clickedPosition = getAdapterPosition();
            int streakID = mStreakPositionAndID.get(clickedPosition);
            String note = (String) noteIcon.getTag();
            mStreakOnClickListener.onStreakListItemClick(clickedPosition, streakID, note);

        }
    }

    public Cursor swapCursor(Cursor cursor) {

        if (dataCursor == cursor) {
            return null;
        }

        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;

        if (cursor != null) {
            //Clear the position and ID hashmap so a new one can be created in BindView
            mStreakPositionAndID.clear();
            this.notifyDataSetChanged();

        }
        return oldCursor;
    }
}
