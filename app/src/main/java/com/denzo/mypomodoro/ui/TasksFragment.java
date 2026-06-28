package com.denzo.mypomodoro.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzo.mypomodoro.Constants;
import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.activities.ActivitiesAdapter;
import com.denzo.mypomodoro.database.Activity;
import com.denzo.mypomodoro.database.Database;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class TasksFragment extends Fragment {

    private Database database;
    private ActivitiesAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_activities, container, false);

        database = Database.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.activities_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(false);

        View addActivity = view.findViewById(R.id.add_activity_button);
        addActivity.setOnClickListener(v -> showAddActivityDialog());

        loadActivities();

        return view;
    }

    private void loadActivities() {
        Database.databaseExecutor.execute(() -> {
            List<Activity> activityList = database.activityDao().getAll();
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getContext() != null) {
                        adapter = new ActivitiesAdapter(getContext(), activityList);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private void showAddActivityDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_activity, null);
        EditText nameEdit = dialogView.findViewById(R.id.edit_activity_name);
        EditText typeEdit = dialogView.findViewById(R.id.edit_task_type);
        EditText projectEdit = dialogView.findViewById(R.id.edit_project_name);
        EditText pomosEdit = dialogView.findViewById(R.id.edit_planned_pomos);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add New Activity")
                .setView(dialogView)
                .setPositiveButton(R.string.OK, (dialog, which) -> {
                    String name = nameEdit.getText().toString();
                    String type = typeEdit.getText().toString();
                    String project = projectEdit.getText().toString();
                    String pomosStr = pomosEdit.getText().toString();

                    if (name.isEmpty()) return;
                    if (type.isEmpty()) type = "Deep Work";
                    if (project.isEmpty()) project = "General";

                    int pomos = 4;
                    try {
                        if (!pomosStr.isEmpty()) pomos = Integer.parseInt(pomosStr);
                    } catch (NumberFormatException ignored) {}

                    final String finalType = type;
                    final String finalProject = project;
                    final int finalPomos = pomos;

                    Database.databaseExecutor.execute(() -> {
                        if (database.activityDao().isNameOccupied(name)) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() ->
                                        Toast.makeText(getContext(), R.string.activity_with_that_name_exists, Toast.LENGTH_LONG).show());
                            }
                        } else {
                            database.activityDao().insertActivity(new Activity(name,
                                    25, Constants.DEFAULT_BREAK_TIME, true,
                                    Constants.DEFAULT_LONG_BREAK_TIME, finalPomos,
                                    false, false, false, true, finalType, finalProject));
                            loadActivities();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }
}
