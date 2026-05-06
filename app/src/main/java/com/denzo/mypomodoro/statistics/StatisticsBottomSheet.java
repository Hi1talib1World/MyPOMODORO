package com.denzo.mypomodoro.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.database.Database;
import com.denzo.mypomodoro.database.PomodoroDao;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatisticsBottomSheet extends BottomSheetDialogFragment {

    private LineChart historyChart;
    private PieChart pieChart;
    private TextView todayCountText, weekCountText, monthCountText, totalCountText;
    private Database database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);

        database = Database.getInstance(requireContext());

        // Overview Views
        todayCountText = view.findViewById(R.id.number_today_text_view);
        weekCountText = view.findViewById(R.id.number_week_text_view);
        monthCountText = view.findViewById(R.id.number_month_text_view);
        totalCountText = view.findViewById(R.id.number_total_text_view);

        // Charts
        historyChart = view.findViewById(R.id.history_chart);
        pieChart = view.findViewById(R.id.pie_chart);

        setupCharts();
        setupSpinners(view);
        loadAllData();

        return view;
    }

    private void loadAllData() {
        loadOverviewData();
        loadLineChartData();
        loadPieChartData();
    }

    private void loadOverviewData() {
        Database.databaseExecutor.execute(() -> {
            PomodoroDao dao = database.pomodoroDao();
            int[] activityIds = database.activityDao().getIdsToShow();
            if (activityIds == null || activityIds.length == 0) return;

            LocalDate now = LocalDate.now();
            int today = dao.getCompletedWorksForDate(now.toString(), activityIds);
            int week = dao.getCompletedWorksBetweenDates(now.minusDays(6).toString(), now.toString(), activityIds);
            int month = dao.getCompletedWorksBetweenDates(now.minusMonths(1).toString(), now.toString(), activityIds);
            int total = dao.getTotalCompletedWorks(activityIds);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    todayCountText.setText(String.valueOf(today));
                    weekCountText.setText(String.valueOf(week));
                    monthCountText.setText(String.valueOf(month));
                    totalCountText.setText(String.valueOf(total));
                });
            }
        });
    }

    private void loadLineChartData() {
        Database.databaseExecutor.execute(() -> {
            PomodoroDao dao = database.pomodoroDao();
            int[] activityIds = database.activityDao().getIdsToShow();
            List<Entry> entries = new ArrayList<>();
            LocalDate now = LocalDate.now();

            // Fetch last 7 days
            for (int i = 6; i >= 0; i--) {
                String date = now.minusDays(i).toString();
                int count = dao.getCompletedWorksForDate(date, activityIds);
                entries.add(new Entry(6 - i, count));
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> updateLineChart(entries));
            }
        });
    }

    private void updateLineChart(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Completed Pomodoros");
        dataSet.setColor(Color.parseColor("#DA1E5B"));
        dataSet.setCircleColor(Color.parseColor("#DA1E5B"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData data = new LineData(dataSet);
        historyChart.setData(data);
        historyChart.animateY(1000);
        historyChart.invalidate();
    }

    private void loadPieChartData() {
        Database.databaseExecutor.execute(() -> {
            // Placeholder: Replace with real DAO query for activity distribution
            List<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(45f, "Work"));
            entries.add(new PieEntry(30f, "Coding"));
            entries.add(new PieEntry(25f, "Study"));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    PieDataSet dataSet = new PieDataSet(entries, "");
                    // Using MATERIAL_COLORS to avoid 'VORDEL_COLORS' resolution error
                    dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                    dataSet.setValueTextColor(Color.WHITE);
                    dataSet.setValueTextSize(12f);

                    PieData data = new PieData(dataSet);
                    pieChart.setData(data);
                    pieChart.animateXY(800, 800);
                    pieChart.invalidate();
                });
            }
        });
    }

    private void setupCharts() {
        // Line Chart Config
        historyChart.getDescription().setEnabled(false);
        historyChart.getLegend().setEnabled(false);
        XAxis xAxis = historyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        historyChart.getAxisRight().setEnabled(false);

        // Pie Chart Config
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setHoleRadius(45f);
        pieChart.setTransparentCircleRadius(50f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Activities");
    }

    private void setupSpinners(View view) {
        Spinner historySpinner = view.findViewById(R.id.history_spinner);
        Spinner activitiesSpinner = view.findViewById(R.id.activities_spinner);

        ArrayAdapter<CharSequence> hAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.history_options, android.R.layout.simple_spinner_item);
        hAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> aAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.activity_options, android.R.layout.simple_spinner_item);
        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        historySpinner.setAdapter(hAdapter);
        activitiesSpinner.setAdapter(aAdapter);
    }
}