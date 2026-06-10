package com.denzo.mypomodoro.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.denzo.mypomodoro.Constants;
import com.denzo.mypomodoro.R;
import com.google.android.material.slider.Slider;
import com.google.android.material.materialswitch.MaterialSwitch;

public class CustomSettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private TextView focusVal, shortBreakVal, longBreakVal;
    private Slider focusSlider, shortSlider, longSlider;
    private MaterialSwitch autoStartSwitch, notificationSwitch, darkModeSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_settings_custom, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        initViews(view);
        loadSettings();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        focusVal = view.findViewById(R.id.focus_duration_value);
        shortBreakVal = view.findViewById(R.id.short_break_duration_value);
        longBreakVal = view.findViewById(R.id.long_break_duration_value);

        focusSlider = view.findViewById(R.id.focus_slider);
        shortSlider = view.findViewById(R.id.short_break_slider);
        longSlider = view.findViewById(R.id.long_break_slider);

        autoStartSwitch = view.findViewById(R.id.auto_start_switch);
        notificationSwitch = view.findViewById(R.id.notification_switch);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);

        view.findViewById(R.id.privacy_policy_row).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            startActivity(intent);
        });

        view.findViewById(R.id.btn_reset_all).setOnClickListener(v -> {
            prefs.edit().clear().apply();
            loadSettings();
            Toast.makeText(requireContext(), "Settings Reset", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSettings() {
        try {
            android.content.pm.PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            View view = getView();
            if (view != null) {
                ((TextView) view.findViewById(R.id.version_text)).setText(pInfo.versionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int focus = prefs.getInt(Constants.WORK_DURATION_SETTING, 25);
        int shortB = prefs.getInt(Constants.BREAK_DURATION_SETTING, 5);
        int longB = prefs.getInt(Constants.LONG_BREAK_DURATION_SETTING, 15);

        focusSlider.setValue(focus);
        shortSlider.setValue(shortB);
        longSlider.setValue(longB);

        focusVal.setText(focus + "m");
        shortBreakVal.setText(shortB + "m");
        longBreakVal.setText(longB + "m");

        autoStartSwitch.setChecked(prefs.getBoolean(Constants.AUTOMATICALLY_START_NEW_ACTIVITIES, false));
        darkModeSwitch.setChecked(prefs.getBoolean(Constants.FULL_SCREEN_MODE, false));
    }

    private void setupListeners() {
        focusSlider.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            focusVal.setText(val + "m");
            prefs.edit().putInt(Constants.WORK_DURATION_SETTING, val).apply();
        });

        shortSlider.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            shortBreakVal.setText(val + "m");
            prefs.edit().putInt(Constants.BREAK_DURATION_SETTING, val).apply();
        });

        longSlider.addOnChangeListener((slider, value, fromUser) -> {
            int val = (int) value;
            longBreakVal.setText(val + "m");
            prefs.edit().putInt(Constants.LONG_BREAK_DURATION_SETTING, val).apply();
        });

        autoStartSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(Constants.AUTOMATICALLY_START_NEW_ACTIVITIES, isChecked).apply();
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(Constants.FULL_SCREEN_MODE, isChecked).apply();
        });
    }
}
