package com.denzo.mypomodoro.activities;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzo.mypomodoro.Constants;
import com.denzo.mypomodoro.MainActivity;

import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.TimerActionReceiver;
import com.denzo.mypomodoro.database.Activity;
import com.denzo.mypomodoro.database.Database;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;



import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    private final List<Activity> activities;
    private final SharedPreferences preferences;

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView activityName;
        final TextView activityPomos;
        final com.google.android.material.chip.Chip chipType;
        final com.google.android.material.chip.Chip chipProject;
        final ImageButton settingsButton;
        final android.widget.RadioButton taskRadio;
        final View progressDots;

        public ViewHolder(@NonNull View view) {
            super(view);
            activityName = view.findViewById(R.id.activity_name);
            activityPomos = view.findViewById(R.id.activity_pomos);
            chipType = view.findViewById(R.id.chip_type);
            chipProject = view.findViewById(R.id.chip_project);
            settingsButton = view.findViewById(R.id.settings_button);
            taskRadio = view.findViewById(R.id.task_radio);
            progressDots = view.findViewById(R.id.progress_dots);
        }
    }

    public ActivitiesAdapter(Context context, List<Activity> activities) {
        this.activities = activities;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activities.get(position);
        Context context = holder.activityName.getContext();

        holder.activityName.setText(activity.getName());

        if (holder.activityPomos != null) {
            holder.activityPomos.setText(String.format("%d POMOS", activity.getSessionsBeforeLongBreak()));
        }

        if (holder.chipType != null) {
            holder.chipType.setText(activity.getTaskType());
        }

        if (holder.chipProject != null) {
            holder.chipProject.setText(activity.getProjectName());
        }
        
        if (holder.progressDots != null && holder.progressDots instanceof android.widget.LinearLayout) {
            android.widget.LinearLayout dotsLayout = (android.widget.LinearLayout) holder.progressDots;
            dotsLayout.removeAllViews();
            
            int totalPomos = activity.getSessionsBeforeLongBreak();
            Database.databaseExecutor.execute(() -> {
                int completedToday = Database.getInstance(context).pomodoroDao()
                    .getCompletedWorksForDate(java.time.LocalDate.now().toString(), new int[]{activity.getId()});
                
                ((android.app.Activity)context).runOnUiThread(() -> {
                    for (int i = 0; i < totalPomos; i++) {
                        View dot = new View(context);
                        int size = (int) com.denzo.mypomodoro.Utility.convertDpToPixel(10, context);
                        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(size, size);
                        params.setMargins(4, 0, 4, 0);
                        dot.setLayoutParams(params);
                        dot.setBackgroundResource(R.drawable.drawable_circle_yellow);
                        if (i >= completedToday) {
                            dot.setAlpha(0.2f);
                        }
                        dotsLayout.addView(dot);
                    }
                });
            });
        }

        int currentActivityId = preferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);
        if (holder.taskRadio != null) {
            holder.taskRadio.setChecked(currentActivityId == activity.getId());
        }

        holder.itemView.setOnClickListener(view -> {
            int activityId = preferences.getInt(Constants.CURRENT_ACTIVITY_ID, 1);

            if (activityId == activity.getId()) {
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);

                if (preferences.getBoolean(Constants.AUTOMATICALLY_START_NEW_ACTIVITIES, false)) {
                    intent = new Intent(context, TimerActionReceiver.class);
                    intent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, activity.getId());
                    intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
                    context.sendBroadcast(intent);
                }
                return;
            }

            SharedPreferences.Editor editor = preferences.edit();

            int timeLeft = preferences.getInt(Constants.TIME_LEFT, 0);

            if (timeLeft == 0) {
                editor.putInt(Constants.CURRENT_ACTIVITY_ID, activity.getId());
                editor.apply();

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);

                if (preferences.getBoolean(Constants.AUTOMATICALLY_START_NEW_ACTIVITIES, false)) {
                    intent = new Intent(context, TimerActionReceiver.class);
                    intent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, activity.getId());
                    intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
                    context.sendBroadcast(intent);
                }
            } else {
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(context);
                dialog.setTitle(R.string.Confirmation);
                dialog.setMessage(R.string.activity_already_running_dialog_message);

                dialog.setPositiveButton(context.getString(R.string.OK), (dialog1, which) -> {
                    Intent intent = new Intent(context, TimerActionReceiver.class);
                    intent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, activityId);
                    intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_STOP);
                    context.sendBroadcast(intent);

                    editor.putInt(Constants.CURRENT_ACTIVITY_ID, activity.getId());
                    editor.apply();

                    intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);

                    if (preferences.getBoolean(Constants.AUTOMATICALLY_START_NEW_ACTIVITIES, false)) {
                        intent = new Intent(context, TimerActionReceiver.class);
                        intent.putExtra(Constants.CURRENT_ACTIVITY_ID_INTENT, activity.getId());
                        intent.putExtra(Constants.BUTTON_ACTION, Constants.BUTTON_START);
                        context.sendBroadcast(intent);
                    }
                });

                dialog.setNegativeButton(context.getString(R.string.cancel), (dialog1, which) -> dialog1.cancel());
                dialog.show();
            }
        });

        holder.settingsButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, ActivitySettings.class);
            intent.putExtra(Constants.ACTIVITY_NAME, activity.getName());
            intent.putExtra(Constants.ACTIVITY_ID_INTENT, activity.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }
}