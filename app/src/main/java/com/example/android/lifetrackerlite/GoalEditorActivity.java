package com.example.android.lifetrackerlite;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.android.lifetrackerlite.data.LTContract;
import com.example.android.lifetrackerlite.data.LTContract.GoalsHabitsEntry;

import java.util.Calendar;

public class GoalEditorActivity extends AppCompatActivity {

    private Spinner mGoalTypeSpinner;

    private int mGoalType;

    //Variables for DatePicker date used for Goal Start Date
    private int mStartYear;
    private int mStartMonth;
    private int mStartDay;
    //Views for DatePicker used for Goal Start Date
    private TextView mDateDisplay;
    private Button mPickDate;

    static final int DATE_DIALOG_ID = 0;

    //Set input Goal Start Date variables based on date selection
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    mStartYear = year;
                    mStartMonth = monthOfYear;
                    mStartDay = dayOfMonth;
                    updateDisplay();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_editor);

        //Find views to read user input from
        mGoalTypeSpinner = (Spinner) findViewById(R.id.spinner_goal_type);
        setupSpinner();

        mDateDisplay = (TextView) findViewById(R.id.goal_start_date_display);
        mPickDate = (Button) findViewById(R.id.goal_start_date_button);

        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

        // Get current date
        final Calendar c = Calendar.getInstance();
        mStartYear = c.get(Calendar.YEAR);
        mStartMonth = c.get(Calendar.MONTH);
        mStartDay = c.get(Calendar.DAY_OF_MONTH);

        // Display current date
        updateDisplay();

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

    private void updateDisplay() {
        this.mDateDisplay.setText(
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mStartMonth + 1).append("-")
                        .append(mStartDay).append("-")
                        .append(mStartYear).append(" "));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                        mDateSetListener,
                        mStartYear, mStartMonth, mStartDay);
        }
        return null;
    }
}
