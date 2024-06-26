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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;



import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ViewHolder> {

    private final List<Activity> activities;
    private final SharedPreferences preferences;

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView activityName;
        final ImageButton settingsButton;

        public ViewHolder(@NonNull View view) {
            super(view);
            activityName = view.findViewById(R.id.activity_name);
            settingsButton = view.findViewById(R.id.settings_button);
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