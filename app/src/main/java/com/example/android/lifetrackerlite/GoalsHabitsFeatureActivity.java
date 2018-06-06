package com.example.android.lifetrackerlite;

import android.app.LoaderManager;
import android.content.ContentUris;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTDbHelper;
import com.example.android.lifetrackerlite.GoalEditorActivity;

import java.util.List;

public class GoalsHabitsFeatureActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GoalRecyclerAdapter.ListItemClickListener{

    private static final int GOALSHABITS_LOADER = 0;

    private RecyclerView mRecyclerView;
    private GoalRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_habits_feature);

        // Add new goal button
        FloatingActionButton addGoalButton = findViewById(R.id.add_goal);
        addGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //New intent to open the editor activity in "insert mode"
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);
                //Send over an extra int to indicate editing a goal
                intent.putExtra("GoalorHabit", GoalsHabitsEntry.GOAL);
                startActivity(intent);

            }
        });

        // Add new habit button
        FloatingActionButton addHabitButton = findViewById(R.id.add_habit);
        addHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //New intent to open the editor activity in "insert mode"
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);
                //Send over an extra int to indicate editing a habit
                intent.putExtra("GoalorHabit", GoalsHabitsEntry.HABIT);
                startActivity(intent);

            }
        });

        // Find recyclerView for goal/habit list and set linearLayoutManager and recyclerAdapter on recyclerView
        mRecyclerView = findViewById(R.id.recycler_view_goals_habits);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(GoalsHabitsFeatureActivity.this));
        mAdapter = new GoalRecyclerAdapter(this, null, this);
        mRecyclerView.setAdapter(mAdapter);

        //Initialize loader for goals data
        getLoaderManager().initLoader(GOALSHABITS_LOADER, null, this);


    }

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

        mAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);

    }

    // When a goal/habit item is clicked, open up that goal/habit in "edit mode"
    @Override
    public void onListItemClick(int clickedItemIndex) {

        // New intent to open GoalEditorActivity class for goal clicked
        Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

        // Set the URI on the intent of the goal clicked to be the id of the clicked item
        // Add 1 to the clickedItemIndex so that URI ID matches correct goal
        Uri currentGoalUri = ContentUris.withAppendedId(GoalsHabitsEntry.CONTENT_URI, clickedItemIndex + 1);

        //Set the data on the intent to be the current URI
        intent.setData(currentGoalUri);

        // Launch the intent to the editor activity to open the activity in "edit mode"
        startActivity(intent);

    }
}
