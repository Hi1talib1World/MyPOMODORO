package com.denzo.mypomodoro;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.denzo.mypomodoro.statistics.StatisticsBottomSheet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final long DEFAULT_TIME = 25 * 60 * 1000; // 25 minutes in milliseconds
    private long timeCountInMilliSeconds = DEFAULT_TIME;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private ProgressBar progressBarCircle;
    private TextView textViewTime;
    private ImageView imageViewReset;
    private ImageView imageViewStartStop;
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

    private void initViews() {
        rootLayout = findViewById(R.id.activity_main); // Root layout
        progressBarCircle = findViewById(R.id.progressBarCircle);
        textViewTime = findViewById(R.id.textViewTime);
        imageViewReset = findViewById(R.id.Reset);
        imageViewStartStop = findViewById(R.id.imageViewStartStop);
        finishMessage = findViewById(R.id.finishMessage); // Initialize finish message TextView

        // Set initial time on textView
        textViewTime.setText(hmsTimeFormatter(DEFAULT_TIME));
    }

    private void initListeners() {
        imageViewReset.setOnClickListener(this);
        imageViewStartStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.Reset:
                reset();
                break;
            case R.id.imageViewStartStop:
                startStop();
                break;
        }
    }

    private void reset() {
        stopCountDownTimer();

        // Reset to default values
        timeCountInMilliSeconds = DEFAULT_TIME;
        textViewTime.setText(hmsTimeFormatter(DEFAULT_TIME));
        progressBarCircle.setProgress((int) (DEFAULT_TIME / 1000));

        // Restore UI state
        imageViewReset.setVisibility(View.GONE);
        imageViewStartStop.setImageResource(R.drawable.icon_start);

        // Reset background color for layout and toolbar to default
        rootLayout.setBackgroundColor(getResources().getColor(R.color.default_background)); // Default background
        toolbar.setBackgroundColor(getResources().getColor(R.color.default_toolbar)); // Default toolbar color
        finishMessage.setVisibility(View.GONE); // Hide finish message
        timerStatus = TimerStatus.STOPPED;
    }

    private void startStop() {
        if (timerStatus == TimerStatus.STOPPED) {
            setProgressBarValues();
            imageViewReset.setVisibility(View.VISIBLE);
            imageViewStartStop.setImageResource(R.drawable.icon_stop);

            // Change background color for layout and toolbar when play button is clicked
            rootLayout.setBackgroundColor(android.graphics.Color.parseColor("#DA1E5B"));
            toolbar.setBackgroundColor(android.graphics.Color.parseColor("#DA1E5B"));

            timerStatus = TimerStatus.STARTED;
            startCountDownTimer();
            startPomodoroService();  // Start the foreground service
        } else {
            stopCountDownTimer();
            imageViewReset.setVisibility(View.GONE);
            imageViewStartStop.setImageResource(R.drawable.icon_start);

            // Reset background color for layout and toolbar to default when stop button is clicked
            rootLayout.setBackgroundColor(getResources().getColor(R.color.default_background)); // Default background
            toolbar.setBackgroundColor(getResources().getColor(R.color.default_toolbar)); // Default toolbar color

            timerStatus = TimerStatus.STOPPED;
        }
    }

    private void startPomodoroService() {
        Intent serviceIntent = new Intent(MainActivity.this, PomodoroService.class);
        startService(serviceIntent);  // Start the service to show notification
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
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
    }

    private void finishPomodoroSession() {
        // Show Pomodoro finished message
        finishMessage.setVisibility(View.VISIBLE);

        // Hide the message after 3 seconds
        new Handler().postDelayed(() -> finishMessage.setVisibility(View.GONE), 3000);
    }

    private void setProgressBarValues() {
        progressBarCircle.setMax((int) (DEFAULT_TIME / 1000));
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
            Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show();
            // Start Settings Activity if needed
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
