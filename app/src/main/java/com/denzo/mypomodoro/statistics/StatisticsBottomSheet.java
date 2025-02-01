package com.denzo.mypomodoro.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.denzo.mypomodoro.R;

public class StatisticsBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        // Initialize UI elements
        TextView todayTextView = view.findViewById(R.id.today_text_view);
        TextView weekTextView = view.findViewById(R.id.week_text_view);
        TextView monthTextView = view.findViewById(R.id.month_text_view);
        TextView totalTextView = view.findViewById(R.id.total_text_view);

        Spinner historySpinner = view.findViewById(R.id.history_spinner);
        Spinner activitiesSpinner = view.findViewById(R.id.activities_spinner);

        // Click listeners for interactive elements
        todayTextView.setOnClickListener(v -> showToast("Today's Stats Clicked!"));
        weekTextView.setOnClickListener(v -> showToast("Weekly Stats Clicked!"));
        monthTextView.setOnClickListener(v -> showToast("Monthly Stats Clicked!"));
        totalTextView.setOnClickListener(v -> showToast("Total Stats Clicked!"));

        historySpinner.setOnItemSelectedListener(new SpinnerInteractionListener());
        activitiesSpinner.setOnItemSelectedListener(new SpinnerInteractionListener());



        return view;
    }

    /**
     * Helper method to show a toast message.
     */
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Listener to handle spinner interactions.
     */
    private class SpinnerInteractionListener implements android.widget.AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            showToast("Selected: " + parent.getSelectedItem().toString());
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
