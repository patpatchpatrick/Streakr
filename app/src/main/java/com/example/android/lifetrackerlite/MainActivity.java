package com.example.android.lifetrackerlite;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Open Goals/Habits Activity if Goals/Habits view is clicked
        LinearLayout goalsFeatureView = (LinearLayout) findViewById(R.id.goals_feature);
        goalsFeatureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GoalsHabitsFeatureActivity.class);
                startActivity(intent);
            }
        });
    }
}
