package com.example.android.lifetrackerlite;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;

import java.text.SimpleDateFormat;

public class GoalAdapter extends CursorAdapter{
    //Cursor adapter for the goals data

    public GoalAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        //Inflate new views from Goals Habits Item view
        return LayoutInflater.from(context).inflate(R.layout.list_goalshabits_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView goalName = (TextView) view.findViewById(R.id.goal_name);
        String goalNameText = cursor.getString(cursor.getColumnIndexOrThrow(LTContract.GoalsHabitsEntry.COLUMN_GOAL_NAME));
        goalName.setText(goalNameText);

        //Set details string
        TextView goalDetails = (TextView) view.findViewById(R.id.goal_details);
        String goalDetailString = "";
        goalDetailString += GoalsHabitsEntry.getGoalTypeString(cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE))) + "\n";
        //Convert unix start date to string and add to details
        long startDate = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_START_DATE)) * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        String date = sdf.format(startDate);
        goalDetailString += date;
        goalDetails.setText(goalDetailString);

        TextView streakLength = (TextView) view.findViewById(R.id.streak_length);


    }
}
