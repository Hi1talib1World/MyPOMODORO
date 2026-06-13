package com.denzo.mypomodoro;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

/**
 * Pillar 1: Single Source of Truth & Permanent Cache.
 */
public class TimerStateManager {
    private static TimerStateManager instance;
    private final SharedPreferences prefs;
    private final MutableLiveData<TimerState> stateStream = new MutableLiveData<>();

    private TimerStateManager(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        hydrate();
    }

    public static synchronized TimerStateManager getInstance(Context context) {
        if (instance == null) {
            instance = new TimerStateManager(context);
        }
        return instance;
    }

    private void hydrate() {
        boolean isRunning = prefs.getBoolean(Constants.IS_TIMER_RUNNING, false);
        long initialTotalLimit = prefs.getLong(Constants.CURRENT_TIME_LIMIT, 1500000L);
        boolean isBreak = prefs.getBoolean(Constants.IS_BREAK_STATE, false);
        long endTime = prefs.getLong(Constants.TIMER_END_TIME, initialTotalLimit);

        long finalTotalLimit = initialTotalLimit;
        long finalEndTime = endTime;

        // Safety: If not running, ensure duration matches current settings
        if (!isRunning) {
            int workDur = prefs.getInt(Constants.WORK_DURATION_SETTING, 25);
            int breakDur = prefs.getInt(Constants.BREAK_DURATION_SETTING, 5);
            finalTotalLimit = (isBreak ? breakDur : workDur) * 60000L;
            finalEndTime = finalTotalLimit;
        }

        TimerState snapshot = new TimerState(isRunning, finalEndTime, finalTotalLimit, isBreak);
        stateStream.postValue(snapshot);
    }

    public LiveData<TimerState> getState() {
        return stateStream;
    }

    public TimerState getCurrentState() {
        return stateStream.getValue() != null ? stateStream.getValue() : new TimerState(false, 0, 1500000L, false);
    }

    /**
     * Pillar 2 & 4: Atomic Mutations & Transaction Safeguards.
     */
    public synchronized boolean commitState(@NonNull TimerState newState) {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.IS_TIMER_RUNNING, newState.isRunning);
            editor.putLong(Constants.TIMER_END_TIME, newState.endTime);
            editor.putLong(Constants.CURRENT_TIME_LIMIT, newState.totalLimitMs);
            editor.putBoolean(Constants.IS_BREAK_STATE, newState.isBreak);
            
            if (editor.commit()) { // Atomic synchronous write
                stateStream.postValue(newState);
                return true;
            }
            return false;
        } catch (Exception e) {
            hydrate(); // Rollback to last known good disk state
            return false;
        }
    }

    public static class TimerState {
        public final boolean isRunning;
        public final long endTime;
        public final long totalLimitMs;
        public final boolean isBreak;

        public TimerState(boolean isRunning, long endTime, long totalLimitMs, boolean isBreak) {
            this.isRunning = isRunning;
            this.endTime = endTime;
            this.totalLimitMs = totalLimitMs;
            this.isBreak = isBreak;
        }
    }
}
