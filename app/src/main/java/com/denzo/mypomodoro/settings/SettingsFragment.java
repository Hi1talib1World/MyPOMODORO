package com.denzo.mypomodoro.settings;


import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.denzo.mypomodoro.Constants;
import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.applicationlock.ApplicationLockActivity;


public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals(Constants.APPLICATION_LOCK_PREFERENCE)) {
            Intent intent = new Intent(getContext(), ApplicationLockActivity.class);
            preference.setIntent(intent);
        }
        return super.onPreferenceTreeClick(preference);
    }
}
