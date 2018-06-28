package com.example.android.lifetrackerlite;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTContract.StreaksEntry;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class StreakDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, StreakDataRecyclerAdapter.StreakListItemClickListener {

    private static final String TAG = StreakDetailsActivity.class.getSimpleName();
    private static final int STREAK_LOADER = 2;

    private int mCurrentGoalID = -1;
    private String mStreakNotes;

    private RecyclerView mRecyclerView;
    private StreakDataRecyclerAdapter mAdapter;
    private TextView mLongestStreakLengthView;
    private TextView mAverageStreakLengthView;
    private GraphView mStreakGraph;
    private PopupWindow mNotesPopupWindow;
    private ScrollView mStreaksScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_details);
        setTitle(R.string.streak_details);

        mLongestStreakLengthView = (TextView) findViewById(R.id.longest_streak_length_textview);
        mAverageStreakLengthView = (TextView) findViewById(R.id.average_streak_length_textview);
        mStreakGraph = (GraphView) findViewById(R.id.streak_graph);
        mStreaksScrollView = (ScrollView) findViewById(R.id.streaks_scroll_view);


        //Get the intent and the current goal ID from the intent to begin loading streak data for that goal
        Intent intent = getIntent();
        mCurrentGoalID = intent.getIntExtra(GoalsHabitsEntry._ID, -1);


        // Find recyclerView for goal/habit list and set linearLayoutManager and recyclerAdapter on recyclerView
        mRecyclerView = findViewById(R.id.recycler_view_streak_details);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(StreakDetailsActivity.this));
        mAdapter = new StreakDataRecyclerAdapter(this, null, this);
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

                    mStreakNotes = cursor.getString(cursor.getColumnIndexOrThrow(StreaksEntry.COLUMN_STREAK_NOTES));

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
                for (Long streakLength : streakLengthData) {
                    streakDataSeries.appendData(new DataPoint(i, streakLength), false, 100);
                    i++;
                }
                streakDataSeries.setColor(ContextCompat.getColor(StreakDetailsActivity.this, R.color.colorAccent));
                mStreakGraph.addSeries(streakDataSeries);

                //TODO fix calculation to show decimal to one digit
                //Calculate average streak length
                averageStreakLength = totalStreakDays / averageStreakCount;

                mLongestStreakLengthView.setText(Long.toString(maxStreakLength) + " days");
                mAverageStreakLengthView.setText(Long.toString(averageStreakLength) + " days");
                mStreakGraph.setBackgroundColor(ContextCompat.getColor(StreakDetailsActivity.this, R.color.colorPrimaryDark));
                int axisColor = ContextCompat.getColor(StreakDetailsActivity.this, R.color.colorTextAndIcons);
                GridLabelRenderer gridLabelRenderer = mStreakGraph.getGridLabelRenderer();
                gridLabelRenderer.setGridColor(axisColor);
                gridLabelRenderer.setHorizontalLabelsColor(axisColor);
                gridLabelRenderer.setVerticalLabelsColor(axisColor);


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

    @Override
    public void onStreakListItemClick(int clickedStreakID) {
        Toast.makeText(StreakDetailsActivity.this, "" + clickedStreakID,
                Toast.LENGTH_SHORT);
        Log.d(TAG, "" + clickedStreakID);

        //Inflate new popup window to edit notes related to goal/streak
        LayoutInflater inflater = getLayoutInflater();
        final View notesView = inflater.inflate(R.layout.view_streak_notes, null);
        mNotesPopupWindow = new PopupWindow(
                notesView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        Button closeButton = (Button) notesView.findViewById(R.id.notes_close_button);
        EditText notesEditText = (EditText) notesView.findViewById(R.id.notes_edit_text);

        //Set note editText to contain current note string data
        notesEditText.setText(mStreakNotes, TextView.BufferType.EDITABLE);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText notesEditText = (EditText) notesView.findViewById(R.id.notes_edit_text);

                // Update the goal note with user note editText value
                //updateNote(notesEditText);

                // Dismiss the popup window
                mNotesPopupWindow.dismiss();
            }
        });

        mNotesPopupWindow.setFocusable(true);
        mNotesPopupWindow.showAtLocation(mStreaksScrollView, Gravity.CENTER, 0, 0);

    }


}
