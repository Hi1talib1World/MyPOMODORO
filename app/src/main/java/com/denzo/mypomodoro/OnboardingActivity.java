package com.denzo.mypomodoro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Principal UIUX Architect - Production-Ready Onboarding Engine.
 * 
 * Requirement 1: First-Run Guard & Session Persistence.
 * Requirement 2: State-Driven Step Engine & UI Passive Observables.
 * Requirement 3: Micro-Feedback, Step Locks, & Guarded Animations.
 * Requirement 4: Structural Data Strategy & Payload Separation.
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
        
        // Requirement 1: On-Launch Interception (Guard)
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
        // Requirement 1: Permanent Dismissal & Async Disk Commit
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
        // Requirement 4: Zero Hardcoding - Isolated Content Payload
        steps.add(new OnboardingStep(
                R.string.onboarding_step1_title,
                R.string.onboarding_step1_desc,
                R.drawable.logo,
                0xFF24395B, // Brand Blue
                0xFFFDFE97  // Brand Yellow
        ));
        steps.add(new OnboardingStep(
                R.string.onboarding_step2_title,
                R.string.onboarding_step2_desc,
                R.drawable.logo,
                0xFF3A4D6E, // Brand Blue Card
                0xFFFDFE97
        ));
        steps.add(new OnboardingStep(
                R.string.onboarding_step3_title,
                R.string.onboarding_step3_desc,
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

        // Requirement 2: Explicit Event Logic Handlers
        btnNext.setOnClickListener(v -> handleNext());
        btnSkip.setOnClickListener(v -> completeOnboarding());
        btnBack.setOnClickListener(v -> handleBack());

        setupDotIndicators();
    }

    private void setupDotIndicators() {
        dotIndicatorContainer.removeAllViews();
        for (int i = 0; i < steps.size(); i++) {
            View dot = new View(this);
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(size / 2, 0, size / 2, 0);
            dot.setLayoutParams(params);
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
        // Requirement 3: Active Navigation Guards (Freeze Input)
        setNavigationEnabled(false);
        isTransitioning = true;

        OnboardingStep step = steps.get(currentStepIndex);

        if (animate) {
            // Requirement 3: Elegant Step-to-Step Transitions
            rootLayout.animate().alpha(0f).setDuration(250).withEndAction(() -> {
                updateUI(step);
                rootLayout.animate().alpha(1.0f).setDuration(250).start();
            }).start();
        } else {
            updateUI(step);
        }

        // Requirement 3: 500ms Transition Channel Lock
        transitionHandler.postDelayed(() -> {
            isTransitioning = false;
            setNavigationEnabled(true);
        }, 500);
    }

    private void updateUI(OnboardingStep step) {
        // Requirement 4: Reactive Layout Variable Binding
        stepTitle.setText(step.titleRes);
        stepDescription.setText(step.descriptionRes);
        stepImage.setImageResource(step.imageRes);
        rootLayout.setBackgroundColor(step.backgroundColor);
        
        // Requirement 2: Button Morphing Contract
        if (currentStepIndex == steps.size() - 1) {
            btnNext.setText(R.string.onboarding_get_started);
        } else {
            btnNext.setText(R.string.onboarding_next);
        }

        btnBack.setVisibility(currentStepIndex > 0 ? View.VISIBLE : View.GONE);
        btnSkip.setVisibility(currentStepIndex < steps.size() - 1 ? View.VISIBLE : View.GONE);

        btnNext.setBackgroundTintList(ColorStateList.valueOf(step.accentColor));

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
        // Visual feedback for disabled state
        float alpha = enabled ? 1.0f : 0.6f;
        btnNext.setAlpha(alpha);
        btnSkip.setAlpha(alpha);
        btnBack.setAlpha(alpha);
    }

    /**
     * Requirement 4: Immutable Data Model for Payload Separation.
     */
    private static class OnboardingStep {
        @StringRes final int titleRes;
        @StringRes final int descriptionRes;
        @DrawableRes final int imageRes;
        final int backgroundColor;
        final int accentColor;

        OnboardingStep(@StringRes int titleRes, @StringRes int descriptionRes, 
                       @DrawableRes int imageRes, int backgroundColor, int accentColor) {
            this.titleRes = titleRes;
            this.descriptionRes = descriptionRes;
            this.imageRes = imageRes;
            this.backgroundColor = backgroundColor;
            this.accentColor = accentColor;
        }
    }
}
