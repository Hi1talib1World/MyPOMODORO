package com.denzo.mypomodoro.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.Utility;
import com.denzo.mypomodoro.database.Database;
import com.denzo.mypomodoro.statistics.activitychart.PieChartItem;
import com.denzo.mypomodoro.statistics.historychart.HistoryChartItem;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StatsFragment extends Fragment {

    private Database database;
    private BarChart focusBarChart;
    private TextView totalFocusTimeText, totalSessionsText, dailyStreakText;
    private TextView topActivityNameText, topActivityTimeText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_statistics, container, false);

        database = Database.getInstance(requireContext());

        totalFocusTimeText = view.findViewById(R.id.number_total_text_view);
        totalSessionsText = view.findViewById(R.id.number_total_sessions_text_view);
        dailyStreakText = view.findViewById(R.id.daily_streak_text_view);
        focusBarChart = view.findViewById(R.id.focus_bar_chart);
        
        topActivityNameText = view.findViewById(R.id.top_activity_name);
        topActivityTimeText = view.findViewById(R.id.top_activity_time);

        setupBarChartLook();
        loadData();

        return view;
    }

    private void setupBarChartLook() {
        focusBarChart.setDrawBarShadow(false);
        focusBarChart.setDrawValueAboveBar(true);
        focusBarChart.getDescription().setEnabled(false);
        focusBarChart.setPinchZoom(false);
        focusBarChart.setDrawGridBackground(false);
        focusBarChart.getLegend().setEnabled(false);

        XAxis xAxis = focusBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        xAxis.setTextColor(getResources().getColor(R.color.brand_grey));

        YAxis leftAxis = focusBarChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(getResources().getColor(R.color.brand_blue_card));
        leftAxis.setTextColor(getResources().getColor(R.color.brand_grey));
        leftAxis.setAxisMinimum(0f);

        focusBarChart.getAxisRight().setEnabled(false);
    }

    private void loadData() {
        Database.databaseExecutor.execute(() -> {
            int[] ids = database.activityDao().getIdsToShow();
            
            // Total Focus Time
            List<HistoryChartItem> allItems = database.pomodoroDao().getAllGroupByDate(ids);
            long totalTime = 0;
            for (HistoryChartItem item : allItems) totalTime += item.getTime();
            final String timeStr = Utility.formatStatisticsTime(totalTime);

            // Total Sessions
            int totalSessions = database.pomodoroDao().getTotalCompletedWorks(ids);

            // Daily Streak
            int streak = calculateDailyStreak(ids);

            // Top Activity
            List<PieChartItem> pieItems = database.pomodoroDao().getAllPieChartItems();
            String topActName = "No Data";
            String topActTime = "0m";
            if (pieItems != null && !pieItems.isEmpty()) {
                PieChartItem top = pieItems.get(0);
                for (PieChartItem item : pieItems) {
                    if (item.getTotalTime() > top.getTotalTime()) top = item;
                }
                topActName = top.getActivityName();
                topActTime = Utility.formatStatisticsTime(top.getTotalTime());
            }

            final String finalTopActName = topActName;
            final String finalTopActTime = topActTime;

            // Bar Chart Last 7 Days
            List<BarEntry> barEntries = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                HistoryChartItem item = database.pomodoroDao().getAllGroupByDate(date.toString(), ids);
                float value = (item != null) ? (float) item.getTime() / 60000 : 0f;
                barEntries.add(new BarEntry(6 - i, value));
            }

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getContext() != null) {
                        totalFocusTimeText.setText(timeStr);
                        totalSessionsText.setText(String.valueOf(totalSessions));
                        dailyStreakText.setText(getString(R.string.days_unit, streak));

                        if (topActivityNameText != null) topActivityNameText.setText(finalTopActName);
                        if (topActivityTimeText != null) topActivityTimeText.setText(finalTopActTime + " total");

                        BarDataSet barDataSet = new BarDataSet(barEntries, "Focus Minutes");
                        barDataSet.setColor(getResources().getColor(R.color.brand_yellow));
                        barDataSet.setDrawValues(false);

                        BarData barData = new BarData(barDataSet);
                        barData.setBarWidth(0.5f);
                        focusBarChart.setData(barData);
                        focusBarChart.invalidate();
                    }
                });
            }
        });
    }

    private int calculateDailyStreak(int[] activityIds) {
        int streak = 0;
        LocalDate date = LocalDate.now();
        while (true) {
            if (database.pomodoroDao().getCompletedWorksForDate(date.toString(), activityIds) > 0) {
                streak++;
                date = date.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }
}
