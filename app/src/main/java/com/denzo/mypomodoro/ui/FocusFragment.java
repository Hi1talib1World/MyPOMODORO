package com.denzo.mypomodoro.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.denzo.mypomodoro.Constants;
import com.denzo.mypomodoro.NewBlockDialogFragment;
import com.denzo.mypomodoro.PomodoroService;
import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.TimerStateManager;
import com.denzo.mypomodoro.Utility;
import com.denzo.mypomodoro.database.Activity;
import com.denzo.mypomodoro.database.Database;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.time.LocalDate;
import java.util.List;

/**
 * Pillar 3: Reactive UI & Interaction Interlocking.
 */
public class FocusFragment extends Fragment implements View.OnClickListener {

    private CircularProgressIndicator progressBarCircle;
    private TextView textViewTime;
    private TextView activityLabelButton;
    private TextView todayPomosCount;
    private TextView sessionsTodayLabel;
    private Button imageViewStartStop;
    private com.google.android.material.button.MaterialButton imageViewReset;
    private MaterialButtonToggleGroup toggleGroup;
    private TextView finishMessage;
    private View scrollRoot;
    
    private List<Activity> activityList;
    private TimerStateManager stateManager;
    private final Handler frameHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        stateManager = TimerStateManager.getInstance(requireContext());
        initViews(view);
        initListeners();
        
        // Pillar 1 & 3: Observe persistent state SSOT
        stateManager.getState().observe(getViewLifecycleOwner(), this::applyStateToUI);
        
        loadRealStats();
        loadCurrentActivity();

        return view;
    }

    private void initViews(View view) {
        scrollRoot = view.findViewById(R.id.focus_scroll_root);
        progressBarCircle = view.findViewById(R.id.progressBarCircle);
        textViewTime = view.findViewById(R.id.textViewTime);
        activityLabelButton = view.findViewById(R.id.activity_label_button);
        todayPomosCount = view.findViewById(R.id.today_pomos_count);
        
        // Find label to change color too
        View statsSummary = view.findViewById(R.id.stats_summary);
        if (statsSummary instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) statsSummary;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof TextView && child != todayPomosCount) {
                    sessionsTodayLabel = (TextView) child;
                    break;
                }
            }
        }

        imageViewReset = view.findViewById(R.id.Reset);
        imageViewStartStop = view.findViewById(R.id.imageViewStartStop);
        finishMessage = view.findViewById(R.id.finishMessage);
        toggleGroup = view.findViewById(R.id.toggleGroup);
    }

    private void initListeners() {
        if (imageViewReset != null) imageViewReset.setOnClickListener(this);
        if (imageViewStartStop != null) imageViewStartStop.setOnClickListener(this);
        
        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked && !stateManager.getCurrentState().isRunning) {
                    boolean isBreak = checkedId != R.id.btn_focus;
                    updateModeInState(isBreak);
                }
            });
        }
    }

    private void applyStateToUI(TimerStateManager.TimerState state) {
        // Interlocking: Disable mode toggle while running
        if (toggleGroup != null) {
            toggleGroup.setEnabled(!state.isRunning);
            toggleGroup.setAlpha(state.isRunning ? 0.5f : 1.0f);
            
            // Sync toggle group selection
            int targetId = state.isBreak ? R.id.btn_short_break : R.id.btn_focus;
            if (toggleGroup.getCheckedButtonId() != targetId) {
                toggleGroup.check(targetId);
            }
        }

        // Color Switching: Trigger ONLY when session is running
        // Idle is always brand Dark Blue
        boolean isRunningBreak = state.isRunning && state.isBreak;
        
        int bgColor = isRunningBreak ? 0xFFFDFE97 : 0xFF24395B; // Yellow if running break, otherwise Blue
        int textColor = isRunningBreak ? 0xFF24395B : 0xFFFDFE97; // Blue if running break, otherwise Yellow
        int secondaryText = isRunningBreak ? 0xFF3A4D6E : 0xFFA0B0D0;
        
        if (scrollRoot != null) scrollRoot.setBackgroundColor(bgColor);
        if (textViewTime != null) textViewTime.setTextColor(textColor);
        if (todayPomosCount != null) todayPomosCount.setTextColor(textColor);
        if (sessionsTodayLabel != null) sessionsTodayLabel.setTextColor(secondaryText);
        
        if (imageViewReset != null) {
            imageViewReset.setTextColor(isRunningBreak ? 0xFF24395B : 0xFFFFFFFF);
            imageViewReset.setIconTint(android.content.res.ColorStateList.valueOf(isRunningBreak ? 0xFF24395B : 0xFFFFFFFF));
        }

        if (progressBarCircle != null) {
            progressBarCircle.setIndicatorColor(textColor);
            progressBarCircle.setTrackColor(isRunningBreak ? 0xFFE0E0E0 : 0xFF3A4D6E);
        }

        imageViewStartStop.setText(state.isRunning ? "PAUSE" : "START SESSION");

        if (state.isRunning) {
            startLocalTimerLoop();
        } else {
            stopLocalTimerLoop();
            long remaining = state.endTime; // Paused state stores remaining ms in endTime
            textViewTime.setText(Utility.formatTime(remaining));
            updateProgress(remaining, state.totalLimitMs);
        }
    }

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            TimerStateManager.TimerState state = stateManager.getCurrentState();
            if (!state.isRunning) return;

            long remaining = state.endTime - System.currentTimeMillis();
            if (remaining > 0) {
                textViewTime.setText(Utility.formatTime(remaining));
                updateProgress(remaining, state.totalLimitMs);
                frameHandler.postDelayed(this, 500);
            } else {
                // Service handles completion logic, UI just observes
                loadRealStats();
            }
        }
    };

    private void updateProgress(long remaining, long total) {
        int progress = (int) ((remaining * 1000) / total);
        progressBarCircle.setProgress(progress);
    }

    private void startLocalTimerLoop() {
        frameHandler.removeCallbacks(timerRunnable);
        frameHandler.post(timerRunnable);
    }

    private void stopLocalTimerLoop() {
        frameHandler.removeCallbacks(timerRunnable);
    }

    private void updateModeInState(boolean isBreak) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int workDur = prefs.getInt(Constants.WORK_DURATION_SETTING, 25);
        int breakDur = prefs.getInt(Constants.BREAK_DURATION_SETTING, 5);
        
        // Force fix if stuck at 48 (or other values)
        if (!isBreak && workDur > 60) workDur = 25; 
        
        long limit = (isBreak ? breakDur : workDur) * 60000L;
        
        TimerStateManager.TimerState newState = new TimerStateManager.TimerState(false, limit, limit, isBreak, 0L);
        stateManager.commitState(newState);
    }

    private void startStop() {
        TimerStateManager.TimerState current = stateManager.getCurrentState();
        TimerStateManager.TimerState newState;
        
        long now = System.currentTimeMillis();
        
        if (current.isRunning) {
            // Save progress before pausing
            if (current.lastSaveTime > 0) {
                long delta = now - current.lastSaveTime;
                saveManualProgress(delta, current.isBreak);
            }
            
            long remaining = current.endTime - now;
            newState = new TimerStateManager.TimerState(false, remaining, current.totalLimitMs, current.isBreak, 0L);
            if (stateManager.commitState(newState)) {
                requireContext().stopService(new Intent(requireContext(), PomodoroService.class));
            } else {
                showRollbackError();
            }
        } else {
            long remaining = current.endTime > 0 ? current.endTime : current.totalLimitMs;
            long targetEndTime = now + remaining;
            // Start running, set lastSaveTime to now
            newState = new TimerStateManager.TimerState(true, targetEndTime, current.totalLimitMs, current.isBreak, now);
            if (stateManager.commitState(newState)) {
                Intent serviceIntent = new Intent(requireContext(), PomodoroService.class);
                requireContext().startService(serviceIntent);
            } else {
                showRollbackError();
            }
        }
    }

    private void reset() {
        TimerStateManager.TimerState current = stateManager.getCurrentState();
        
        if (current.isRunning && current.lastSaveTime > 0) {
            long delta = System.currentTimeMillis() - current.lastSaveTime;
            saveManualProgress(delta, current.isBreak);
        }

        TimerStateManager.TimerState newState = new TimerStateManager.TimerState(false, current.totalLimitMs, current.totalLimitMs, current.isBreak, 0L);
        if (stateManager.commitState(newState)) {
            requireContext().stopService(new Intent(requireContext(), PomodoroService.class));
            if (finishMessage != null) finishMessage.setVisibility(View.GONE);
        }
    }

    private void saveManualProgress(long deltaMs, boolean isBreak) {
        if (deltaMs < 1000) return; // Ignore less than 1 second
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int activityId = prefs.getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        
        if (isBreak) {
            Utility.updateDatabaseBreakTimeOnly(requireContext(), deltaMs, activityId);
        } else {
            Utility.updateDatabaseWorkTimeOnly(requireContext(), deltaMs, activityId);
        }
    }

    private void showRollbackError() {
        Toast.makeText(requireContext(), "Sync Error: Hardware rollback initiated.", Toast.LENGTH_SHORT).show();
    }

    private void loadRealStats() {
        if (!isAdded()) return;
        Database.databaseExecutor.execute(() -> {
            Database db = Database.getInstance(requireContext());
            int todaySessions = db.pomodoroDao().getCompletedWorksForDate(LocalDate.now().toString(), 
                db.activityDao().getIdsToShow());
            
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(() -> {
                if (todayPomosCount != null) {
                    todayPomosCount.setText(String.valueOf(todaySessions));
                }
            });
        });
    }

    private void loadCurrentActivity() {
        if (!isAdded()) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int activityId = prefs.getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        
        Database.databaseExecutor.execute(() -> {
            Database db = Database.getInstance(requireContext());
            activityList = db.activityDao().getAll();
            
            Activity current = null;
            for (Activity a : activityList) {
                if (a.getId() == activityId) {
                    current = a;
                    break;
                }
            }
            
            final Activity finalActivity = current;
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (finalActivity != null && activityLabelButton != null) {
                        activityLabelButton.setText(finalActivity.getName());
                        View view = getView();
                        if (view != null) {
                            com.google.android.material.chip.Chip typeChip = view.findViewById(R.id.chip_type);
                            com.google.android.material.chip.Chip projectChip = view.findViewById(R.id.chip_project);
                            if (typeChip != null) typeChip.setText(finalActivity.getTaskType());
                            if (projectChip != null) projectChip.setText(finalActivity.getProjectName());
                        }
                        activityLabelButton.setOnClickListener(v -> showActivitySelectionDialog());
                    }
                });
            }
        });
    }

    private void showActivitySelectionDialog() {
        if (activityList == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select or Add Activity");

        String[] names = new String[activityList.size() + 1];
        for (int i = 0; i < activityList.size(); i++) {
            names[i] = activityList.get(i).getName();
        }
        names[activityList.size()] = "+ Add New Activity";

        builder.setItems(names, (dialog, which) -> {
            if (which == activityList.size()) {
                showAddActivityDialog();
            } else {
                Activity selected = activityList.get(which);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                prefs.edit().putInt(Constants.CURRENT_ACTIVITY_ID, selected.getId()).apply();
                loadCurrentActivity();
            }
        });
        builder.show();
    }

    private void showAddActivityDialog() {
        NewBlockDialogFragment dialog = new NewBlockDialogFragment();
        dialog.setOnActivityAddedListener(this::loadCurrentActivity);
        dialog.show(getParentFragmentManager(), "NewBlockDialog");
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.Reset) {
            reset();
        } else if (id == R.id.imageViewStartStop) {
            startStop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRealStats();
    }
}
