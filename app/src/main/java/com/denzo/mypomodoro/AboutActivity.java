package com.denzo.mypomodoro;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        TextView applicationVersion = findViewById(R.id.about_application_version);
        applicationVersion.setText(String.format(getString(R.string.about_version_number), BuildConfig.VERSION_NAME));

        TextView viewSourceCode = findViewById(R.id.view_source_code);
        viewSourceCode.setOnClickListener(view -> {
            Intent openGithub = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.sourceCodeURL));
            startActivity(openGithub);
        });

        TextView sendFeedback = findViewById(R.id.send_feedback);
        sendFeedback.setOnClickListener(view -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                    Uri.parse(String.format(Constants.feedbackURL, getString(R.string.app_name))));

            startActivity(emailIntent);
        });

        TextView notificationsNotWorking = findViewById(R.id.notifications_not_working);
        notificationsNotWorking.setOnClickListener(view -> {
            MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.application_getting_killed_title)
                    .setMessage(R.string.application_getting_killed_message)
                    .setPositiveButton(R.string.open_website, (dialog, which) -> {
                        Intent openWebsite = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dontkillmyapp.com/"));
                        startActivity(openWebsite);
                    })
                    .setNegativeButton(R.string.cancel, null);

            dialogBuilder.show();
        });

        TextView showTutorial = findViewById(R.id.show_tutorial);
        showTutorial.setOnClickListener(view -> {
            SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(this).edit();
            preferences.putInt(Constants.TUTORIAL_STEP, 0);
            preferences.apply();

            Intent startMainActivity = new Intent(this, MainActivity.class);
            startActivity(startMainActivity);
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TextView showIgnoreOptimizationDialog = findViewById(R.id.show_ignore_optimization_dialog);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            String packageName = getPackageName();
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                showIgnoreOptimizationDialog.setVisibility(View.VISIBLE);
            }

            showIgnoreOptimizationDialog.setOnClickListener(view -> {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(Constants.NEVER_SHOW_IGNORE_BATTERY_OPTIMIZATION_DIALOG, false);
                editor.apply();

                Intent startMainActivity = new Intent(this, MainActivity.class);
                startActivity(startMainActivity);
            });
        }
    }
}