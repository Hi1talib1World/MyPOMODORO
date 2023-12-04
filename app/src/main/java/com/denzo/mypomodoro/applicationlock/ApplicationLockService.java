package com.denzo.mypomodoro.applicationlock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;

import androidx.preference.PreferenceManager;


import com.denzo.mypomodoro.Constants;
import com.denzo.mypomodoro.MainActivity;

import java.util.HashSet;
import java.util.Set;

public class ApplicationLockService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!sharedPreferences.getBoolean(Constants.MASTER_LOCK_APPLICATION_SETTING, false)) {
            return;
        }

        if (!sharedPreferences.getBoolean(Constants.IS_TIMER_RUNNING, false)) {
            return;
        }

        if (sharedPreferences.getBoolean(Constants.IS_BREAK_STATE, false)) {
            return;
        }

        String packageName = String.valueOf(event.getPackageName());

        Set<String> applicationList =
                sharedPreferences.getStringSet(Constants.LOCKED_APPLICATIONS_LIST, new HashSet<>());

        if (applicationList.contains(packageName)) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onInterrupt() {

    }
}