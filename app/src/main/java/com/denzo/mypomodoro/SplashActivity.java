package com.denzo.mypomodoro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fullscreen mode (Hides status bar)
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set the splash screen layout
        setContentView(R.layout.activity_splash);

        // Initialize UI elements
        ImageView logo = findViewById(R.id.logo);
        TextView appName = findViewById(R.id.app_name);
        android.widget.ProgressBar progressBar = findViewById(R.id.splash_progress);

        // Apply fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);
        appName.startAnimation(fadeIn);
        if (progressBar != null) {
            progressBar.startAnimation(fadeIn);
        }

        // Delay before navigating to MainActivity
        createNotificationChannels();
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close splash screen
        }, SPLASH_DELAY);
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                // Ongoing Timer Channel
                NotificationChannel timerChannel = new NotificationChannel(
                        Constants.CHANNEL_TIMER,
                        "Timer",
                        NotificationManager.IMPORTANCE_LOW
                );
                timerChannel.setSound(null, null);
                timerChannel.enableVibration(false);
                notificationManager.createNotificationChannel(timerChannel);

                // Timer Completed Channel
                NotificationChannel completedChannel = new NotificationChannel(
                        Constants.CHANNEL_TIMER_COMPLETED,
                        "Timer Completed",
                        NotificationManager.IMPORTANCE_LOW
                );
                completedChannel.setSound(null, null);
                notificationManager.createNotificationChannel(completedChannel);
            }
        }
    }
}
