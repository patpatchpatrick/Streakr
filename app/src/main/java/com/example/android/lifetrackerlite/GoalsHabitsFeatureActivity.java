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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTDbHelper;
import com.example.android.lifetrackerlite.GoalEditorActivity;

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

                //New intent to open the editor activity in "insert mode"
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);
                //Send over an extra int to indicate editing a goal
                intent.putExtra("GoalorHabit", GoalsHabitsEntry.GOAL);
                startActivity(intent);

            }
        });

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

        //Get background listview and set adapter to show goal data
        ListView goalsHabitsListView = (ListView) findViewById(R.id.list_view_goals_habits);
        mGoalAdapter = new GoalAdapter(this, null);
        goalsHabitsListView.setAdapter(mGoalAdapter);
        goalsHabitsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

                // Set the URI on the intent of the goal clicked to be the id of the clicked item
                Uri currentGoalUri = ContentUris.withAppendedId(GoalsHabitsEntry.CONTENT_URI, id);

                //Set the data on the intent to be the current URI
                intent.setData(currentGoalUri);

                // Launch the intent to the editor activity to open the activity in "edit mode"
                startActivity(intent);
            }
        });

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

        mGoalAdapter.swapCursor(cursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mGoalAdapter.swapCursor(null);

    }
}
