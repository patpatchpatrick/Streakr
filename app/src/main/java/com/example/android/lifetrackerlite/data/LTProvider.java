package com.example.android.lifetrackerlite.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.lifetrackerlite.GoalEditorActivity;
import com.example.android.lifetrackerlite.GoalRecyclerAdapter;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTContract.StreaksEntry;

public class LTProvider extends ContentProvider {

    private static final int GOALSHABITS = 100;
    private static final int GOALSHABITS_ID = 101;
    private static final int STREAKS = 200;
    private static final int STREAKS_ID = 201;


    //URI matcher to handle different URIs input into provider
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(LTContract.CONTENT_AUTHORITY, LTContract.PATH_GOALSHABITS, GOALSHABITS);
        sUriMatcher.addURI(LTContract.CONTENT_AUTHORITY, LTContract.PATH_GOALSHABITS + "/#", GOALSHABITS_ID);
        sUriMatcher.addURI(LTContract.CONTENT_AUTHORITY, LTContract.PATH_STREAKS, STREAKS);
        sUriMatcher.addURI(LTContract.CONTENT_AUTHORITY, LTContract.PATH_STREAKS + "/#", STREAKS_ID);
    }

    //Tag for log messages
    public static final String LOG_TAG = LTProvider.class.getSimpleName();
    private LTDbHelper mDbHelper;

    @Override
    public boolean onCreate() {

        //Create new instance of LTDbHelper to access database.
        mDbHelper = new LTDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        //Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //Cursor to hold results of query
        Cursor cursor;

        //Match the URI
        int match = sUriMatcher.match(uri);
        switch (match) {
            case GOALSHABITS:
                //Query the table directly with the given inputs
                cursor = database.query(GoalsHabitsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case GOALSHABITS_ID:
                //Query the table for a specific goal ID
                selection = GoalsHabitsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(GoalsHabitsEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case STREAKS:
                //TODO Update the query to only query streaks of a specific ID depending on what goal you are querying streaks for
                //Query the table for all streaks
                cursor = database.query(StreaksEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case GOALSHABITS:
                return GoalsHabitsEntry.CONTENT_LIST_TYPE;
            case GOALSHABITS_ID:
                return GoalsHabitsEntry.CONTENT_ITEM_TYPE;
            case STREAKS:
                return StreaksEntry.CONTENT_LIST_TYPE;
            case STREAKS_ID:
                return StreaksEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GOALSHABITS:
                return insertGoal(uri, contentValues);
            case STREAKS:
                return insertStreak(uri, contentValues);
            default:
                //Query is not supported for a specific GOAL ID, will hit default exception
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertGoal(Uri uri, ContentValues values) {

        // Checks to determine values are ok before inserting into database
        // Check to ensure name is not null
        String goalName = values.getAsString(GoalsHabitsEntry.COLUMN_GOAL_NAME);
        if (goalName == null) {
            throw new IllegalArgumentException("Goal requires a name");
        }

        // Check that the goal is valid
        Integer goal = values.getAsInteger(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT);
        if (goal == null || !GoalsHabitsEntry.isValidGoal(goal)) {
            throw new IllegalArgumentException("Goal requires valid goal or habit int");
        }

        // Check that the goal type is valid
        Integer goalType = values.getAsInteger(GoalsHabitsEntry.COLUMN_GOAL_TYPE);
        if (goalType == null || !GoalsHabitsEntry.isValidGoalType(goalType)) {
            throw new IllegalArgumentException("Goal requires valid goal type");
        }

        // Check that the goal completed int is valid
        Integer goalCompleted = values.getAsInteger(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED);
        if (goalCompleted == null || !GoalsHabitsEntry.isValidGoalCompleted(goalCompleted)) {
            throw new IllegalArgumentException("Goal requires valid goal completed int");
        }

        //If data is valid, insert data into SQL database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(GoalsHabitsEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify any listeners that the data has changed for the URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    private Uri insertStreak(Uri uri, ContentValues values) {

        // Checks to determine values are ok before inserting into database

        // Check that the streak parent ID is valid
        Integer streakParentID = values.getAsInteger(StreaksEntry.COLUMN_PARENT_ID);
        if (streakParentID == null) {
            throw new IllegalArgumentException("Streak requires valid parent ID");
        }

        // Check that the streak start date is valid
        Integer streakStartDate = values.getAsInteger(StreaksEntry.COLUMN_STREAK_START_DATE);
        if (streakStartDate == null) {
            throw new IllegalArgumentException("Streak requires valid start date");
        }

        // Check that the streak end date is valid
        Integer streakEndDate = values.getAsInteger(StreaksEntry.COLUMN_STREAK_END_DATE);
        if (streakEndDate == null) {
            throw new IllegalArgumentException("Streak requires valid end date");
        }

        // Check that the streak fail date is valid
        Integer streakFailDate = values.getAsInteger(StreaksEntry.COLUMN_STREAK_FAIL_DATE);
        if (streakFailDate == null) {
            throw new IllegalArgumentException("Streak requires valid fail date");
        }

        //If data is valid, insert data into SQL database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long id = db.insert(StreaksEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify any listeners that the data has changed for the URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);


    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);

        int rowsDeleted;

        switch (match) {
            case GOALSHABITS:
                rowsDeleted = database.delete(GoalsHabitsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case GOALSHABITS_ID:
                selection = GoalsHabitsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(GoalsHabitsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        //If 1 or more rows were deleted, notify all listeners that data at the given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GOALSHABITS:
                return updateGoal(uri, contentValues, selection, selectionArgs);
            case GOALSHABITS_ID:
                // For the GOALSHABITS_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = GoalsHabitsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateGoal(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update goals in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more goals).
     * Return the number of rows that were successfully updated.
     */
    private int updateGoal(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the GoalsHabitsEntry.Name key is present,
        // check that the name value is not null.
        if (values.containsKey(GoalsHabitsEntry.COLUMN_GOAL_NAME)) {
            String goalName = values.getAsString(GoalsHabitsEntry.COLUMN_GOAL_NAME);
            if (goalName == null) {
                throw new IllegalArgumentException("Goal requires a name");
            }
        }

        // If the GoalsHabitsEntry.GoalOrHabit key is present,
        // check that the goal or habit value is valid.
        if (values.containsKey(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT)) {
            Integer goal = values.getAsInteger(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT);
            if (goal == null || !GoalsHabitsEntry.isValidGoal(goal)) {
                throw new IllegalArgumentException("Goal requires valid goal or habit int");
            }
        }

        // If the GoalsHabitsEntry.GoalType key is present,
        // check that the goal or habit value is valid.
        if (values.containsKey(GoalsHabitsEntry.COLUMN_GOAL_TYPE)) {
            Integer goalType = values.getAsInteger(GoalsHabitsEntry.COLUMN_GOAL_TYPE);
            if (goalType == null || !GoalsHabitsEntry.isValidGoalType(goalType)) {
                throw new IllegalArgumentException("Goal requires valid goal type");
            }
        }

        // If the GoalsHabitsEntry.GoalCompleted key is present,
        // check that the goal or habit value is valid.
        if (values.containsKey(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED)) {
            Integer goalCompleted = values.getAsInteger(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED);
            if (goalCompleted == null || !GoalsHabitsEntry.isValidGoalCompleted(goalCompleted)) {
                throw new IllegalArgumentException("Goal requires valid goal completed int");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(GoalsHabitsEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
           // getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }


}

