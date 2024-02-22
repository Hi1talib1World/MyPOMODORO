package com.denzo.mypomodoro.settings;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.denzo.mypomodoro.MainActivity;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        // Makes back button work like navigation up - onCreate() from MainActivity is called. Without it, if the user
        // had full screen mode on and turned it off, the layout wouldn't go back its place properly.
        navigateUpTo(new Intent(this, MainActivity.class));
    }
}