package com.denzo.mypomodoro;

import android.app.Dialog;
import android.content.Context;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.denzo.mypomodoro.database.Activity;
import com.denzo.mypomodoro.database.Database;

public class NewBlockDialogFragment extends DialogFragment {

    private EditText nameInput;
    private EditText sessionInput;
    private TextView durationText;
    private int selectedDurationMins = 25;
    private OnActivityAddedListener listener;

    public interface OnActivityAddedListener {
        void onActivityAdded();
    }

    public void setOnActivityAddedListener(OnActivityAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_new_block, container, false);

        nameInput = view.findViewById(R.id.edit_activity_name);
        sessionInput = view.findViewById(R.id.edit_sessions);
        durationText = view.findViewById(R.id.text_duration);
        ImageView closeBtn = view.findViewById(R.id.btn_close);
        ImageView saveBtn = view.findViewById(R.id.btn_save);

        closeBtn.setOnClickListener(v -> dismiss());
        saveBtn.setOnClickListener(v -> saveActivity());
        durationText.setOnClickListener(v -> showDurationPicker());

        // Focus and show keyboard
        nameInput.requestFocus();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return view;
    }

    private void showDurationPicker() {
        int hours = selectedDurationMins / 60;
        int mins = selectedDurationMins % 60;

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                R.style.RedTimePickerTheme,
                (view, hourOfDay, minute) -> {
                    selectedDurationMins = (hourOfDay * 60) + minute;
                    if (selectedDurationMins == 0) selectedDurationMins = 1; // Min 1 min
                    durationText.setText(selectedDurationMins + " minutes");
                }, hours, mins, true);

        timePickerDialog.setTitle("Set Duration");
        timePickerDialog.show();
        
        // Change button text to "DONE" to match design
        timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE).setText("DONE");
    }

    private void saveActivity() {
        String name = nameInput.getText().toString().trim();
        String sessionsStr = sessionInput.getText().toString().trim();
        int sessions = 4; // Default
        try {
            if (!sessionsStr.isEmpty()) sessions = Integer.parseInt(sessionsStr);
        } catch (NumberFormatException ignored) {}

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        final int finalSessions = sessions;
        Database.databaseExecutor.execute(() -> {
            Database db = Database.getInstance(requireContext());
            if (!db.activityDao().isNameOccupied(name)) {
                Activity newActivity = new Activity(name,
                        selectedDurationMins,
                        Constants.DEFAULT_BREAK_TIME,
                        true,
                        Constants.DEFAULT_LONG_BREAK_TIME,
                        finalSessions,
                        false, false, false, true);

                db.activityDao().insertActivity(newActivity);
                
                // Set as active activity
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
                // We need the ID of the newly inserted activity. 
                // For simplicity, I'll just refresh and let the user select it, 
                // or I can fetch it back.
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (listener != null) listener.onActivityAdded();
                        dismiss();
                    });
                }
            } else {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Label already exists", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setWindowAnimations(R.style.AppTheme);
        }
        return dialog;
    }
}
