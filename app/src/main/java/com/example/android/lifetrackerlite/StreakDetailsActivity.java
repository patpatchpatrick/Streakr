package com.example.android.lifetrackerlite;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTContract.StreaksEntry;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class StreakDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = StreakDetailsActivity.class.getSimpleName();
    private static final int STREAK_LOADER = 2;

    private int mCurrentGoalID = -1;

    private RecyclerView mRecyclerView;
    private StreakDataRecyclerAdapter mAdapter;
    private TextView mLongestStreakLengthView;
    private TextView mAverageStreakLengthView;
    private GraphView mStreakGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_details);
        setTitle(R.string.streak_details);

        mLongestStreakLengthView = (TextView) findViewById(R.id.longest_streak_length_textview);
        mAverageStreakLengthView = (TextView) findViewById(R.id.average_streak_length_textview);
        mStreakGraph = (GraphView) findViewById(R.id.streak_graph);


        //Get the intent and the current goal ID from the intent to begin loading streak data for that goal
        Intent intent = getIntent();
        mCurrentGoalID = intent.getIntExtra(GoalsHabitsEntry._ID, -1);


        // Find recyclerView for goal/habit list and set linearLayoutManager and recyclerAdapter on recyclerView
        mRecyclerView = findViewById(R.id.recycler_view_streak_details);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(StreakDetailsActivity.this));
        mAdapter = new StreakDataRecyclerAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(STREAK_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        switch (id) {

            case STREAK_LOADER:
                //TODO figure out way to only load streaks for current goal

                String[] streakProjection = {
                        StreaksEntry._ID,
                        StreaksEntry.COLUMN_PARENT_ID,
                        StreaksEntry.COLUMN_STREAK_START_DATE,
                        StreaksEntry.COLUMN_STREAK_END_DATE,
                        StreaksEntry.COLUMN_STREAK_FAIL_DATE,
                        StreaksEntry.COLUMN_STREAK_NOTES};

                String selection = StreaksEntry.COLUMN_PARENT_ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(mCurrentGoalID)};

                return new CursorLoader(this,
                        StreaksEntry.CONTENT_URI,
                        streakProjection,
                        selection,
                        selectionArgs,
                        StreaksEntry.COLUMN_STREAK_FAIL_DATE + " DESC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case STREAK_LOADER:
                mAdapter.swapCursor(cursor);
                long maxStreakLength = -1;
                long averageStreakLength = 0;
                long averageStreakCount = 0;
                long totalStreakDays = 0;
                ArrayList<Long> streakLengthData = new ArrayList<Long>();

                while (cursor.moveToNext()) {

                    String streakNotes = cursor.getString(cursor.getColumnIndexOrThrow(StreaksEntry.COLUMN_STREAK_NOTES));

                    //Convert unix start date to string and add to details
                    long startDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(StreaksEntry.COLUMN_STREAK_START_DATE)) * 1000;
                    SimpleDateFormat startSdf = new SimpleDateFormat("MMMM d, yyyy");
                    String startDateString = startSdf.format(startDateMillis);

                    //Convert unix end date to string and add to details
                    long endDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(StreaksEntry.COLUMN_STREAK_END_DATE)) * 1000;
                    SimpleDateFormat endSdf = new SimpleDateFormat("MMMM d, yyyy");
                    String endDateString = endSdf.format(endDateMillis);

                    //Convert unix fail date to string and add to details
                    long failDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(StreaksEntry.COLUMN_STREAK_FAIL_DATE)) * 1000;
                    SimpleDateFormat failSdf = new SimpleDateFormat("MMMM d, yyyy");
                    String failDateString = failSdf.format(failDateMillis);

                    //Get data for streak length and percent
                    long streakLengthMillis = failDateMillis - startDateMillis;
                    long streakLengthDays = streakLengthMillis / (1000 * 60 * 60 * 24);
                    long totalGoalLengthMillis = endDateMillis - startDateMillis;
                    long totalGoalLengthDays = totalGoalLengthMillis / (1000 * 60 * 60 * 24);
                    int streakCompletionPercent = (int) Math.round(((double) streakLengthDays / (double) totalGoalLengthDays) * 100);

                    //Calculate the maximum streak length
                    if (streakLengthDays > maxStreakLength) {
                        maxStreakLength = streakLengthDays;
                    }

                    //Collect the total streak lengths in an ArrayList to use for streak graph trendline
                    streakLengthData.add(streakLengthDays);

                    //Gather data for average streak length calculation
                    totalStreakDays += streakLengthDays;
                    averageStreakCount++;


                }

                // Reverse the streak length data so the trendline goes in proper direction
                // Add datapoints to streaks trendline to display trending streak data over time
                // X axis represents the streak attempt #
                // Y axis represents the streak length
                LineGraphSeries<DataPoint> streakDataSeries = new LineGraphSeries<>();
                Collections.reverse(streakLengthData);
                int i = 0;
                for(Long streakLength : streakLengthData) {
                    streakDataSeries.appendData(new DataPoint(i, streakLength), false, 100);
                    i++;
                }
                mStreakGraph.addSeries(streakDataSeries);

                //TODO fix calculation to show decimal to one digit
                //Calculate average streak length
                averageStreakLength = totalStreakDays / averageStreakCount;

                mLongestStreakLengthView.setText(Long.toString(maxStreakLength) + " days");
                mAverageStreakLengthView.setText(Long.toString(averageStreakLength) + " days");

                break;

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {

            case STREAK_LOADER:
                mAdapter.swapCursor(null);
                break;
        }

    }
}
