package com.denzo.mypomodoro.statistics;

import android.os.Bundle;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
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

    private LineChart historyChart;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        // Initialize charts
        historyChart = view.findViewById(R.id.history_chart);
        pieChart = view.findViewById(R.id.pie_chart);

        setupCharts();
        setupTimePeriodButtons(view);
        setupSpinners(view);

        return view;
    }

    private void setupCharts() {
        // Configure line chart (Pomodoro history)
        setupLineChart();

        // Configure pie chart (Activity distribution)
        setupPieChart();
    }

    private void setupLineChart() {
        // Sample data - replace with your actual Pomodoro data
        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 4)); // Day 1: 4 pomodoros
        entries.add(new Entry(1, 6)); // Day 2: 6 pomodoros
        entries.add(new Entry(2, 3)); // Day 3: 3 pomodoros

        LineDataSet dataSet = new LineDataSet(entries, "Daily Pomodoros");
        dataSet.setColor(Color.parseColor("#DA1E5B"));
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        historyChart.setData(lineData);

        // Customize chart appearance
        historyChart.getDescription().setEnabled(false);
        historyChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        historyChart.animateY(1000);
        historyChart.invalidate(); // Refresh
    }

    private void setupPieChart() {
        // Sample data - replace with your actual activity data
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(45f, "Work"));
        entries.add(new PieEntry(30f, "Study"));
        entries.add(new PieEntry(25f, "Other"));

        PieDataSet dataSet = new PieDataSet(entries, "Activities");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        // Customize chart appearance
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh
    }

    private void setupTimePeriodButtons(View view) {
        int[] buttonIds = {R.id.today_text_view, R.id.week_text_view,
                R.id.month_text_view, R.id.total_text_view};

        for (int id : buttonIds) {
            view.findViewById(id).setOnClickListener(v -> {
                updateChartsForPeriod(((TextView)v).getText().toString());
            });
        }
    }

    private void updateChartsForPeriod(String period) {
        // Implement your logic to update charts based on selected period
        Toast.makeText(getContext(), "Showing data for: " + period,
                Toast.LENGTH_SHORT).show();

        // Here you would:
        // 1. Fetch data for the selected period from your database
        // 2. Update both charts with the new data
    }

    private void setupSpinners(View view) {
        Spinner historySpinner = view.findViewById(R.id.history_spinner);
        Spinner activitiesSpinner = view.findViewById(R.id.activities_spinner);

        // Set up adapters (use your own data sources)
        ArrayAdapter<CharSequence> historyAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.history_options,
                android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> activitiesAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.activity_options,
                android.R.layout.simple_spinner_item);

        historySpinner.setAdapter(historyAdapter);
        activitiesSpinner.setAdapter(activitiesAdapter);

        historySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateHistoryChart(parent.getItemAtPosition(position).toString());
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        activitiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePieChart(parent.getItemAtPosition(position).toString());
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateHistoryChart(String selectedOption) {
        // Update line chart based on spinner selection
    }

    private void updatePieChart(String selectedOption) {
        // Update pie chart based on spinner selection
    }
}
