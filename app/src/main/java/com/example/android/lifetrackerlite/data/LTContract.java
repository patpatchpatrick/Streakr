package com.example.android.lifetrackerlite.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class LTContract {

    //Contract for DB used to store data for this app

    //URI Information
    public static final String CONTENT_AUTHORITY = "com.example.android.lifetrackerlite";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_GOALSHABITS = "goalshabits";

    private LTContract(){}

    public static final class GoalsHabitsEntry implements BaseColumns {

        //The MIME type of the {@link #CONTENT_URI} for a list of pets.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOALSHABITS;


         //The MIME type of the {@link #CONTENT_URI} for a single pet.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GOALSHABITS;

        //URI for GoalsHabits table
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,  PATH_GOALSHABITS);


        //Define table and columns for goals and habits data
        public static final String TABLE_NAME = "goalsHabits";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_GOAL_NAME = "goalName";
        public static final String COLUMN_GOAL_OR_HABIT = "goalOrHabit";
        public static final String COLUMN_GOAL_TYPE = "goalType";
        public static final String COLUMN_GOAL_START_DATE = "goalStartDate";
        public static final String COLUMN_GOAL_END_DATE = "goalEndDate";
        public static final String COLUMN_GOAL_COMPLETED = "goalCompleted";

        //Define input variables for goals and habits table
        public static final int GOAL = 0;
        public static final int HABIT = 1;

        public static final int GOAL_TYPE_OTHER = 0;
        public static final int HABIT_TYPE_OTHER = 1;
        public static final int GOAL_TYPE_FITNESS = 2;
        public static final int GOAL_TYPE_READ = 3;
        public static final int HABIT_TYPE_NOFAP = 4;
        public static final int HABIT_TYPE_DRUGS = 5;

        public static final int GOAL_COMPLETED_NO = 0;
        public static final int GOAL_COMPLETED_YES = 1;

    }

}
