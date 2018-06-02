package com.example.android.lifetrackerlite;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTContract.StreaksEntry;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GoalEditorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    private Uri mCurrentGoalUri;
    private int mCurrentGoalID;
    private static final int GOAL_EDIT_LOADER = 1;
    private static final int STREAK_LOADER = 2;

    private int mGoalType;
    private int mGoalOrHabit;
    private int mStartOrEndDate;
    private static final int GOAL_START_DATE = 0;
    private static final int GOAL_END_DATE = 1;
    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;
    private boolean mStartDateSet;
    private boolean mEndDateSet;
    private int mEndYear;
    private int mEndMonth;
    private int mEndDay;

    //Views for DatePicker used for Goal Start Date
    private TextView mGoalNameTextView;
    private TextView mGoalTypeTextView;
    private TextView mGoalStartDateDisplay;
    private TextView mGoalEndDateDisplay;
    private TextView mStreakDataTextView;
    private TextView mStreakDataLengthTextView;
    private EditText mNameEditText;
    private Spinner mGoalTypeSpinner;
    private Button mPickStartDate;
    private Button mPickEndDate;
    private Button mAddGoal;
    private Button mDeleteGoal;
    private Button mFailResetStreak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_editor);

        //Get the intent that created activity to determine if activity should be in "insert mode"
        //for inserting a new goal or "edit mode" for editing an existing goal.
        Intent intent = getIntent();
        mCurrentGoalUri = intent.getData();

        //Check if you are editing a goal or a habit. if neither then set the default value to NEITHER
        mGoalOrHabit = intent.getIntExtra("GoalorHabit", GoalsHabitsEntry.NEITHER);

        //Find views to read user input from
        mGoalTypeSpinner = (Spinner) findViewById(R.id.spinner_goal_type);
        setupSpinner();

        mGoalNameTextView = (TextView) findViewById(R.id.goal_name_textview);
        mGoalTypeTextView = (TextView) findViewById(R.id.goal_type_textview);
        mStreakDataTextView = (TextView) findViewById(R.id.streak_data);
        mStreakDataLengthTextView = (TextView) findViewById(R.id.streak_data_length);
        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mGoalStartDateDisplay = (TextView) findViewById(R.id.goal_start_date_display);
        mGoalEndDateDisplay = (TextView) findViewById(R.id.goal_end_date_display);
        mPickStartDate = (Button) findViewById(R.id.goal_start_date_button);
        mPickEndDate = (Button) findViewById(R.id.goal_end_date_button);
        mAddGoal = (Button) findViewById(R.id.add_goal_editor);
        mDeleteGoal = (Button) findViewById(R.id.delete_goal_editor);
        mFailResetStreak = (Button) findViewById(R.id.fail_reset_streak);

        //Dates are not set when activity starts
        mStartDateSet = false;
        mEndDateSet = false;

        if (mCurrentGoalUri == null) {
            //If editing a goal, set strings within editor activity to goal strings
            if (mGoalOrHabit == GoalsHabitsEntry.GOAL) {
                //Set up workspace and strings for Add Goal mode
                setAddGoalWorkspace();
            }
            //If editing a habit, set strings within editor activity to habit strings
            if (mGoalOrHabit == GoalsHabitsEntry.HABIT) {
                //Set up workspace and strings for Add Habit mode
                setAddHabitWorkspace();
            }

        } else {
            //Set up workspace and strings for Edit Goal mode
            setEditGoalWorkspace();
            getLoaderManager().initLoader(GOAL_EDIT_LOADER, null, this);
        }

        mPickStartDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //If selecting a start date, set the mStartorEndDate variable to be GOAL START DATE
                //to let the system know to set dates for StartDate variable
                mStartOrEndDate = GOAL_START_DATE;
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        mPickEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If selecting an end date, set the mStartorEndDate variable to be GOAL END DATE
                //to let the system know to set dates for EndDate variable
                mStartOrEndDate = GOAL_END_DATE;
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        mAddGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean goalUpdatedorInserted = false;
                if (mCurrentGoalUri == null) {
                    //Insert Goal when Add Goal button is clicked if in "Insert Mode"
                    goalUpdatedorInserted = insertGoal();
                } else {
                    //Update Gal when Add Goal button is clicked  if in "Edit Mode"
                    goalUpdatedorInserted = updateGoal();
                }
                if (goalUpdatedorInserted) {
                    finish();
                }
            }
        });

        mDeleteGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteGoal();
            }
        });

        mFailResetStreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If you are able to insert a streak, clear the start and end dates so new streak
                //can be started
                if (insertStreak()) {
                    clearStartAndEndDates();
                }
            }
        });


    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter goalTypeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_goal_types, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        goalTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGoalTypeSpinner.setAdapter(goalTypeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGoalTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.goal_type_other))) {
                        mGoalType = GoalsHabitsEntry.GOAL_TYPE_OTHER; // Other
                    } else if (selection.equals(getString(R.string.goal_type_fitness))) {
                        mGoalType = GoalsHabitsEntry.GOAL_TYPE_FITNESS; // Fitness
                    } else {
                        mGoalType = GoalsHabitsEntry.GOAL_TYPE_READ; // Read
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGoalType = GoalsHabitsEntry.GOAL_TYPE_OTHER; // Unknown
            }
        });
    }

    public boolean insertGoal() {

        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();

        //Ensure fields are properly defined before inserting a new goal
        if (nameString.isEmpty() || nameString == null || undefinedStartDate() || undefinedEndDate() || mGoalOrHabit == GoalsHabitsEntry.NEITHER) {
            Toast.makeText(this, "All Fields Must Be Populated", Toast.LENGTH_SHORT).show();
            return false;
        }

        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long endDate = dateToUnixTime(mEndYear, mEndMonth, mEndDay);


        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NAME, nameString);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, mGoalOrHabit);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_TYPE, mGoalType);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_END_DATE, endDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_NO);

        //Insert values into database
        Uri uri = getContentResolver().insert(GoalsHabitsEntry.CONTENT_URI, values);

        Toast.makeText(this, "Goal Inserted", Toast.LENGTH_SHORT).show();

        clearStartAndEndDates();
        return true;

    }

    private boolean updateGoal() {

        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();

        //Ensure fields are properly defined before updating a goal
        if (nameString.isEmpty() || nameString == null || undefinedStartDate() || undefinedEndDate() || mGoalOrHabit == GoalsHabitsEntry.NEITHER) {
            Toast.makeText(this, "All Fields Must Be Populated", Toast.LENGTH_SHORT).show();
            return false;
        }

        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long endDate = dateToUnixTime(mEndYear, mEndMonth, mEndDay);
        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NAME, nameString);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, mGoalOrHabit);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_TYPE, mGoalType);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_END_DATE, endDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_NO);

        int rowsUpdated = getContentResolver().update(mCurrentGoalUri, values, null, null);

        Toast.makeText(this, "Goal Updated", Toast.LENGTH_SHORT).show();

        clearStartAndEndDates();

        return true;
    }

    public void deleteGoal() {
        // If Delete Button is clicked from within Editor Activity, pet is deleted.

        int rowsDeleted = getContentResolver().delete(mCurrentGoalUri, null, null);
        Toast.makeText(this, "Goal Deleted", Toast.LENGTH_SHORT).show();
        clearStartAndEndDates();
        finish();
    }

    public boolean insertStreak() {

        //Ensure fields are properly defined before inserting a new streak
        if (undefinedStartDate() || undefinedEndDate()) {
            Toast.makeText(this, "Date fields must be populated", Toast.LENGTH_SHORT).show();
            return false;
        }

        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long endDate = dateToUnixTime(mEndYear, mEndMonth, mEndDay);
        long failDate = System.currentTimeMillis() / 1000;

        ContentValues values = new ContentValues();
        values.put(StreaksEntry.COLUMN_PARENT_ID, mCurrentGoalID);
        values.put(StreaksEntry.COLUMN_STREAK_START_DATE, startDate);
        values.put(StreaksEntry.COLUMN_STREAK_END_DATE, endDate);
        values.put(StreaksEntry.COLUMN_STREAK_FAIL_DATE, failDate);

        //Insert values into database
        Uri uri = getContentResolver().insert(StreaksEntry.CONTENT_URI, values);

        Toast.makeText(this, "Streak Reset", Toast.LENGTH_SHORT).show();

        return true;


    }

    private void setAddGoalWorkspace() {
        //Set up workspace and strings for Add Goal mode
        setTitle(R.string.add_goal_activity_title);
        mDeleteGoal.setVisibility(View.GONE);
        mAddGoal.setText(R.string.add_goal_button);
        mGoalNameTextView.setText(R.string.goal_name);
        mGoalTypeTextView.setText(R.string.goal_type);
    }

    private void setAddHabitWorkspace() {
        //Set up workspace and strings for Add Habit mode
        setTitle(R.string.add_habit_activity_title);
        mDeleteGoal.setVisibility(View.GONE);
        mAddGoal.setText(R.string.add_habit_button);
        mGoalNameTextView.setText(R.string.habit_name);
        mGoalTypeTextView.setText(R.string.habit_type);
    }

    private void setEditGoalWorkspace() {
        //Set up workspace and strings for Edit Goal mode
        setTitle(getString(R.string.edit_goal_activity_title));
        mDeleteGoal.setVisibility(View.VISIBLE);
        mAddGoal.setText(R.string.save_goal_button);
        mDeleteGoal.setText(R.string.delete_goal_button);
    }

    private void setEditHabitWorkspace() {
        //Set up workspace and strings for Edit Habit mode
        setTitle(getString(R.string.edit_habit_activity_title));
        mDeleteGoal.setVisibility(View.VISIBLE);
        mAddGoal.setText(R.string.save_habit_button);
        mGoalNameTextView.setText(R.string.habit_name);
        mGoalTypeTextView.setText(R.string.habit_type);
        mDeleteGoal.setText(R.string.delete_habit_button);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        //Create Goal and Streak loaders

        switch (id) {

            case GOAL_EDIT_LOADER:
                String[] goalProjection = {
                        GoalsHabitsEntry._ID,
                        GoalsHabitsEntry.COLUMN_GOAL_NAME,
                        GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT,
                        GoalsHabitsEntry.COLUMN_GOAL_TYPE,
                        GoalsHabitsEntry.COLUMN_GOAL_START_DATE,
                        GoalsHabitsEntry.COLUMN_GOAL_END_DATE,
                        GoalsHabitsEntry.COLUMN_GOAL_COMPLETED};

                return new CursorLoader(this,
                        mCurrentGoalUri,
                        goalProjection,
                        null,
                        null,
                        null);

            case STREAK_LOADER:
                //TODO figure out way to only load streaks for current goal

                String[] streakProjection = {
                        StreaksEntry._ID,
                        StreaksEntry.COLUMN_PARENT_ID,
                        StreaksEntry.COLUMN_STREAK_START_DATE,
                        StreaksEntry.COLUMN_STREAK_END_DATE,
                        StreaksEntry.COLUMN_STREAK_FAIL_DATE};

                String selection = StreaksEntry.COLUMN_PARENT_ID + "=?";
                String[] selectionArgs = new String[]{String.valueOf(mCurrentGoalID)};

                return new CursorLoader(this,
                        StreaksEntry.CONTENT_URI,
                        streakProjection,
                        selection,
                        selectionArgs,
                        null);
        }

        //TODO  Figure out what to return in default scenario
        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Load data for Goal and Streak loaders

        if (cursor.getCount() >= 1) {
            switch (loader.getId()) {
                case GOAL_EDIT_LOADER:
                    while (cursor.moveToNext()) {

                        //Load the current goal _ID, and once you have that info you can begin loading the Streak data for that goal
                        mCurrentGoalID = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry._ID));
                        getLoaderManager().initLoader(STREAK_LOADER, null, this);

                        // Load the data from the cursor for the single goal you are editing
                        String goalName = cursor.getString(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_NAME));
                        mGoalOrHabit = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT));
                        int goalType = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE));
                        long startDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_START_DATE)) * 1000;
                        long endDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_END_DATE)) * 1000;


                        mNameEditText.setText(goalName, TextView.BufferType.EDITABLE);
                        //TODO Determine goal type spinner position... Find a way to remove this code by auomatically determining spinner position
                        int goalTypeSpinnerPosition = 0;
                        switch (goalType) {
                            case GoalsHabitsEntry.GOAL_TYPE_FITNESS:
                                goalTypeSpinnerPosition = 1;
                                break;
                            case GoalsHabitsEntry.GOAL_TYPE_READ:
                                goalTypeSpinnerPosition = 2;
                                break;
                            default:
                                break;
                        }
                        mGoalTypeSpinner.setSelection(goalTypeSpinnerPosition);

                        //Set default goal start date
                        SimpleDateFormat startSdf = new SimpleDateFormat("MMMM d, yyyy");
                        String startDateString = startSdf.format(startDateMillis);
                        Calendar startCal = Calendar.getInstance();
                        startCal.setTimeInMillis(startDateMillis);
                        mStartYear = startCal.get(Calendar.YEAR);
                        mStartMonth = startCal.get(Calendar.MONTH);
                        mStartDay = startCal.get(Calendar.DAY_OF_MONTH);
                        mStartDateSet = true;
                        mGoalStartDateDisplay.setText(startDateString);

                        //Set default goal end date
                        SimpleDateFormat endSdf = new SimpleDateFormat("MMMM d, yyyy");
                        String endDateString = endSdf.format(endDateMillis);
                        Calendar endCal = Calendar.getInstance();
                        endCal.setTimeInMillis(endDateMillis);
                        mEndYear = endCal.get(Calendar.YEAR);
                        mEndMonth = endCal.get(Calendar.MONTH);
                        mEndDay = endCal.get(Calendar.DAY_OF_MONTH);
                        mEndDateSet = true;
                        mGoalEndDateDisplay.setText(endDateString);

                        if (mGoalOrHabit == GoalsHabitsEntry.HABIT) {
                            //If habit is loaded, set up workspace and strings for Edit Habit mode
                            setEditHabitWorkspace();
                        }


                    }
                    break;
                case STREAK_LOADER:

                    String streakDetailString = "";
                    String streakLengthString = "";
                    while (cursor.moveToNext()) {

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

                        //Set streak details string
                        streakLengthString += Long.toString(streakLengthDays) + " days" + "\n";

                        //Set goal details string

                        streakDetailString += "" + startDateString + " ---> ";
                        streakDetailString += "" + failDateString + "\n";


                    }
                    mStreakDataTextView.setText(streakDetailString);
                    mStreakDataLengthTextView.setText(streakLengthString);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()) {

            //Reset data for Goal and Streak loaders

            case GOAL_EDIT_LOADER:
                // Clear Edit Views upon reset.
                mNameEditText.setText("", TextView.BufferType.EDITABLE);
                mGoalTypeSpinner.setAdapter(null);
                mGoalStartDateDisplay.setText("");
                mGoalEndDateDisplay.setText("");
                break;

            case STREAK_LOADER:

                //Clear streak data upon reset.
                mStreakDataTextView.setText("");
                break;

        }
    }

    private long dateToUnixTime(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR, 5);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis() / 1000;
    }

    private boolean endDateAfterStartDate() {
        //Check if goal end date is after start date, if so, make Toast.
        long startDateUnix = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long endDateUnix = dateToUnixTime(mEndYear, mEndMonth, mEndDay);
        if (startDateUnix >= endDateUnix) {
            Toast.makeText(this, "End Date must be after Start Date", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        //Set date display when date set by DatePicker.  Determine if the date is a start date
        //or if the date is an end date and set the date integer values accordingly.
        if (mStartOrEndDate == GOAL_START_DATE) {
            mStartYear = year;
            mStartMonth = month;
            mStartDay = day;
            mStartDateSet = true;
            //If end date is not after start date, display toast message and return
            if (mEndDateSet && !endDateAfterStartDate()) {
                return;
            }

            long startDateMillis = dateToUnixTime(mStartYear, mStartMonth, mStartDay) * 1000;
            SimpleDateFormat startSdf = new SimpleDateFormat("MMMM d, yyyy");
            String startDateString = startSdf.format(startDateMillis);
            mGoalStartDateDisplay.setText(startDateString);

        } else if (mStartOrEndDate == GOAL_END_DATE) {
            mEndYear = year;
            mEndMonth = month;
            mEndDay = day;
            mEndDateSet = true;
            //If end date is not after start date, display toast message and return
            if (mStartDateSet && !endDateAfterStartDate()) {
                return;
            }

            long endDateMillis = dateToUnixTime(mEndYear, mEndMonth, mEndDay) * 1000;
            SimpleDateFormat endSdf = new SimpleDateFormat("MMMM d, yyyy");
            String endDateString = endSdf.format(endDateMillis);
            mGoalEndDateDisplay.setText(endDateString);
        } else {
            return;
        }


    }

    public static class DatePickerFragment extends DialogFragment {

        //TODO Find a way to default in the start or end date if they are already selected for a goal you are editing

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), (GoalEditorActivity) getActivity(), year, month, day);
        }


    }

    private void clearStartAndEndDates() {
        //Clear out StartDateSet and EndDateSet booleans to indicate dates are no longer set
        mStartDateSet = false;
        mEndDateSet = false;
        mStartYear = 0;
        mStartMonth = 0;
        mStartDay = 0;
        mEndYear = 0;
        mEndMonth = 0;
        mEndDay = 0;
        mGoalStartDateDisplay.setText("");
        mGoalEndDateDisplay.setText("");
    }


    private boolean undefinedStartDate() {
        //Check if date is properly defined
        if (mStartYear == 0 || mStartMonth == 0 || mStartDay == 0) {
            return true;
        }
        return false;
    }

    private boolean undefinedEndDate() {
        //Check if date is properly defined
        if (mEndYear == 0 || mEndMonth == 0 || mEndDay == 0) {
            return true;
        }
        return false;
    }
}
