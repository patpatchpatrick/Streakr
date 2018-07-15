package com.patrickdoyle30.android.streakr;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.patrickdoyle30.android.streakr.data.LTContract.GoalsHabitsEntry;
import com.patrickdoyle30.android.streakr.helper.GoalItemTouchHelperCallback;
import com.patrickdoyle30.android.streakr.helper.ItemTouchHelperAdapter;
import com.patrickdoyle30.android.streakr.helper.OnStartDragListener;
import com.patrickdoyle30.android.streakr.helper.ThemeHelper;

import java.util.HashMap;

import static android.support.v7.widget.DividerItemDecoration.HORIZONTAL;

public class GoalsHabitsFeatureActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, GoalRecyclerAdapter.ListItemClickListener, OnStartDragListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = GoalsHabitsFeatureActivity.class.getSimpleName();

    private static final int GOALSHABITS_LOADER = 0;

    private InterstitialAd mInterstitial;
    private AdView mAdView;

    private RecyclerView mRecyclerView;
    private ImageView mEmptyViewArrow1;
    private ImageView mEmptyViewArrow2;
    private ImageView mStreakrLogo;
    private TextView mEmptyViewText1;
    private TextView mEmptyViewText2;
    private GoalRecyclerAdapter mAdapter;
    private ItemTouchHelper mItemtouchHelper;
    private Integer mNumberGoals = -2;
    private ImageView mSettingsButton;
    private boolean mShowCompletedGoals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set up the preferences/theme for the app
        setUpSharedPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals_habits_feature);
        getSupportActionBar().hide();
        //Set background drawable to null to increase performance (decrease overdraw) since we are drawing a background over it
        getWindow().setBackgroundDrawable(null);

        //Load the AdView to display banner advertisement
        AdRequest adRequestBanner = new AdRequest.Builder().build();
        mAdView = (AdView)this.findViewById(R.id.adView);
        mAdView.loadAd(adRequestBanner);

        mEmptyViewArrow1 = (ImageView) findViewById(R.id.recycler_empty_view_arrow_1);
        mEmptyViewArrow2 = (ImageView) findViewById(R.id.recycler_empty_view_arrow_2);
        mStreakrLogo = (ImageView) findViewById(R.id.streakr_logo);
        mEmptyViewText1 = (TextView) findViewById(R.id.recycler_empty_view_text_1);
        mEmptyViewText2 = (TextView) findViewById(R.id.recycler_empty_view_text_2);


        //Create and load a new interstitial ad (displayed in onListItemClickListener)
        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitial.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Toast.makeText(GoalsHabitsFeatureActivity.this,  "Ad Loaded", Toast.LENGTH_SHORT);
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();

            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitial.loadAd(adRequest);

        // Add new goal button
        FloatingActionButton mAddGoalButton = (FloatingActionButton) findViewById(R.id.add_goal);
        // If the black theme is being used, make the button icons dark
        mAddGoalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //New intent to open the editor activity in "insert mode"
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

                // If number of goals  is not fully loaded (i.e. is still the default of -2), return
                // If number of goals is loaded, pass over the data of number of cursor goals to editor activity
                //  This data is used to determine goal order (used for drag and drop functionality)
                if (mNumberGoals == -2) {
                    Toast.makeText(GoalsHabitsFeatureActivity.this, GoalsHabitsFeatureActivity.this.getResources().getString(R.string.please_wait_data_load),
                            Toast.LENGTH_SHORT);
                    return;
                }
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_ORDER, mNumberGoals);

                //Send over an extra int to indicate editing a goal
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.GOAL);
                startActivity(intent);

            }
        });

        // Add new habit button
        FloatingActionButton mAddHabitButton = (FloatingActionButton) findViewById(R.id.add_habit);


        mAddHabitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //New intent to open the editor activity in "insert mode"
                Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

                // If number of goals  is not fully loaded (i.e. is still the default of -2), return
                // If number of goals is loaded, pass over the data of number of cursor goals to editor activity
                //  This data is used to determine goal order (used for drag and drop functionality)
                if (mNumberGoals == -2) {
                    Toast.makeText(GoalsHabitsFeatureActivity.this, GoalsHabitsFeatureActivity.this.getResources().getString(R.string.please_wait_data_load),
                            Toast.LENGTH_SHORT);
                    return;
                }
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_ORDER, mNumberGoals);

                //Send over an extra int to indicate editing a habit
                intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT, GoalsHabitsEntry.HABIT);
                startActivity(intent);

            }
        });

        mSettingsButton = findViewById(R.id.settings_icon);

        // If dark theme is being used, set the floating action buttons to the dark drawable
        // Set other icons and text colors to black theme colors as well
        if (ThemeHelper.getTheme() == R.style.BlackAppTheme) {
            mAddGoalButton.setImageResource(R.drawable.ic_goal_black);
            mAddHabitButton.setImageResource(R.drawable.ic_habit_black);
            mSettingsButton.setImageResource(R.drawable.ic_settings_black);
            mEmptyViewText1.setTextColor(ContextCompat.getColor(this, R.color.colorTextAndIconsBlack));
            mEmptyViewText2.setTextColor(ContextCompat.getColor(this, R.color.colorTextAndIconsBlack));
            mEmptyViewArrow1.setImageResource(R.drawable.ic_down_arrow_black);
            mEmptyViewArrow2.setImageResource(R.drawable.ic_down_arrow_black);

        }
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startSettingsActivity = new Intent(GoalsHabitsFeatureActivity.this, SettingsActivity.class);
                startActivity(startSettingsActivity);
            }
        });

        // Find recyclerView for goal/habit list and set linearLayoutManager and recyclerAdapter on recyclerView
        mRecyclerView = findViewById(R.id.recycler_view_goals_habits);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(GoalsHabitsFeatureActivity.this));
        mAdapter = new GoalRecyclerAdapter(this, null, this, this);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback =
                new GoalItemTouchHelperCallback(mAdapter);
        mItemtouchHelper = new ItemTouchHelper(callback);
        mItemtouchHelper.attachToRecyclerView(mRecyclerView);

        //Initialize loader for goals data
        getLoaderManager().initLoader(GOALSHABITS_LOADER, null, this);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        //Load all goals data to display

        String[] projection = {
                GoalsHabitsEntry._ID,
                GoalsHabitsEntry.COLUMN_GOAL_ORDER,
                GoalsHabitsEntry.COLUMN_GOAL_NAME,
                GoalsHabitsEntry.COLUMN_GOAL_OR_HABIT,
                GoalsHabitsEntry.COLUMN_GOAL_TYPE,
                GoalsHabitsEntry.COLUMN_GOAL_START_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_END_DATE,
                GoalsHabitsEntry.COLUMN_GOAL_COMPLETED};

        if (mShowCompletedGoals == false) {

            //Query the table for only incomplete goals if the showCompleteGoals preference is set to false
            String selection = GoalsHabitsEntry.COLUMN_GOAL_COMPLETED + "=?";
            String[] selectionArgs = new String[]{String.valueOf(GoalsHabitsEntry.GOAL_COMPLETED_NO)};

            return new CursorLoader(this, GoalsHabitsEntry.CONTENT_URI, projection, selection, selectionArgs, GoalsHabitsEntry.COLUMN_GOAL_ORDER);

        } else {

            return new CursorLoader(this, GoalsHabitsEntry.CONTENT_URI, projection, null, null, GoalsHabitsEntry.COLUMN_GOAL_ORDER);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //Pass in the new cursor and whether or not completed goals should be shown
        mAdapter.swapCursor(cursor);
        mNumberGoals = cursor.getCount();

        if (mAdapter.getItemCount() <= 0) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mEmptyViewArrow1.setVisibility(View.VISIBLE);
            mEmptyViewArrow2.setVisibility(View.VISIBLE);
            mEmptyViewText1.setVisibility(View.VISIBLE);
            mEmptyViewText2.setVisibility(View.VISIBLE);
            mStreakrLogo.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyViewArrow1.setVisibility(View.INVISIBLE);
            mEmptyViewArrow2.setVisibility(View.INVISIBLE);
            mEmptyViewText1.setVisibility(View.INVISIBLE);
            mEmptyViewText2.setVisibility(View.INVISIBLE);
            mStreakrLogo.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mAdapter.swapCursor(null);

    }

    // When a goal/habit item is clicked, open up that goal/habit in "edit mode"
    @Override
    public void onListItemClick(int clickedGoalID) {

        // New intent to open GoalEditorActivity class for goal clicked
        Intent intent = new Intent(GoalsHabitsFeatureActivity.this, GoalEditorActivity.class);

        // Set the URI on the intent of the goal clicked to be the id of the clicked item
        Uri currentGoalUri = ContentUris.withAppendedId(GoalsHabitsEntry.CONTENT_URI, clickedGoalID);


        //Set the data on the intent to be the current URI
        intent.setData(currentGoalUri);

        // If number of goals  is not fully loaded (i.e. is still the default of -2), return
        // If number of goals is loaded, pass over the data of number of cursor goals to editor activity
        //  This data is used to determine goal order (used for drag and drop functionality)
        if (mNumberGoals == -2) {
            Toast.makeText(GoalsHabitsFeatureActivity.this, GoalsHabitsFeatureActivity.this.getResources().getString(R.string.please_wait_data_load),
                    Toast.LENGTH_SHORT);
            return;
        }
        intent.putExtra(GoalsHabitsEntry.COLUMN_GOAL_ORDER, mNumberGoals);

        // Launch the intent to the editor activity to open the activity in "edit mode"
        startActivity(intent);

        // Display the interstitial ad
        if (mInterstitial.isLoaded()){
            mInterstitial.show();
        }

    }

    // Start dragging the viewHolder when the startDrag callback is received from the GoalRecyclerAdapter
    // The drag is initiated when the view is touched
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemtouchHelper.startDrag(viewHolder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(GOALSHABITS_LOADER, null, this);
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitial.loadAd(adRequest);
    }


    private void setUpSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(sharedPreferences);
        mShowCompletedGoals = sharedPreferences.getBoolean(getString(R.string.pref_show_completed_goals_key),
                getResources().getBoolean(R.bool.pref_show_goals_default));
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_theme_key))) {
            //If the app theme is changed, the theme must be first set by the app and then the app
            //must be created for the new theme to be applied immediately
            setTheme(sharedPreferences);
            GoalsHabitsFeatureActivity.this.recreate();
        } else if (key.equals(getString(R.string.pref_show_completed_goals_key))) {
            mShowCompletedGoals = sharedPreferences.getBoolean(key,
                    getResources().getBoolean(R.bool.pref_show_goals_default));
            getLoaderManager().restartLoader(GOALSHABITS_LOADER, null, this);
        }

    }



    private void setTheme(SharedPreferences sharedPreferences) {

        //Set the app theme based on the theme selected in settings/preferences
        String theme = (sharedPreferences.getString(getString(R.string.settings_theme_key), getString(R.string.settings_theme_value_default)));
        if (theme.equals(getString(R.string.settings_theme_value_default))) {
            setTheme(R.style.AppTheme);
            ThemeHelper.setTheme(R.style.AppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_pink))) {
            setTheme(R.style.PinkAppTheme);
            ThemeHelper.setTheme(R.style.PinkAppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_blue))) {
            setTheme(R.style.BlueAppTheme);
            ThemeHelper.setTheme(R.style.BlueAppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_red))) {
            setTheme(R.style.RedAppTheme);
            ThemeHelper.setTheme(R.style.RedAppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_black))) {
            setTheme(R.style.BlackAppTheme);
            ThemeHelper.setTheme(R.style.BlackAppTheme);
        } else {
            setTheme(R.style.AppTheme);
            ThemeHelper.setTheme(R.style.AppTheme);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }


}