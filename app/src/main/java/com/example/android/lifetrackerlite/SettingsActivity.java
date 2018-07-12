package com.example.android.lifetrackerlite;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.lifetrackerlite.helper.ThemeHelper;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setUpSharedPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getWindow().setBackgroundDrawable(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_theme_key))) {
            //TODO only recreate the app  if the theme preference is changed
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
        }else if (theme.equals(getString(R.string.settings_theme_value_black))) {
            setTheme(R.style.BlackAppTheme);
            ThemeHelper.setTheme(R.style.BlackAppTheme);
        }else {
            setTheme(R.style.AppTheme);
            ThemeHelper.setTheme(R.style.AppTheme);
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
