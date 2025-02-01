package com.denzo.mypomodoro;

import android.content.Intent;
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

        // Apply fade-in animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);
        appName.startAnimation(fadeIn);

        // Delay before navigating to MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close splash screen
        }, SPLASH_DELAY);
    }
}
