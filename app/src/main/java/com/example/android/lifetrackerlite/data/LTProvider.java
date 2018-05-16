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

import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;

public class LTProvider extends ContentProvider {

    private static final int GOALSHABITS = 100;
    private static final int GOALSHABITS_ID = 101;


    //URI matcher to handle different URIs input into provider
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(LTContract.CONTENT_AUTHORITY, LTContract.PATH_GOALSHABITS, GOALSHABITS);
        sUriMatcher.addURI(LTContract.CONTENT_AUTHORITY, LTContract.PATH_GOALSHABITS + "/#", GOALSHABITS_ID);
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
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case GOALSHABITS:
                return insertGoal(uri, contentValues);
            default:
                //Query is not supported for a specific GOAL ID, will hit default exception
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }

    }

    private Uri insertGoal(Uri uri, ContentValues values) {
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

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
