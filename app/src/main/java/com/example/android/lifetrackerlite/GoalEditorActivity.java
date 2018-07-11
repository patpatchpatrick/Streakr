package com.example.android.lifetrackerlite;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;
import com.example.android.lifetrackerlite.data.LTContract.StreaksEntry;
import com.example.android.lifetrackerlite.helper.ThemeHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GoalEditorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = GoalEditorActivity.class.getSimpleName();

    private Uri mCurrentGoalUri;
    private int mCurrentGoalID = -1;
    private static final int GOAL_EDIT_LOADER = 1;
    private static final int STREAK_LOADER = 2;
    private boolean mLoadingNote = false;

    private int mGoalType;
    private int mNumberOfGoals;
    private int mNumberOfStreaks;
    private int mGoalOrHabit;
    private int mDateType;
    private int mGoalComplete;
    private static final int GOAL_START_DATE = 0;
    private static final int GOAL_END_DATE = 1;
    private static final int GOAL_FAIL_DATE = 2;
    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;
    private boolean mStartDateSet;
    private boolean mEndDateSet;
    private boolean mFailDateSet;
    private boolean mGoalHasChanged = false;
    private boolean mNewGoal = false;
    private int mEndYear;
    private int mEndMonth;
    private int mEndDay;
    private int mFailYear;
    private int mFailMonth;
    private int mFailDay;
    private String mCurrentGoalNotes = "";
    private long mStreakLengthDays = -1;

    //Views for DatePicker used for Goal Start Date
    private TextView mGoalNameTextView;
    private TextView mGoalTypeTextView;
    private TextView mGoalStartDateDisplay;
    private TextView mGoalEndDateDisplay;
    private TextView mFailDateDisplay;
    private TextView mStreakDataTextView;
    private TextView mStreakDataLengthTextView;
    private TextView mHistoricalStreaksHeader;
    private TextView mViewStreakDetails;
    private EditText mNameEditText;
    private Spinner mGoalTypeSpinner;
    private Button mPickStartDate;
    private Button mPickEndDate;
    private Button mAddOrSaveGoal;
    private Button mDeleteGoal;
    private Button mNotesButton;
    private Button mFailResetStreak;
    private Button mGoalCompleted;

    private PopupWindow mNotesPopupWindow;
    private LinearLayout mNotesLinearLayout;

    //Keys for onSavedInstanceState
    private static final String LIFECYCLE_START_YEAR_KEY = "start year";
    private static final String LIFECYCLE_START_MONTH_KEY = "start month";
    private static final String LIFECYCLE_START_DAY_KEY = "start day";
    private static final String LIFECYCLE_END_YEAR_KEY = "end year";
    private static final String LIFECYCLE_END_MONTH_KEY = "end month";
    private static final String LIFECYCLE_END_DAY_KEY = "end day";
    private static final String LIFECYCLE_GOAL_OR_HABIT = "goal or habit";
    private static final String LIFECYCLE_STREAK_DATA = "streak data";
    private static final String LIFECYCLE_STREAK_DATA_LENGTH = "streak data length";
    private static final String LIFECYCLE_CURRENT_GOAL_ID = "current goal id";
    private static final String LIFECYCLE_NUMBER_OF_GOALS = "number of goals";
    private static final String LIFECYCLE_CURRENT_URI = "current uri";
    public static final String LIFECYCLE_STREAK_LENGTH = "streak length";

    //Keys for bundle
    private static final String START_YEAR = "start year";
    private static final String START_MONTH = "start month";
    private static final String START_DAY = "start day";
    private static final String END_YEAR = "end year";
    private static final String END_MONTH = "end month";
    private static final String END_DAY = "end day";
    private static final String DATE_TYPE = "date type";
    private static final String START_DATE_SET = "start date set";
    private static final String END_DATE_SET = "end date set";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set up theme from user-selected shared preference theme before activity is created
        setTheme(ThemeHelper.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_editor);
        getSupportActionBar().hide();

        //Set background drawable to null to increase performance (decrease overdraw) since we are drawing a background over it
        getWindow().setBackgroundDrawable(null);

        //Get the intent that created activity to determine if activity should be in "insert mode"
        //for inserting a new goal or "edit mode" for editing an existing goal.
        Intent intent = getIntent();
        mCurrentGoalUri = intent.getData();

        //Check if you are editing a goal or a habit. if neither then set the default value to NEITHER
        mGoalOrHabit = intent.getIntExtra(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.NEITHER);

        //Determine the number of goals in cursor received from GoalsHabitsFeatureActivity
        //Use this number to determine what the goal order of new goal should be
        mNumberOfGoals = intent.getIntExtra(GoalsHabitsEntry.COLUMN_GOAL_ORDER, -2);

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
        mFailDateDisplay = (TextView) findViewById(R.id.failure_date_display);
        mHistoricalStreaksHeader = (TextView) findViewById(R.id.historical_streaks_header);
        mViewStreakDetails = (TextView) findViewById(R.id.view_streak_details);
        mPickStartDate = (Button) findViewById(R.id.goal_start_date_button);
        mPickEndDate = (Button) findViewById(R.id.goal_end_date_button);
        mAddOrSaveGoal = (Button) findViewById(R.id.add_goal_editor);
        mDeleteGoal = (Button) findViewById(R.id.delete_goal_editor);
        mNotesButton = (Button) findViewById(R.id.notes_button);
        mFailResetStreak = (Button) findViewById(R.id.fail_reset_streak);
        mGoalCompleted = (Button) findViewById(R.id.goal_completed);
        mNotesLinearLayout = (LinearLayout) findViewById(R.id.notes_linear_layout);

        //Set onTouchListeners to all views that can be edited for discard changes dialog
        mNameEditText.setOnTouchListener(mTouchListener);
        mPickStartDate.setOnTouchListener(mTouchListener);
        mPickEndDate.setOnTouchListener(mTouchListener);
        mGoalTypeSpinner.setOnTouchListener(mTouchListener);

        //Dates are not set when activity starts
        mStartDateSet = false;
        mEndDateSet = false;
        mFailDateSet = false;


        if (savedInstanceState != null) {


            //If there is a savedInstanceState from the app being unexpectedly terminated, reload the data

            if (savedInstanceState.containsKey(GoalsHabitsEntry.COLUMN_GOAL_NAME)) {
                mGoalNameTextView.setText(savedInstanceState.getString(GoalsHabitsEntry.COLUMN_GOAL_NAME));
            }
            if (savedInstanceState.containsKey(GoalsHabitsEntry.COLUMN_GOAL_TYPE)) {
                mGoalTypeSpinner.setSelection(savedInstanceState.getInt(GoalsHabitsEntry.COLUMN_GOAL_TYPE));
            }
            if (savedInstanceState.containsKey(GoalsHabitsEntry.COLUMN_GOAL_START_DATE)) {
                mGoalStartDateDisplay.setText(savedInstanceState.getString(GoalsHabitsEntry.COLUMN_GOAL_START_DATE));
            }
            if (savedInstanceState.containsKey(GoalsHabitsEntry.COLUMN_GOAL_END_DATE)) {
                mGoalEndDateDisplay.setText(savedInstanceState.getString(GoalsHabitsEntry.COLUMN_GOAL_END_DATE));
            }
            if (savedInstanceState.containsKey(GoalsHabitsEntry.COLUMN_GOAL_NOTES)) {
                mCurrentGoalNotes = savedInstanceState.getString(GoalsHabitsEntry.COLUMN_GOAL_NOTES);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_START_YEAR_KEY)) {
                mStartYear = savedInstanceState.getInt(LIFECYCLE_START_YEAR_KEY);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_START_MONTH_KEY)) {
                mStartMonth = savedInstanceState.getInt(LIFECYCLE_START_MONTH_KEY);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_START_DAY_KEY)) {
                mStartDay = savedInstanceState.getInt(LIFECYCLE_START_DAY_KEY);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_END_YEAR_KEY)) {
                mEndYear = savedInstanceState.getInt(LIFECYCLE_END_YEAR_KEY);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_END_MONTH_KEY)) {
                mEndMonth = savedInstanceState.getInt(LIFECYCLE_END_MONTH_KEY);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_END_DAY_KEY)) {
                mEndDay = savedInstanceState.getInt(LIFECYCLE_END_DAY_KEY);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_GOAL_OR_HABIT)) {
                mGoalOrHabit = savedInstanceState.getInt(LIFECYCLE_GOAL_OR_HABIT);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_CURRENT_GOAL_ID)) {
                mCurrentGoalID = savedInstanceState.getInt(LIFECYCLE_CURRENT_GOAL_ID);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_NUMBER_OF_GOALS)) {
                mNumberOfGoals = savedInstanceState.getInt(LIFECYCLE_NUMBER_OF_GOALS);
            }
            if (savedInstanceState.containsKey(LIFECYCLE_CURRENT_URI)) {
                mCurrentGoalUri = Uri.parse(savedInstanceState.getString(LIFECYCLE_CURRENT_URI));
            }
            if (savedInstanceState.containsKey(LIFECYCLE_STREAK_LENGTH)) {
                mStreakLengthDays = savedInstanceState.getLong(LIFECYCLE_STREAK_LENGTH);
            }
            if (savedInstanceState.containsKey(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED)) {
                mGoalComplete = savedInstanceState.getInt(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED);
                if (mGoalComplete == GoalsHabitsEntry.GOAL_COMPLETED_YES) {
                    mGoalCompleted.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_restart));
                }
            }

        }

        if (mCurrentGoalUri == null) {
            //If editing a goal, set strings within editor activity to goal strings
            if (mGoalOrHabit == GoalsHabitsEntry.GOAL) {
                //Set up workspace and strings for Add Goal mode
                //mNewGoal boolean is used in datepicker so that it knows to populate date with today's date by default
                mNewGoal = true;
                setAddGoalWorkspace();

            }
            //If editing a habit, set strings within editor activity to habit strings
            if (mGoalOrHabit == GoalsHabitsEntry.HABIT) {
                //Set up workspace and strings for Add Habit mode
                //mNewGoal boolean is used in datepicker so that it knows to populate date with today's date by default
                mNewGoal = true;
                setAddHabitWorkspace();

            }

        } else {

            if (mGoalOrHabit == GoalsHabitsEntry.HABIT) {
                //Set up workspace and strings for Edit Habit mode
                setEditHabitWorkspace();
                getLoaderManager().initLoader(GOAL_EDIT_LOADER, null, this);
            } else {
                //Set up workspace and strings for Edit Goal mode
                setEditGoalWorkspace();
                getLoaderManager().initLoader(GOAL_EDIT_LOADER, null, this);
            }
        }


        mPickStartDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //If selecting a start date, set the mStartorEndDate variable to be GOAL START DATE
                //to let the system know to set dates for StartDate variable
                mDateType = GOAL_START_DATE;
                DialogFragment newFragment = new DatePickerFragment();
                Bundle bundle = new Bundle();
                if (mStartDateSet) {
                    bundle.putInt(START_YEAR, mStartYear);
                    bundle.putInt(START_MONTH, mStartMonth);
                    bundle.putInt(START_DAY, mStartDay);
                }
                bundle.putInt(DATE_TYPE, mDateType);
                bundle.putBoolean(START_DATE_SET, mStartDateSet);
                bundle.putBoolean(END_DATE_SET, mEndDateSet);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        mPickEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If selecting an end date, set the mStartorEndDate variable to be GOAL END DATE
                //to let the system know to set dates for EndDate variable

                //Add bundle of data to the datepicker fragment so that it can set the date by default
                mDateType = GOAL_END_DATE;
                DialogFragment newFragment = new DatePickerFragment();
                Bundle bundle = new Bundle();
                if (mEndDateSet) {
                    bundle.putInt(END_YEAR, mEndYear);
                    bundle.putInt(END_MONTH, mEndMonth);
                    bundle.putInt(END_DAY, mEndDay);
                }
                bundle.putInt(DATE_TYPE, mDateType);
                bundle.putBoolean(START_DATE_SET, mStartDateSet);
                bundle.putBoolean(END_DATE_SET, mEndDateSet);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        mAddOrSaveGoal.setOnClickListener(new View.OnClickListener() {
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

                //Prompt user to ensure they want to reset streak
                showResetStreakDialog();
            }
        });

        mNotesButton.setOnClickListener(new View.OnClickListener() {

            //When notes button is clicked, create new notes popup window to edit/save notes related
            //to the current streak

            @Override
            public void onClick(View view) {

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
                notesEditText.setText(mCurrentGoalNotes, TextView.BufferType.EDITABLE);

                // Set a click listener for the popup window close button
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        EditText notesEditText = (EditText) notesView.findViewById(R.id.notes_edit_text);

                        // Update the goal note with user note editText value
                        updateNote(notesEditText);

                        // Dismiss the popup window
                        mNotesPopupWindow.dismiss();
                    }
                });

                mNotesPopupWindow.setFocusable(true);
                mNotesPopupWindow.showAtLocation(mNotesButton, Gravity.CENTER, 0, 0);

            }
        });

        mViewStreakDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GoalEditorActivity.this, StreakDetailsActivity.class);

                // If the currentGoalID is not yet set by the loader, display a toast that data is loading and do not start intent
                if (mCurrentGoalID == -1) {
                    Toast.makeText(GoalEditorActivity.this, GoalEditorActivity.this.getResources().getString(R.string.please_wait_data_load),
                            Toast.LENGTH_SHORT);
                    return;
                }

                //Send over the currentGoalID so that the streak details activity can load streak details for that goal
                intent.putExtra(GoalsHabitsEntry._ID, mCurrentGoalID);
                intent.putExtra(LIFECYCLE_STREAK_LENGTH, mStreakLengthDays);

                startActivity(intent);
            }
        });

        mGoalCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //If goal complete button is pushed, create new dialog to prompt user if they are sure they would
                //like to complete the goal/habit

                //Dialog click listener to update the goal to be "complete" if user indicates they are sure
                //that they want to complete the goal/habit
                //Completed goals display on the goals list with a star icon
                DialogInterface.OnClickListener goalCompleteClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (completeOrRestartGoal()) {
                            finish();
                        }
                    }
                };

                showGoalCompleteDialog(goalCompleteClickListener);

            }
        });


    }

    private void showGoalCompleteDialog(DialogInterface.OnClickListener goalCompleteClickListener) {

        //Dialog box to prompt user if they are sure they would like to complete the goal/habit

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, ThemeHelper.getPopUpTheme()));
        //If the goal is complete, restart the goal and set dialogue text accordingly
        //If the goal is not complete, complete the goal and set the dialogue text accordingly
        if (mGoalComplete == GoalsHabitsEntry.GOAL_COMPLETED_YES) {
            if (mGoalOrHabit == GoalsHabitsEntry.HABIT) {
                builder.setMessage(R.string.restart_habit);
            } else {
                builder.setMessage(R.string.restart_goal);
            }
        } else if (mGoalOrHabit == GoalsHabitsEntry.HABIT) {
            builder.setMessage(R.string.complete_habit);
        } else {
            builder.setMessage(R.string.complete_goal);
        }
        builder.setPositiveButton(R.string.yes, goalCompleteClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private boolean completeOrRestartGoal() {

        if (!doAllFieldsContainData()) {
            return false;
        }

        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();

        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long endDate = dateToUnixTime(mEndYear, mEndMonth, mEndDay);
        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NAME, nameString);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, mGoalOrHabit);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_TYPE, mGoalType);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_END_DATE, endDate);

        // Determine if goal is already complete.  If goal is already complete, then restart the goal
        // and set goal  complete = no.  If goal is not complete, set goal complete = yes.
        if (mGoalComplete == GoalsHabitsEntry.GOAL_COMPLETED_YES) {
            values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_NO);
        } else {
            values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_YES);
        }


        int rowsUpdated = getContentResolver().update(mCurrentGoalUri, values, null, null);

        clearStartAndEndDates();

        return true;

    }

    private boolean doAllFieldsContainData() {

        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();

        //Ensure fields are properly defined before inserting or completing a goal
        if (nameString.isEmpty() || nameString == null || undefinedStartDate() || undefinedEndDate() || mGoalOrHabit == GoalsHabitsEntry.NEITHER) {
            Toast.makeText(this, this.getResources().getString(R.string.all_fields_populated), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;

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

        if (!doAllFieldsContainData()) {
            return false;
        }

        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();

        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long endDate = dateToUnixTime(mEndYear, mEndMonth, mEndDay);


        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_ORDER, mNumberOfGoals);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NAME, nameString);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, mGoalOrHabit);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_TYPE, mGoalType);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_END_DATE, endDate);
        values.put(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, GoalsHabitsEntry.GOAL_COMPLETED_NO);

        //Insert values into database
        Uri uri = getContentResolver().insert(GoalsHabitsEntry.CONTENT_URI, values);

        Toast.makeText(this, this.getResources().getString(R.string.goal_inserted), Toast.LENGTH_SHORT).show();

        clearStartAndEndDates();
        return true;

    }

    private boolean updateGoal() {

        if (!doAllFieldsContainData()) {
            return false;
        }

        //Get values from editor entry views
        String nameString = mNameEditText.getText().toString().trim();

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

        Toast.makeText(this, this.getResources().getString(R.string.goal_updated), Toast.LENGTH_SHORT).show();

        clearStartAndEndDates();

        return true;
    }

    public void deleteGoal() {
        // If Delete Button is clicked from within Editor Activity, goal is deleted.
        // Dialog is displayed confirming if user wants to delete goal
        showDeleteGoalDialog();
    }

    public boolean insertStreak() {

        //Ensure fields are properly defined before inserting a new streak
        if (undefinedStartDate() || undefinedEndDate()) {
            Toast.makeText(this, this.getResources().getString(R.string.date_fields_populated), Toast.LENGTH_SHORT).show();
            return false;
        }

        long startDate = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long endDate = dateToUnixTime(mEndYear, mEndMonth, mEndDay);
        long failDate = System.currentTimeMillis() / 1000;

        //If the failDate is set, set the fail date to be equal to user chosen value, otherwise use
        //the current date as defined above
        if (!undefinedFailDate()) {
            failDate = dateToUnixTime(mFailYear, mFailMonth, mFailDay);
        }


        ContentValues values = new ContentValues();
        values.put(StreaksEntry.COLUMN_PARENT_ID, mCurrentGoalID);
        values.put(StreaksEntry.COLUMN_STREAK_START_DATE, startDate);
        values.put(StreaksEntry.COLUMN_STREAK_END_DATE, endDate);
        values.put(StreaksEntry.COLUMN_STREAK_FAIL_DATE, failDate);
        values.put(StreaksEntry.COLUMN_STREAK_NOTES, mCurrentGoalNotes);

        //Insert values into database
        Uri uri = getContentResolver().insert(StreaksEntry.CONTENT_URI, values);

        Toast.makeText(this, "Streak Reset", Toast.LENGTH_SHORT).show();

        return true;


    }

    private void updateNote(EditText notesEditText) {

        // Get note string from edit text field
        String notesString = notesEditText.getText().toString().trim();

        // Update goal database table to contain user inputted note
        ContentValues values = new ContentValues();
        values.put(GoalsHabitsEntry.COLUMN_GOAL_NOTES, notesString);

        mLoadingNote = true;
        int rowsUpdated = getContentResolver().update(mCurrentGoalUri, values, null, null);

        Toast.makeText(this, this.getResources().getString(R.string.note_updated), Toast.LENGTH_SHORT).show();

        // Notify the content resolver that the Goal URI data has changed
        getContentResolver().notifyChange(mCurrentGoalUri, null);
    }

    private void setAddGoalWorkspace() {
        //Set up workspace and strings for Add Goal mode
        setTitle(R.string.add_goal_activity_title);
        mDeleteGoal.setVisibility(View.INVISIBLE);
        mGoalNameTextView.setText(R.string.goal_name);
        mGoalTypeTextView.setText(R.string.goal_type);
        mFailResetStreak.setVisibility(View.INVISIBLE);
        mGoalCompleted.setVisibility(View.INVISIBLE);
        mHistoricalStreaksHeader.setVisibility(View.INVISIBLE);
        mNotesButton.setVisibility(View.INVISIBLE);
        mViewStreakDetails.setVisibility(View.INVISIBLE);

    }

    private void setAddHabitWorkspace() {
        //Set up workspace and strings for Add Habit mode
        setTitle(R.string.add_habit_activity_title);
        mDeleteGoal.setVisibility(View.INVISIBLE);
        mGoalNameTextView.setText(R.string.habit_name);
        mGoalTypeTextView.setText(R.string.habit_type);
        mFailResetStreak.setVisibility(View.INVISIBLE);
        mGoalCompleted.setVisibility(View.INVISIBLE);
        mHistoricalStreaksHeader.setVisibility(View.INVISIBLE);
        mNotesButton.setVisibility(View.INVISIBLE);
        mViewStreakDetails.setVisibility(View.INVISIBLE);
    }

    private void setEditGoalWorkspace() {
        //Set up workspace and strings for Edit Goal mode
        setTitle(getString(R.string.edit_goal_activity_title));
        mDeleteGoal.setVisibility(View.VISIBLE);
        mFailResetStreak.setVisibility(View.VISIBLE);
        mGoalCompleted.setVisibility(View.VISIBLE);
        mNotesButton.setVisibility(View.VISIBLE);
    }

    private void setEditHabitWorkspace() {
        //Set up workspace and strings for Edit Habit mode
        setTitle(getString(R.string.edit_habit_activity_title));
        mDeleteGoal.setVisibility(View.VISIBLE);
        mGoalNameTextView.setText(R.string.habit_name);
        mGoalTypeTextView.setText(R.string.habit_type);
        mHistoricalStreaksHeader.setVisibility(View.VISIBLE);
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
                        GoalsHabitsEntry.COLUMN_GOAL_NOTES,
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

        //TODO  Figure out what to return in default scenario
        return null;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Load data for Goal and Streak loaders

        if (cursor.getCount() >= 1 && !mLoadingNote) {
            //If the cursor has data and you are not loading a note,  then the GOAL or STREAK LOADER will
            //be loaded below
            switch (loader.getId()) {
                case GOAL_EDIT_LOADER:
                    while (cursor.moveToNext()) {

                        //Load the current goal _ID, and once you have that info you can begin loading the Streak data for that goal
                        mCurrentGoalID = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry._ID));

                        // Load the data from the cursor for the single goal you are editing
                        String goalName = cursor.getString(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_NAME));
                        mGoalOrHabit = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT));
                        mCurrentGoalNotes = cursor.getString(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_NOTES));
                        int goalType = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_TYPE));
                        long startDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_START_DATE)) * 1000;
                        long endDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_END_DATE)) * 1000;
                        mGoalComplete = cursor.getInt(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED));
                        if (mGoalComplete == GoalsHabitsEntry.GOAL_COMPLETED_YES) {
                            mGoalCompleted.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_restart));
                        }

                        //Get data for streak length (info is passed in to streak details activity via intent
                        //so that current streak length can be added to graphView of streak length over time
                        long currentTimeMillis = System.currentTimeMillis();
                        long streakLengthMillis = currentTimeMillis - startDateMillis;
                        mStreakLengthDays = streakLengthMillis / (1000 * 60 * 60 * 24);


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

                    //Set the notes icon based on whether or not the note has text in it
                    setGoalNoteIcon();
                    //Load Streak Data after goal has been loaded so that you have the goal ID to load streak data for
                    getLoaderManager().initLoader(STREAK_LOADER, null, this);

                    //If Streak TextView is empty after app is unexpectedly terminated, then the STREAK LOADER must be restarted
                    if (mStreakDataTextView.getText().toString().isEmpty()) {
                        this.getLoaderManager().restartLoader(STREAK_LOADER, null, this);
                    }

                    break;
                case STREAK_LOADER:

                    String streakDetailString = "";
                    String streakLengthString = "";
                    mNumberOfStreaks = 0;
                    while (cursor.moveToNext()) {

                        mNumberOfStreaks++;

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

                        //Set streak details string
                        streakLengthString += Long.toString(streakLengthDays) + " days" + "\n";

                        //Set goal details string

                        streakDetailString += "" + startDateString + " ---> ";
                        streakDetailString += "" + failDateString + "\n";


                    }
                    mStreakDataTextView.setText(streakDetailString);
                    mStreakDataLengthTextView.setText(streakLengthString);
                    mViewStreakDetails.setVisibility(View.VISIBLE);
                    break;
            }
        } else if (!mLoadingNote) {

            //This condition will be hit if the cursor being loaded has a count of < 1 (i.e. no streak data)
            //Views will be customized and set accordingly to account for no streak data
            mViewStreakDetails.setVisibility(View.INVISIBLE);
            mStreakDataTextView.setText("No Historical Streak Data");
        } else {

            // Will hit this criteria if only loading notes for a particular goal.  When loading notes for a particular
            // goal, it is not necessary to reload the entire cursor
            while (cursor.moveToNext()) {
                mCurrentGoalNotes = cursor.getString(cursor.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_NOTES));
            }

            //Set the notes icon based on whether or not the note has text in it
            setGoalNoteIcon();
            mLoadingNote = false;
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
                mStreakDataLengthTextView.setText("");
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
            Toast.makeText(this, GoalEditorActivity.this.getResources().getString(R.string.end_date_after_start_date), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean failDateAfterStartDate() {
        //Check if goal end date is after start date, if so, make Toast.
        long startDateUnix = dateToUnixTime(mStartYear, mStartMonth, mStartDay);
        long failDateUnix = dateToUnixTime(mFailYear, mFailMonth, mFailDay);
        if (startDateUnix >= failDateUnix) {
            Toast.makeText(this, GoalEditorActivity.this.getResources().getString(R.string.fail_date_after_start_date), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showResetStreakDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, ThemeHelper.getPopUpTheme())).create();
        alertDialog.setTitle(GoalEditorActivity.this.getResources().getString(R.string.reset_streak_header));
        alertDialog.setMessage(GoalEditorActivity.this.getResources().getString(R.string.reset_streak_message));

        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.view_resetstreak_alertdialog, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        alertDialog.setView(dialogView);

        // Set the fail date display as either current time or user chosen time
        if (undefinedFailDate()) {
            long failDateMillis = System.currentTimeMillis();
            SimpleDateFormat failSdf = new SimpleDateFormat("MMMM d, yyyy");
            String failDateString = "";
            failDateString += failSdf.format(failDateMillis);
            TextView failDateDisplay = (TextView) dialogView.findViewById(R.id.failure_date_display);
            failDateDisplay.setText(failDateString);
        } else {
            long failDateMillis = dateToUnixTime(mFailYear, mFailMonth, mFailDay) * 1000;
            SimpleDateFormat failSdf = new SimpleDateFormat("MMMM d, yyyy");
            String failDateString = "";
            failDateString += failSdf.format(failDateMillis);
            Log.d(TAG, "fdate: " + failDateString);
            TextView failDateDisplay = (TextView) dialogView.findViewById(R.id.failure_date_display);
            failDateDisplay.setText(failDateString);
        }

        // Dismiss dialog if cancelled
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, GoalEditorActivity.this.getResources().getString(R.string.reset_streak_cancel_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Reset streak
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, GoalEditorActivity.this.getResources().getString(R.string.reset_streak_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //Reset streak.  This will save historical streak info and clear dates
                        //so that a new streak can be started
                        if (insertStreak()) {
                            clearFailDate();
                        }
                    }
                });

        // User can manually choose fail date, after chosen, alert dialog will be recreated
        // and user selected fail date will be displayed
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, GoalEditorActivity.this.getResources().getString(R.string.reset_streak_fail_date_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDateType = GOAL_FAIL_DATE;
                        DialogFragment newFragment = new DatePickerFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt(DATE_TYPE, mDateType);
                        bundle.putBoolean(START_DATE_SET, mStartDateSet);
                        bundle.putBoolean(END_DATE_SET, mEndDateSet);
                        newFragment.setArguments(bundle);
                        newFragment.show(getFragmentManager(), "datePicker");
                    }
                });
        alertDialog.show();
    }

    private void showDeleteGoalDialog() {

        // Dialog to confirm if user wants to delete goal

        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, ThemeHelper.getPopUpTheme())).create();
        alertDialog.setTitle(GoalEditorActivity.this.getResources().getString(R.string.delete_goal_header));
        alertDialog.setMessage(GoalEditorActivity.this.getResources().getString(R.string.delete_goal_message));

        // Dismiss dialog if cancelled
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, GoalEditorActivity.this.getResources().getString(R.string.delete_goal_cancel_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Delete goal
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, GoalEditorActivity.this.getResources().getString(R.string.delete_goal_delete_button),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        //Update Goal Order for other goals whose goal order are affected by deleted goal

                        //Query current goal info
                        String[] goalProjection = {
                                GoalsHabitsEntry._ID,
                                GoalsHabitsEntry.COLUMN_GOAL_ORDER};
                        Cursor deletedGoalDetails = getContentResolver().query(mCurrentGoalUri, goalProjection, null, null, null);
                        //Get the deleted goal order
                        Integer currentGoalOrder = -1;
                        if (deletedGoalDetails.getCount() >= 1) {
                            while (deletedGoalDetails.moveToNext()) {
                                currentGoalOrder = deletedGoalDetails.getInt(deletedGoalDetails.getColumnIndexOrThrow(GoalsHabitsEntry.COLUMN_GOAL_ORDER));
                            }
                        }


                        //Update goal order for goals whose order is greater than the order of the deleted goal
                        //For each goal whose goal order is greater than the deleted goal's order, subtract one
                        //from the goal's order to account for deleted goal and update the database
                        String selection = GoalsHabitsEntry.COLUMN_GOAL_ORDER + "=?";
                        String[] selectionArgs;
                        for (int firstGoalToUpdate = currentGoalOrder + 1; firstGoalToUpdate <= mNumberOfGoals; firstGoalToUpdate++) {
                            selectionArgs = new String[]{String.valueOf(firstGoalToUpdate)};
                            ContentValues values = new ContentValues();
                            values.put(GoalsHabitsEntry.COLUMN_GOAL_ORDER, firstGoalToUpdate - 1);
                            int rowsUpdated = getContentResolver().update(GoalsHabitsEntry.CONTENT_URI, values, selection, selectionArgs);

                        }
                        getContentResolver().notifyChange(GoalsHabitsEntry.CONTENT_URI, null);

                        //Delete the goal
                        int rowsDeleted = getContentResolver().delete(mCurrentGoalUri, null, null);
                        Toast.makeText(GoalEditorActivity.this, GoalEditorActivity.this.getResources().getString(R.string.goal_deleted), Toast.LENGTH_SHORT).show();
                        clearStartAndEndDates();
                        clearFailDate();
                        finish();
                    }
                });

        alertDialog.show();
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGoalHasChanged = true;
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        // If the goal hasn't changed, continue with handling back button press
        if (!mGoalHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, ThemeHelper.getPopUpTheme()));
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the goal.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        //Set date display when date set by DatePicker.  Determine if the date is a start date
        //or if the date is an end date and set the date integer values accordingly.
        if (mDateType == GOAL_START_DATE) {
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

        } else if (mDateType == GOAL_END_DATE) {
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
        } else if (mDateType == GOAL_FAIL_DATE) {
            mFailYear = year;
            mFailMonth = month;
            mFailDay = day;
            mFailDateSet = true;
            //If fail date is not after start date, display toast message and return
            if (mStartDateSet && !failDateAfterStartDate()) {
                clearFailDate();
                return;
            }

            //After choosing a fail date, show the reset streak dialog box to prompt user if they
            //want to reset streak
            showResetStreakDialog();


        } else {
            return;
        }


    }


    public static class DatePickerFragment extends DialogFragment {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int year;
            int month;
            int day;

            int dateType = getArguments().getInt(DATE_TYPE);
            boolean startDateSet = getArguments().getBoolean(START_DATE_SET);
            boolean endDateSet = getArguments().getBoolean(END_DATE_SET);

            //Set the default date in the datepicker depending on which datepicker was opened.
            //The information on the type of datepicker that was initialized is passed in the bundle.
            //Depending on the type of datepicker (dateType), the default year, month and day are set.
            if (dateType == GOAL_START_DATE && startDateSet) {
                year = getArguments().getInt(START_YEAR);
                month = getArguments().getInt(START_MONTH);
                day = getArguments().getInt(START_DAY);
            } else if (dateType == GOAL_END_DATE && endDateSet) {
                year = getArguments().getInt(END_YEAR);
                month = getArguments().getInt(END_MONTH);
                day = getArguments().getInt(END_DAY);
            } else {
                // Use the current date as the default date in the picker
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }


            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), ThemeHelper.getPopUpTheme(), (GoalEditorActivity) getActivity(), year, month, day);
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

    private void clearFailDate() {
        //Clear FailDateSet boolean and set dates to 0
        mFailDateSet = false;
        mFailYear = 0;
        mFailMonth = 0;
        mFailDay = 0;
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

    private boolean undefinedFailDate() {
        //Check if date is properly defined
        //Don't include fail month in check because it's possible for fail month to be 0 (January)
        if (mFailYear == 0 || mFailDay == 0) {
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

        // Save data if the app is ever terminated unexpectedly so that data can be reloaded when app
        // is restarted

        super.onSaveInstanceState(outState, outPersistentState);

        String goalName = mGoalNameTextView.getText().toString();
        int goalType = mGoalTypeSpinner.getSelectedItemPosition();
        String startDateString = mGoalStartDateDisplay.getText().toString();
        String endDateString = mGoalEndDateDisplay.getText().toString();
        String streakDataString = mStreakDataTextView.getText().toString();
        String streakDataLengthString = mStreakDataLengthTextView.getText().toString();
        if (mCurrentGoalUri != null) {
            String currentUri = mCurrentGoalUri.toString();
            outState.putString(LIFECYCLE_CURRENT_URI, currentUri);
        }

        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_NAME, goalName);
        outState.putInt(GoalsHabitsEntry.COLUMN_GOAL_TYPE, goalType);
        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDateString);
        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_END_DATE, endDateString);
        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_NOTES, mCurrentGoalNotes);
        outState.putInt(LIFECYCLE_START_YEAR_KEY, mStartYear);
        outState.putInt(LIFECYCLE_START_MONTH_KEY, mStartMonth);
        outState.putInt(LIFECYCLE_START_DAY_KEY, mStartDay);
        outState.putInt(LIFECYCLE_END_YEAR_KEY, mEndYear);
        outState.putInt(LIFECYCLE_END_MONTH_KEY, mEndMonth);
        outState.putInt(LIFECYCLE_END_DAY_KEY, mEndDay);
        outState.putInt(LIFECYCLE_GOAL_OR_HABIT, mGoalOrHabit);
        outState.putString(LIFECYCLE_STREAK_DATA, streakDataString);
        outState.putString(LIFECYCLE_STREAK_DATA_LENGTH, streakDataLengthString);
        outState.putInt(LIFECYCLE_CURRENT_GOAL_ID, mCurrentGoalID);
        outState.putInt(LIFECYCLE_NUMBER_OF_GOALS, mNumberOfGoals);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save data if the app is ever terminated unexpectedly so that data can be reloaded when app
        // is restarted

        String goalName = mGoalNameTextView.getText().toString();
        int goalType = mGoalTypeSpinner.getSelectedItemPosition();
        String startDateString = mGoalStartDateDisplay.getText().toString();
        String endDateString = mGoalEndDateDisplay.getText().toString();
        String streakDataString = mStreakDataTextView.getText().toString();
        String streakDataLengthString = mStreakDataLengthTextView.getText().toString();
        if (mCurrentGoalUri != null) {
            String currentUri = mCurrentGoalUri.toString();
            outState.putString(LIFECYCLE_CURRENT_URI, currentUri);
        }

        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_NAME, goalName);
        outState.putInt(GoalsHabitsEntry.COLUMN_GOAL_TYPE, goalType);
        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_START_DATE, startDateString);
        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_END_DATE, endDateString);
        outState.putString(GoalsHabitsEntry.COLUMN_GOAL_NOTES, mCurrentGoalNotes);
        outState.putInt(LIFECYCLE_START_YEAR_KEY, mStartYear);
        outState.putInt(LIFECYCLE_START_MONTH_KEY, mStartMonth);
        outState.putInt(LIFECYCLE_START_DAY_KEY, mStartDay);
        outState.putInt(LIFECYCLE_END_YEAR_KEY, mEndYear);
        outState.putInt(LIFECYCLE_END_MONTH_KEY, mEndMonth);
        outState.putInt(LIFECYCLE_END_DAY_KEY, mEndDay);
        outState.putInt(LIFECYCLE_GOAL_OR_HABIT, mGoalOrHabit);
        outState.putString(LIFECYCLE_STREAK_DATA, streakDataString);
        outState.putString(LIFECYCLE_STREAK_DATA_LENGTH, streakDataLengthString);
        outState.putInt(LIFECYCLE_CURRENT_GOAL_ID, mCurrentGoalID);
        outState.putInt(LIFECYCLE_NUMBER_OF_GOALS, mNumberOfGoals);
        outState.putLong(LIFECYCLE_STREAK_LENGTH, mStreakLengthDays);
        outState.putInt(GoalsHabitsEntry.COLUMN_GOAL_COMPLETED, mGoalComplete);

    }

    private void setGoalNoteIcon() {
        //Set the notes icon based on whether or not the note has text in it
        if (mCurrentGoalNotes.trim().isEmpty()) {
            mNotesButton.setBackgroundDrawable(ContextCompat.getDrawable(GoalEditorActivity.this, R.drawable.ic_blank_note));
        } else {
            mNotesButton.setBackgroundDrawable(ContextCompat.getDrawable(GoalEditorActivity.this, R.drawable.ic_notesvg));
        }
    }


}
