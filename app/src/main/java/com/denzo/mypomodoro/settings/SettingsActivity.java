package com.denzo.mypomodoro.settings;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.denzo.mypomodoro.MainActivity;


import com.denzo.mypomodoro.MainActivity;
import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.activities.Activities;
import com.denzo.mypomodoro.statistics.StatisticsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_nav);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_focus) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_tasks) {
                startActivity(new Intent(this, Activities.class));
                return true;
            } else if (itemId == R.id.nav_stats) {
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        // Makes back button work like navigation up - onCreate() from MainActivity is called. Without it, if the user
        // had full screen mode on and turned it off, the layout wouldn't go back its place properly.
        navigateUpTo(new Intent(this, MainActivity.class));
    }
}