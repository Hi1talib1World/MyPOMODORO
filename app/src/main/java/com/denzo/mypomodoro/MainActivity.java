package com.denzo.mypomodoro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.denzo.mypomodoro.database.Activity;
import com.denzo.mypomodoro.database.Database;
import com.denzo.mypomodoro.settings.SettingsActivity;
import com.denzo.mypomodoro.statistics.StatisticsBottomSheet;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private long workTimeMs;
    private long shortBreakTimeMs;
    private long longBreakTimeMs;
    private long currentTimeLimitMs;
    private long timeCountInMilliSeconds;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private TextView activityLabelButton;
    private List<Activity> activityList;
    private ImageView imageViewReset;
    private ImageView imageViewStartStop;
    private ImageView imageViewRewind;
    private CountDownTimer countDownTimer;
    private RelativeLayout rootLayout; // Root layout to change background color
    private Toolbar toolbar; // Toolbar reference
    private TextView finishMessage; // TextView to show Pomodoro finished message

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar); // Toolbar initialization
        setSupportActionBar(toolbar);

        // Remove title from Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // Initialize views and listeners
        initViews();
        initListeners();

        FloatingActionButton mFab = findViewById(R.id.m_fab);
        mFab.setOnClickListener(v -> {
            StatisticsBottomSheet bottomSheet = new StatisticsBottomSheet();
            bottomSheet.show(getSupportFragmentManager(), "StatisticsBottomSheet");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadTimerSettings();
    }

    private void loadTimerSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        workTimeMs = prefs.getInt(Constants.WORK_DURATION_SETTING, 25) * 60 * 1000L;
        shortBreakTimeMs = prefs.getInt(Constants.BREAK_DURATION_SETTING, 5) * 60 * 1000L;
        longBreakTimeMs = prefs.getInt(Constants.LONG_BREAK_DURATION_SETTING, 20) * 60 * 1000L;

        int currentActivityId = prefs.getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        Database.databaseExecutor.execute(() -> {
            Database db = Database.getInstance(this);
            activityList = db.activityDao().getAll();
            
            String currentName = "Work";
            for (Activity a : activityList) {
                if (a.getId() == currentActivityId) {
                    currentName = a.getName();
                    break;
                }
            }
            
            final String fName = currentName;
            runOnUiThread(() -> {
                activityLabelButton.setText(fName);
                activityLabelButton.setOnClickListener(v -> showActivitySelectionDialog());
            });
        });

        if (timerStatus == TimerStatus.STOPPED) {
            currentTimeLimitMs = workTimeMs;
            timeCountInMilliSeconds = currentTimeLimitMs;
            textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
            setProgressBarValues();
        }
    }

    private void showActivitySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select or Add Activity");

        // Prepare the list of activity names
        String[] names = new String[activityList.size() + 1];
        for (int i = 0; i < activityList.size(); i++) {
            names[i] = activityList.get(i).getName();
        }
        names[activityList.size()] = "+ Add New Activity";

        builder.setItems(names, (dialog, which) -> {
            if (which == activityList.size()) {
                // Add New Activity option selected
                showAddActivityDialog();
            } else {
                // Existing activity selected
                Activity selected = activityList.get(which);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putInt(Constants.CURRENT_ACTIVITY_ID, selected.getId()).apply();
                activityLabelButton.setText(selected.getName());
            }
        });

        builder.show();
    }

    private void showAddActivityDialog() {
        NewBlockDialogFragment dialog = new NewBlockDialogFragment();
        dialog.setOnActivityAddedListener(this::loadTimerSettings);
        dialog.show(getSupportFragmentManager(), "NewBlockDialog");
    }

    private void initViews() {
        rootLayout = findViewById(R.id.activity_main); // Root layout
        progressBarCircle = findViewById(R.id.progressBarCircle);
        textViewTime = findViewById(R.id.textViewTime);
        activityLabelButton = findViewById(R.id.activity_label_button);
        imageViewReset = findViewById(R.id.Reset);
        imageViewStartStop = findViewById(R.id.imageViewStartStop);
        imageViewRewind = findViewById(R.id.imageViewRewind);
        finishMessage = findViewById(R.id.finishMessage); // Initialize finish message TextView
    }

    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
        imageViewRewind.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.Reset) {
            reset();
        } else if (id == R.id.imageViewStartStop) {
            startStop();
        } else if (id == R.id.imageViewRewind) {
            rewind();
        }
    }

    private void reset() {
        stopCountDownTimer();

        // Reset to values from settings
        loadTimerSettings();

        // Restore UI state
        imageViewStartStop.setImageResource(R.drawable.ic_play_button);

        // Reset background color for layout and toolbar to default
        rootLayout.setBackgroundColor(getResources().getColor(R.color.default_background)); // Default background
        toolbar.setBackgroundColor(getResources().getColor(R.color.default_toolbar)); // Default toolbar color
        finishMessage.setVisibility(View.GONE); // Hide finish message
        timerStatus = TimerStatus.STOPPED;
    }

    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {
            setProgressBarValues();
            imageViewStartStop.setImageResource(R.drawable.pause);

            // Change background color for layout and toolbar when play button is clicked
            rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#DA1E5B"));
            toolbar.setBackgroundColor(android.graphics.Color.parseColor("#DA1E5B"));

            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
            startPomodoroService();  // Start the foreground service
        } else {
            stopCountDownTimer();
            imageViewStartStop.setImageResource(R.drawable.ic_play_button);

            // Reset background color for layout and toolbar to default when stop button is clicked
            rootLayout.setBackgroundColor(getResources().getColor(R.color.default_background)); // Default background
            toolbar.setBackgroundColor(getResources().getColor(R.color.default_toolbar)); // Default toolbar color

            timerStatus = TimerStatus.STOPPED;
        }
    }

    private void rewind() {
        // Add 10 seconds (10,000 ms)
        timeCountInMilliSeconds += 10000;

        if (timeCountInMilliSeconds > currentTimeLimitMs) {
             currentTimeLimitMs = timeCountInMilliSeconds;
             progressBarCircle.setMax((int) (currentTimeLimitMs / 1000));
        }
        
        // Update UI immediately
        textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
        progressBarCircle.setProgress((int) (timeCountInMilliSeconds / 1000));

        // If timer is running, restart it with new time to sync
        if (timerStatus == TimerStatus.STARTED) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            startCountDownTimer();
            startPomodoroService();
        }
    }

    private void startPomodoroService() {
        Intent serviceIntent = new Intent(MainActivity.this, PomodoroService.class);
        serviceIntent.putExtra("TIME_REMAINING", timeCountInMilliSeconds);
        startService(serviceIntent);  // Start the service to show notification
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeCountInMilliSeconds = millisUntilFinished;
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                finishPomodoroSession(); // Call to display Pomodoro finished message
                reset(); // Reset timer after Pomodoro finishes
            }
        }.start();
    }

    private void stopCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Intent serviceIntent = new Intent(MainActivity.this, PomodoroService.class);
        stopService(serviceIntent);
    }

    private void finishPomodoroSession() {
        // Show Pomodoro finished message
        finishMessage.setVisibility(View.VISIBLE);

        // Hide the message after 3 seconds
        new Handler().postDelayed(() -> finishMessage.setVisibility(View.GONE), 3000);
    }

    private void setProgressBarValues() {
        progressBarCircle.setMax((int) (currentTimeLimitMs / 1000));
        progressBarCircle.setProgress((int) (timeCountInMilliSeconds / 1000));
    }

    private String hmsTimeFormatter(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu to show the three dots (overflow) menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Handle menu item clicks
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, "About Clicked", Toast.LENGTH_SHORT).show();
            // Start About Activity if needed
            Intent aboutIntent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(aboutIntent);
            return true;
        } else if (id == R.id.action_sign_in) {
            Toast.makeText(this, "Sign In Clicked", Toast.LENGTH_SHORT).show();
            // Start Sign In Activity
            Intent signInIntent = new Intent(this, SignInActivity.class);
            startActivity(signInIntent);
            return true;
        } else if (id == R.id.action_sign_up) {
            Toast.makeText(this, "Sign Up Clicked", Toast.LENGTH_SHORT).show();
            // Start Sign Up Activity
            Intent signUpIntent = new Intent(this, SignUpActivity.class);
            startActivity(signUpIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
