package com.example.android.lifetrackerlite.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;

public class LTDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = LTDbHelper.class.getSimpleName();

    //DB Name and version
    private static final String DATABASE_NAME = "lifetrackerlite.db";
    private static final int DATABASE_VERSION = 1;

    public LTDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_GOALS_HABITS_TABLE =  "CREATE TABLE " + GoalsHabitsEntry.TABLE_NAME + " ("
                + GoalsHabitsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GoalsHabitsEntry.COLUMN_GOAL_NAME + " TEXT NOT NULL, "
                + GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT + " INTEGER NOT NULL, "
                + GoalsHabitsEntry.COLUMN_GOAL_TYPE + " INTEGER NOT NULL DEFAULT 0, "
                + GoalsHabitsEntry.COLUMN_GOAL_START_DATE + " INTEGER NOT NULL, "
                + GoalsHabitsEntry.COLUMN_GOAL_END_DATE + " INTEGER, "
                + GoalsHabitsEntry.COLUMN_GOAL_COMPLETED + " INTEGER NOT NULL);";


        // Execute the SQL statement
        db.execSQL(SQL_CREATE_GOALS_HABITS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //Nothing to add until we need to do an upgrade
    }
}
