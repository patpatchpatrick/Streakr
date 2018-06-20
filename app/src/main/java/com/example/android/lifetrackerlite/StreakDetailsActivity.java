package com.example.android.lifetrackerlite;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class StreakDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_details);
        setTitle(R.string.streak_details);
    }
}
