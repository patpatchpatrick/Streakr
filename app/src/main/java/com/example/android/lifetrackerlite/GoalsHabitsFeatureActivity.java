package com.example.android.lifetrackerlite;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTDbHelper;

import java.util.List;

public class GoalsHabitsFeatureActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int GOALSHABITS_LOADER = 0;

    private GoalAdapter mGoalAdapter;
    private LTDbHelper mLTDbHelper;
    private SQLiteDatabase mLTDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_habits_feature);

        FloatingActionButton addGoalButton = findViewById(R.id.add_goal);
        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);
                startActivity(intent);

            }
        });

        //Get background listview and set adapter to show goal data
        ListView goalsHabitsListView = (ListView) findViewById(R.id.list_view_goals_habits);
        mGoalAdapter = new GoalAdapter(this, null);
        goalsHabitsListView.setAdapter(mGoalAdapter);

        //Initialize loader for goals data
        getLoaderManager().initLoader(GOALSHABITS_LOADER, null, this);


    }

   /** private void insertTestGoal() {
        SQLiteDatabase db = mLTDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NAME, "Test Goal Name 2");
        values.put(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.GOAL);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_TYPE, GoalsHabitsEntry.GOAL_TYPE_OTHER);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, 0);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_END_DATE, 0);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_NO);
        db.insert(GoalsHabitsEntry.TABLE_NAME, null, values);


    } */


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        //Load all goals data to display

        String[] projection = {
                GoalsHabitsEntry._ID,
                GoalsHabitsEntry.COLUMN_GOAL_NAME,
                GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT,
                GoalsHabitsEntry.COLUMN_GOAL_TYPE,
                GoalsHabitsEntry.COLUMN_GOAL_START_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_END_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_COMPLETED};

        return new CursorLoader(this, GoalsHabitsEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        mGoalAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mGoalAdapter.swapCursor(null);

    }
}
