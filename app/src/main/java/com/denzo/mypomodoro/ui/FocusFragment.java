package com.denzo.mypomodoro.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.denzo.mypomodoro.Constants;
import com.denzo.mypomodoro.NewBlockDialogFragment;
import com.denzo.mypomodoro.PomodoroService;
import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.database.Activity;
import com.denzo.mypomodoro.database.Database;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FocusFragment extends Fragment implements View.OnClickListener {

    private long workTimeMs;
    private long shortBreakTimeMs;
    private long longBreakTimeMs;
    private long currentTimeLimitMs;
    private long timeCountInMilliSeconds;

    private enum TimerStatus {
        STARTED,
        STOPPED
    }

    private enum TimerMode {
        FOCUS,
        SHORT_BREAK,
        LONG_BREAK
    }

    private TimerMode currentMode = TimerMode.FOCUS;
    private TimerStatus timerStatus = TimerStatus.STOPPED;
    private CircularProgressIndicator progressBarCircle;
    private TextView textViewTime;
    private TextView activityLabelButton;
    private TextView todayPomosCount;
    private List<Activity> activityList;
    private View imageViewReset;
    private Button imageViewStartStop;
    private MaterialButtonToggleGroup toggleGroup;
    private CountDownTimer countDownTimer;
    private TextView finishMessage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        initViews(view);
        initListeners();
        setupToggleGroup(view);

        loadTimerSettings();
        loadRealStats();

        return view;
    }

    private void loadRealStats() {
        if (!isAdded()) return;
        Database.databaseExecutor.execute(() -> {
            Database db = Database.getInstance(requireContext());
            int todaySessions = db.pomodoroDao().getCompletedWorksForDate(LocalDate.now().toString(), 
                db.activityDao().getIdsToShow());
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (todayPomosCount != null) {
                        todayPomosCount.setText(String.valueOf(todaySessions));
                    }
                });
            }
        });
    }

    private void initViews(View view) {
        progressBarCircle = view.findViewById(R.id.progressBarCircle);
        textViewTime = view.findViewById(R.id.textViewTime);
        activityLabelButton = view.findViewById(R.id.activity_label_button);
        todayPomosCount = view.findViewById(R.id.today_pomos_count);
        imageViewReset = view.findViewById(R.id.Reset);
        imageViewStartStop = view.findViewById(R.id.imageViewStartStop);
        finishMessage = view.findViewById(R.id.finishMessage);
    }

    private void initListeners() {
        if (imageViewReset != null) imageViewReset.setOnClickListener(this);
        if (imageViewStartStop != null) imageViewStartStop.setOnClickListener(this);
    }

    private void setupToggleGroup(View view) {
        toggleGroup = view.findViewById(R.id.toggleGroup);
        if (toggleGroup != null) {
            toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
                if (isChecked) {
                    if (checkedId == R.id.btn_focus) {
                        setTimerMode(TimerMode.FOCUS);
                    } else if (checkedId == R.id.btn_short_break) {
                        setTimerMode(TimerMode.SHORT_BREAK);
                    } else if (checkedId == R.id.btn_long_break) {
                        setTimerMode(TimerMode.LONG_BREAK);
                    }
                }
            });
        }
    }

    private void setTimerMode(TimerMode mode) {
        currentMode = mode;
        stopCountDownTimer();
        timerStatus = TimerStatus.STOPPED;
        if (imageViewStartStop != null) imageViewStartStop.setText("START SESSION");
        loadTimerSettings();
    }

    private void loadTimerSettings() {
        if (!isAdded()) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        
        switch (currentMode) {
            case FOCUS:
                workTimeMs = prefs.getInt(Constants.WORK_DURATION_SETTING, 25) * 60 * 1000L;
                currentTimeLimitMs = workTimeMs;
                break;
            case SHORT_BREAK:
                shortBreakTimeMs = prefs.getInt(Constants.BREAK_DURATION_SETTING, 5) * 60 * 1000L;
                currentTimeLimitMs = shortBreakTimeMs;
                break;
            case LONG_BREAK:
                longBreakTimeMs = prefs.getInt(Constants.LONG_BREAK_DURATION_SETTING, 20) * 60 * 1000L;
                currentTimeLimitMs = longBreakTimeMs;
                break;
        }

        int currentActivityId = prefs.getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        Database.databaseExecutor.execute(() -> {
            Database db = Database.getInstance(requireContext());
            activityList = db.activityDao().getAll();
            
            Activity currentActivity = null;
            for (Activity a : activityList) {
                if (a.getId() == currentActivityId) {
                    currentActivity = a;
                    break;
                }
            }
            
            final Activity finalActivity = currentActivity;
            if (getActivity() != null) {
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

        if (timerStatus == TimerStatus.STOPPED) {
            timeCountInMilliSeconds = currentTimeLimitMs;
            textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds));
            setProgressBarValues();
        }
    }

    private void showActivitySelectionDialog() {
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
                loadTimerSettings();
            }
        });

        builder.show();
    }

    private void showAddActivityDialog() {
        NewBlockDialogFragment dialog = new NewBlockDialogFragment();
        dialog.setOnActivityAddedListener(this::loadTimerSettings);
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
        syncWithRunningTimer();
    }

    private void syncWithRunningTimer() {
        if (!isAdded()) return;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isRunning = prefs.getBoolean(Constants.IS_TIMER_RUNNING, false);
        
        if (isRunning) {
            long endTime = prefs.getLong(Constants.TIMER_END_TIME, 0);
            currentTimeLimitMs = prefs.getLong(Constants.CURRENT_TIME_LIMIT, workTimeMs);
            long now = System.currentTimeMillis();
            long remaining = endTime - now;
            
            if (remaining > 0) {
                timeCountInMilliSeconds = remaining;
                timerStatus = TimerStatus.STARTED;
                if (imageViewStartStop != null) imageViewStartStop.setText("PAUSE");

                startCountDownTimer();
            } else {
                // Timer finished while app was closed
                prefs.edit().putBoolean(Constants.IS_TIMER_RUNNING, false).apply();
                reset();
            }
        }
    }

    private void reset() {
        stopCountDownTimer();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.edit().putBoolean(Constants.IS_TIMER_RUNNING, false).apply();
        
        loadTimerSettings();
        if (imageViewStartStop != null) imageViewStartStop.setText("START SESSION");
        if (finishMessage != null) finishMessage.setVisibility(View.GONE);
        timerStatus = TimerStatus.STOPPED;
    }

    private void startStop() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        if (timerStatus == TimerStatus.STOPPED) {
            setProgressBarValues();
            if (imageViewStartStop != null) imageViewStartStop.setText("PAUSE");
            timerStatus = TimerStatus.STARTED;
            
            // Save state for persistence
            long endTime = System.currentTimeMillis() + timeCountInMilliSeconds;
            prefs.edit()
                .putBoolean(Constants.IS_TIMER_RUNNING, true)
                .putLong(Constants.TIMER_END_TIME, endTime)
                .putLong(Constants.CURRENT_TIME_LIMIT, currentTimeLimitMs)
                .apply();
            
            startCountDownTimer();
            startPomodoroService();
        } else {
            stopCountDownTimer();
            if (imageViewStartStop != null) imageViewStartStop.setText("RESUME");
            timerStatus = TimerStatus.STOPPED;
            
            prefs.edit().putBoolean(Constants.IS_TIMER_RUNNING, false).apply();
        }
    }

    private void startPomodoroService() {
        Intent serviceIntent = new Intent(requireContext(), PomodoroService.class);
        serviceIntent.putExtra("TIME_REMAINING", timeCountInMilliSeconds);
        requireContext().startService(serviceIntent);
    }

    private void startCountDownTimer() {
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeCountInMilliSeconds = millisUntilFinished;
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished));
                
                int progress = (int) ((millisUntilFinished * 1000) / currentTimeLimitMs);
                progressBarCircle.setProgress(progress);
            }

            @Override
            public void onFinish() {
                progressBarCircle.setProgress(0);
                playSound();
                finishPomodoroSession();
                loadRealStats();
                reset();
            }
        }.start();
    }

    private void playSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mp = MediaPlayer.create(requireContext(), notification);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Intent serviceIntent = new Intent(requireContext(), PomodoroService.class);
        requireContext().stopService(serviceIntent);
    }

    private void finishPomodoroSession() {
        if (finishMessage != null) finishMessage.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            if (finishMessage != null) finishMessage.setVisibility(View.GONE);
        }, 3000);
    }

    private void setProgressBarValues() {
        progressBarCircle.setMax(1000);
        int progress = (int) ((timeCountInMilliSeconds * 1000) / currentTimeLimitMs);
        progressBarCircle.setProgress(progress);
    }

    private String hmsTimeFormatter(long milliSeconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
    }
}
