package com.example.android.lifetrackerlite;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView settingsTextViewTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settingsTextViewTest = (TextView) findViewById(R.id.settings_textview_test);

        setUpSharedPreferences();

        //Open Goals/Habits Activity if Goals/Habits view is clicked
        LinearLayout goalsFeatureView = (LinearLayout) findViewById(R.id.goals_feature);
        goalsFeatureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GoalsHabitsFeatureActivity.class);
                startActivity(intent);
            }
        });

        //Open Settings Activity if Settings view is clicked
        LinearLayout settingsView = (LinearLayout) findViewById(R.id.settings);
        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(startSettingsActivity);
            }
        });



    }

    private void setUpSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_theme_key))){
            setTheme(sharedPreferences);
        }

    }

    private void setTheme(SharedPreferences sharedPreferences) {

        //Set the app theme based on the theme selected in settings/preferences
        settingsTextViewTest.setText(sharedPreferences.getString(getString(R.string.settings_theme_key), getString(R.string.settings_theme_value_default)));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.primary_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.primary_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
