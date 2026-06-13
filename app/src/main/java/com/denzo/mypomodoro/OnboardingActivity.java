package com.denzo.mypomodoro;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Principal UIUX Architect - Onboarding Engine
 * Requirement 1: First-Run Guard & Session Persistence
 * Requirement 2: State-Driven Step Engine
 * Requirement 3: Micro-Feedback & Navigation Guards
 * Requirement 4: Structural Data Strategy
 */
public class OnboardingActivity extends AppCompatActivity {

    public static final String PREF_FIRST_RUN_COMPLETED = "is_first_run_completed";

    private ConstraintLayout rootLayout;
    private ImageView stepImage;
    private TextView stepTitle;
    private TextView stepDescription;
    private LinearLayout dotIndicatorContainer;
    private MaterialButton btnNext;
    private TextView btnSkip;
    private TextView btnBack;

    private int currentStepIndex = 0;
    private boolean isTransitioning = false;
    private final List<OnboardingStep> steps = new ArrayList<>();
    private final Handler transitionHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Requirement 1: Boot-time Interception
        if (isFirstRunCompleted()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_onboarding);
        initializeData();
        initializeViews();
        renderStep(false);
    }

    private boolean isFirstRunCompleted() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getBoolean(PREF_FIRST_RUN_COMPLETED, false);
    }

    private void completeOnboarding() {
        // Requirement 1: Permanent Dismissal (Asynchronous Disk Commit)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(PREF_FIRST_RUN_COMPLETED, true).apply();
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeData() {
        // Requirement 4: Structural Data Strategy & Payload Separation
        steps.add(new OnboardingStep(
                "Welcome to MyPOMODORO",
                "Master your productivity with the scientifically proven Pomodoro technique.",
                R.drawable.logo, // Reusing logo as placeholder
                0xFF24395B,
                0xFFFDFE97
        ));
        steps.add(new OnboardingStep(
                "Focus & Break",
                "Work for 25 minutes, then take a short 5-minute break. Repeat to maintain peak performance.",
                R.drawable.logo,
                0xFF24395B,
                0xFFFDFE97
        ));
        steps.add(new OnboardingStep(
                "Track Progress",
                "Visualize your daily achievements and keep your streak alive.",
                R.drawable.logo,
                0xFF24395B,
                0xFFFDFE97
        ));
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.onboarding_root);
        stepImage = findViewById(R.id.step_image);
        stepTitle = findViewById(R.id.step_title);
        stepDescription = findViewById(R.id.step_description);
        dotIndicatorContainer = findViewById(R.id.dot_indicator_container);
        btnNext = findViewById(R.id.btn_next);
        btnSkip = findViewById(R.id.btn_skip);
        btnBack = findViewById(R.id.btn_back);

        // Requirement 2: Event Logic for Next, Back, and Skip
        btnNext.setOnClickListener(v -> handleNext());
        btnSkip.setOnClickListener(v -> completeOnboarding());
        btnBack.setOnClickListener(v -> handleBack());

        setupDotIndicators();
    }

    private void setupDotIndicators() {
        dotIndicatorContainer.removeAllViews();
        for (int i = 0; i < steps.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(24, 24);
            params.setMargins(12, 0, 12, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive); // Need to create this
            dotIndicatorContainer.addView(dot);
        }
    }

    private void handleNext() {
        if (isTransitioning) return;

        if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            renderStep(true);
        } else {
            completeOnboarding();
        }
    }

    private void handleBack() {
        if (isTransitioning || currentStepIndex == 0) return;
        currentStepIndex--;
        renderStep(true);
    }

    private void renderStep(boolean animate) {
        // Requirement 3: Active Navigation Guards
        setNavigationEnabled(false);
        isTransitioning = true;

        OnboardingStep step = steps.get(currentStepIndex);

        if (animate) {
            // Requirement 3: Guarded Animations (Crossfade)
            rootLayout.animate().alpha(0.5f).setDuration(250).withEndAction(() -> {
                updateUI(step);
                rootLayout.animate().alpha(1.0f).setDuration(250).start();
            }).start();
        } else {
            updateUI(step);
        }

        // Requirement 3: Simulated Transition Channel (300ms-500ms)
        transitionHandler.postDelayed(() -> {
            isTransitioning = false;
            setNavigationEnabled(true);
        }, 500);
    }

    private void updateUI(OnboardingStep step) {
        stepTitle.setText(step.title);
        stepDescription.setText(step.description);
        stepImage.setImageResource(step.imageRes);
        
        // Requirement 2: Button Morphing Contracts
        if (currentStepIndex == steps.size() - 1) {
            btnNext.setText("GET STARTED");
        } else {
            btnNext.setText("NEXT");
        }

        btnBack.setVisibility(currentStepIndex > 0 ? View.VISIBLE : View.GONE);
        btnSkip.setVisibility(currentStepIndex < steps.size() - 1 ? View.VISIBLE : View.GONE);

        updateDots();
    }

    private void updateDots() {
        for (int i = 0; i < dotIndicatorContainer.getChildCount(); i++) {
            View dot = dotIndicatorContainer.getChildAt(i);
            dot.setBackgroundResource(i == currentStepIndex ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }

    private void setNavigationEnabled(boolean enabled) {
        btnNext.setEnabled(enabled);
        btnSkip.setEnabled(enabled);
        btnBack.setEnabled(enabled);
        btnNext.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**
     * Requirement 4: Payload Model
     */
    private static class OnboardingStep {
        final String title;
        final String description;
        @DrawableRes final int imageRes;
        final int backgroundColor;
        final int accentColor;

        OnboardingStep(String title, String description, int imageRes, int backgroundColor, int accentColor) {
            this.title = title;
            this.description = description;
            this.imageRes = imageRes;
            this.backgroundColor = backgroundColor;
            this.accentColor = accentColor;
        }
    }
}
