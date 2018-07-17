package com.patrickdoyle30.android.streakr;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.patrickdoyle30.android.streakr.helper.PreferenceHelper;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpSharedPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().setBackgroundDrawable(null);
        getSupportActionBar().hide();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_theme_key))) {
            //If the app theme is changed, the theme must be first set by the app and then the app
            //must be created for the new theme to be applied immediately
            setTheme(sharedPreferences);
            SettingsActivity.this.recreate();
        }
    }

    private void setTheme(SharedPreferences sharedPreferences) {

        //Set the app theme based on the theme selected in settings/preferences
        String theme = (sharedPreferences.getString(getString(R.string.settings_theme_key), getString(R.string.settings_theme_value_default)));
        if (theme.equals(getString(R.string.settings_theme_value_default))) {
            setTheme(R.style.AppTheme);
            PreferenceHelper.setTheme(R.style.AppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_pink))) {
            setTheme(R.style.PinkAppTheme);
            PreferenceHelper.setTheme(R.style.PinkAppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_blue))) {
            setTheme(R.style.BlueAppTheme);
            PreferenceHelper.setTheme(R.style.BlueAppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_red))) {
            setTheme(R.style.RedAppTheme);
            PreferenceHelper.setTheme(R.style.RedAppTheme);
        }else if (theme.equals(getString(R.string.settings_theme_value_black))) {
            setTheme(R.style.BlackAppTheme);
            PreferenceHelper.setTheme(R.style.BlackAppTheme);
        }else {
            setTheme(R.style.AppTheme);
            PreferenceHelper.setTheme(R.style.AppTheme);
        }

    }

    private void setUpSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
