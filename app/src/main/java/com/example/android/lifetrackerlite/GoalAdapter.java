package com.example.android.lifetrackerlite;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;

import java.text.SimpleDateFormat;

public class GoalAdapter extends CursorAdapter {
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

        //Set goal name and type string
        TextView goalNameType = (TextView) view.findViewById(R.id.goal_name);
        String goalNameTypeText = cursor.getString(cursor.getColumnIndexOrThrow(LTContract.GoalsHabitsEntry.COLUMN_GOAL_NAME)) + "\n";
        goalNameTypeText += GoalsHabitsEntry.getGoalTypeString(cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE)));
        goalNameType.setText(goalNameTypeText);

        int goalOrHabit = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT));
        ImageView goalHabitIcon = (ImageView) view.findViewById(R.id.goal_habit_icon);
        if (goalOrHabit == GoalsHabitsEntry.GOAL) {
            goalHabitIcon.setImageResource(R.drawable.goal_icon);
        }
        if (goalOrHabit == GoalsHabitsEntry.HABIT) {
            goalHabitIcon.setImageResource(R.drawable.habit_icon);
        }

        //Get data for goal details string

        //Convert unix start date to string and add to details
        long startDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_START_DATE)) * 1000;
        SimpleDateFormat startSdf = new SimpleDateFormat("MMMM d, yyyy");
        String startDateString = startSdf.format(startDateMillis);

        //Convert unix end date to string and add to details
        long endDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_END_DATE)) * 1000;
        SimpleDateFormat endSdf = new SimpleDateFormat("MMMM d, yyyy");
        String endDateString = endSdf.format(endDateMillis);

        //Set goal details string
        TextView goalDetails = (TextView) view.findViewById(R.id.goal_details);
        String goalDetailString = "";
        goalDetailString += "S: " + startDateString + "\n";
        goalDetailString += "E: " + endDateString;
        goalDetails.setText(goalDetailString);

        //Get data for streak details string
        long currentTimeMillis = System.currentTimeMillis();
        long streakLengthMillis = currentTimeMillis - startDateMillis;
        long streakLengthDays = streakLengthMillis / (1000 * 60 * 60 * 24);
        long totalGoalLengthMillis = endDateMillis - startDateMillis;
        long totalGoalLengthDays = totalGoalLengthMillis / (1000 * 60 * 60 * 24);
        int streakCompletionPercent = (int) Math.round(((double) streakLengthDays / (double) totalGoalLengthDays) * 100);

        //Set streak details string
        TextView streakLengthView = (TextView) view.findViewById(R.id.streak_length);
        String streakDetailsString = "";
        streakDetailsString += Long.toString(streakLengthDays) + " days" + "\n";
        streakDetailsString += Integer.toString(streakCompletionPercent) + "%";
        streakLengthView.setText(streakDetailsString);

        //Set color of streak circle based on streak percent completion
        int streakColor = ContextCompat.getColor(context, getStreakColor(streakCompletionPercent));
        // Fetch the background from the TextView, which is a GradientDrawable.
        GradientDrawable magnitudeCircle = (GradientDrawable) streakLengthView.getBackground();
        // Set the color on the magnitude circle
        magnitudeCircle.setColor(streakColor);



    }

    public int getStreakColor(int streakCompletionPercent) {
        int streakColor;
        if (streakCompletionPercent >= 0 && streakCompletionPercent < 10) {
            streakColor = R.color.streakLength10;
        } else if (streakCompletionPercent >= 10 && streakCompletionPercent < 20) {
            streakColor = R.color.streakLength20;
        } else if (streakCompletionPercent >= 20 && streakCompletionPercent < 30) {
            streakColor = R.color.streakLength30;
        } else if (streakCompletionPercent >= 30 && streakCompletionPercent < 40) {
            streakColor = R.color.streakLength40;
        } else if (streakCompletionPercent >= 40 && streakCompletionPercent < 50) {
            streakColor = R.color.streakLength50;
        } else if (streakCompletionPercent >= 50 && streakCompletionPercent < 60) {
            streakColor = R.color.streakLength60;
        } else if (streakCompletionPercent >= 60 && streakCompletionPercent < 70) {
            streakColor = R.color.streakLength70;
        } else if (streakCompletionPercent >= 70 && streakCompletionPercent < 80) {
            streakColor = R.color.streakLength80;
        } else if (streakCompletionPercent >= 80 && streakCompletionPercent < 90) {
            streakColor = R.color.streakLength90;
        } else {
            streakColor = R.color.streakLength100;
        }

        return streakColor;
    }
}
