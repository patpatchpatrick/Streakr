package com.example.android.lifetrackerlite;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.lifetrackerlite.helper.ThemeHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = GoalEditorActivity.class.getSimpleName();

    private static final int STATE_SIGNED_IN = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private int mSignInProgress;

    private PendingIntent mSignInIntent;
    private int mSignInError;

    private static final int RC_SIGN_IN = 0;

    private static final int DIALOG_PLAY_SERVICES_ERROR = 0;

    private SignInButton mSignInButton;
    private Button mSignOutButton;
    private Button mRevokeButton;
    private TextView mSignInStatus;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set up the preferences/theme for the app
        setUpSharedPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setBackgroundDrawable(null);

        mSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        mSignOutButton = (Button) findViewById(R.id.google_sign_out_button);
        mRevokeButton = (Button) findViewById(R.id.google_revoke_access_button);
        mSignInStatus = (TextView) findViewById(R.id.sign_in_status);

        mSignInButton.setOnClickListener(this);
        mSignOutButton.setOnClickListener(this);
        mRevokeButton.setOnClickListener(this);

        buildApiClient();


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

    protected synchronized  void buildApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API,  Plus.PlusOptions.builder().build())
                .addScope(new Scope(Scopes.PROFILE))
                .build();
    }


    private void setUpSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_theme_key))){
            //TODO only recreate the app  if the theme preference is changed
            //If the app theme is changed, the theme must be first set by the app and then the app
            //must be created for the new theme to be applied immediately
            setTheme(sharedPreferences);
            MainActivity.this.recreate();
        }

    }

    private void setTheme(SharedPreferences sharedPreferences) {

        //Set the app theme based on the theme selected in settings/preferences
        String theme = (sharedPreferences.getString(getString(R.string.settings_theme_key), getString(R.string.settings_theme_value_default)));
        if (theme.equals(getString(R.string.settings_theme_value_default))){
            setTheme(R.style.AppTheme);
            ThemeHelper.setTheme(R.style.AppTheme);
        } else if (theme.equals(getString(R.string.settings_theme_value_pink))){
            setTheme(R.style.PinkAppTheme);
            ThemeHelper.setTheme(R.style.PinkAppTheme);
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


    //Google API methods:

    @Override
    public void onClick(View view) {
        if (!mGoogleApiClient.isConnecting()) {

            switch (view.getId()){
                case R.id.google_sign_in_button:
                    mSignInStatus.setText("Signing In");
                    resolveSignInError();
                    break;
                case R.id.google_sign_out_button:
                    //Clear default accounts for security purposes
                    //Then disconnect from API and reconnnect
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient.connect();
                    break;
                case R.id.google_revoke_access_button:
                    //Clear default account for security purposes
                    //Revoke access and disconnect
                    //Then create a new client and reconnect it
                    Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                    Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                    buildApiClient();
                    mGoogleApiClient.connect();
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Log that we are connected
        Log.i(TAG, "onConnected");

        // Disable sign-in button since we are already signed in
        // Enable sign-out and revoke access buttons
        mSignInButton.setEnabled(false);
        mSignOutButton.setEnabled(true);
        mRevokeButton.setEnabled(true);

        //Indicate that we are signed in
        mSignInProgress = STATE_SIGNED_IN;

        // Display that the user is signed in and display their display name
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        mSignInStatus.setText(String.format("Signed In to G+ as %s",  currentUser.getDisplayName()));

    }

    @Override
    public void onConnectionSuspended(int cause) {
        //Try to reconnect and also log the reason why connection was dropped
        mGoogleApiClient.connect();
        Log.i(TAG,  "onConnectionSuspended:"+cause);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {

        Log.i(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

        if (mSignInProgress != STATE_IN_PROGRESS) {
            //Get the error resolution intent from the connection result
            //Log the sign in error to variable mSignInError
            mSignInIntent =  result.getResolution();
            mSignInError = result.getErrorCode();

            if (mSignInProgress == STATE_SIGN_IN) {
                //User already clicked on the sign in button, so continue processing errors
                resolveSignInError();
            }

        }

        //If neither of the if statements are hit above, then the user is signed out
        onSignedOut();
    }

    private void resolveSignInError(){
        if (mSignInIntent != null) {
            //We have an intent which will allow us to resolve in error

            try{

                //Send the pending intent for the most recent OnConnectionFailed error.
                //The RC_SIGN_IN is a code that lets the system know this is a sign in error
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),  RC_SIGN_IN, null, 0, 0, 0);

            } catch (IntentSender.SendIntentException e) {
                Log.i(TAG, "Sign in intent could not be sent: " + e.getLocalizedMessage());
                //If there is an exception when sending the intent, log it and change the sign in progress
                //If there is an error, also attempt to reconnect the client
                mSignInProgress = STATE_SIGN_IN;
                mGoogleApiClient.connect();

            }
        } else {
            // If there is no sign in intent, show a dialog to indicate there was an error
            showDialog(DIALOG_PLAY_SERVICES_ERROR);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {
            case RC_SIGN_IN:
            if (resultCode == RESULT_OK){
                //Error was successful, but we may want to continue processing more errors
                mSignInProgress = STATE_SIGN_IN;
            } else {
                //We are signed in
                mSignInProgress = STATE_SIGNED_IN;
            }

            if (!mGoogleApiClient.isConnecting()) {
                //If we're still connecting, continue to connect
                mGoogleApiClient.connect();
            }
            break;

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onSignedOut(){

        //Enable sign in button and disable signed out and revoke buttons since we are signed out

        mSignInButton.setEnabled(true);
        mSignOutButton.setEnabled(false);
        mRevokeButton.setEnabled(false);
        mSignInStatus.setText("Signed Out");

    }

    /*  @Override
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
    }*/



}
