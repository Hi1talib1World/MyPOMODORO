package com.denzo.mypomodoro.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzo.mypomodoro.R;
import com.denzo.mypomodoro.database.Activity;
import com.denzo.mypomodoro.database.Database;
import com.denzo.mypomodoro.database.PomodoroDao;
import com.denzo.mypomodoro.statistics.activitychart.PieChartItem;
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
    private EditText newActivityEditText;
    private Button addActivityButton;
    private RecyclerView legendRecyclerView;
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

        // Add Activity Views
        newActivityEditText = view.findViewById(R.id.new_activity_edit_text);
        addActivityButton = view.findViewById(R.id.add_activity_button);

        // Charts
        historyChart = view.findViewById(R.id.history_chart);
        pieChart = view.findViewById(R.id.pie_chart);

        // Legend
        legendRecyclerView = view.findViewById(R.id.legend_recycler_view);
        legendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setupCharts();
        setupSpinners(view);
        setupAddActivity();
        loadAllData();

        return view;
    }

    private void setupAddActivity() {
        addActivityButton.setOnClickListener(v -> {
            String name = newActivityEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a label", Toast.LENGTH_SHORT).show();
                return;
            }

            Database.databaseExecutor.execute(() -> {
                if (database.activityDao().isNameOccupied(name)) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> 
                            Toast.makeText(getContext(), "Label already exists", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    database.activityDao().insertActivity(new Activity(name));
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            newActivityEditText.setText("");
                            Toast.makeText(getContext(), "Label added", Toast.LENGTH_SHORT).show();
                            loadPieChartData();
                        });
                    }
                }
            });
        });
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
            
            int today = 0, week = 0, month = 0, total = 0;
            
            if (activityIds != null && activityIds.length > 0) {
                LocalDate now = LocalDate.now();
                today = dao.getCompletedWorksForDate(now.toString(), activityIds);
                week = dao.getCompletedWorksBetweenDates(now.minusDays(6).toString(), now.toString(), activityIds);
                month = dao.getCompletedWorksBetweenDates(now.minusMonths(1).toString(), now.toString(), activityIds);
                total = dao.getTotalCompletedWorks(activityIds);
            }

            final int fToday = today, fWeek = week, fMonth = month, fTotal = total;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    todayCountText.setText(String.valueOf(fToday));
                    weekCountText.setText(String.valueOf(fWeek));
                    monthCountText.setText(String.valueOf(fMonth));
                    totalCountText.setText(String.valueOf(fTotal));
                });
            }
        });
    }

    private void loadLineChartData() {
        Database.databaseExecutor.execute(() -> {
            PomodoroDao dao = database.pomodoroDao();
            int[] activityIds = database.activityDao().getIdsToShow();
            List<Entry> entries = new ArrayList<>();
            
            if (activityIds != null && activityIds.length > 0) {
                LocalDate now = LocalDate.now();

                // Fetch last 7 days
                for (int i = 6; i >= 0; i--) {
                    String date = now.minusDays(i).toString();
                    int count = dao.getCompletedWorksForDate(date, activityIds);
                    entries.add(new Entry(6 - i, count));
                }
            } else {
                // Add empty entries if no data
                for (int i = 0; i < 7; i++) {
                    entries.add(new Entry(i, 0));
                }
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
            List<PieChartItem> items = database.pomodoroDao().getAllPieChartItems();
            List<PieEntry> entries = new ArrayList<>();

            if (items != null && !items.isEmpty()) {
                for (PieChartItem item : items) {
                    if (item.getTotalTime() > 0) {
                        entries.add(new PieEntry(item.getTotalTime(), item.getActivityName()));
                    }
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (entries.isEmpty()) {
                        pieChart.clear();
                        pieChart.setNoDataText("No activity data yet");
                    } else {
                        PieDataSet dataSet = new PieDataSet(entries, "");
                        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                        dataSet.setValueTextColor(Color.WHITE);
                        dataSet.setValueTextSize(12f);

                        PieData data = new PieData(dataSet);
                        pieChart.setData(data);
                        pieChart.animateXY(800, 800);
                    }
                    pieChart.invalidate();

                    // Update Legend with ALL items (including those with 0 time)
                    LegendAdapter adapter = new LegendAdapter(items);
                    legendRecyclerView.setAdapter(adapter);
                });
            }
        });
    }

    private static class LegendAdapter extends RecyclerView.Adapter<LegendAdapter.ViewHolder> {
        private final List<PieChartItem> items;

        LegendAdapter(List<PieChartItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PieChartItem item = items.get(position);
            String text = item.getActivityName();
            if (item.getTotalTime() > 0) {
                long mins = item.getTotalTime() / (60 * 1000);
                text += " (" + mins + "m)";
            } else {
                text += " (0m)";
            }
            ((TextView) holder.itemView).setText(text);
            ((TextView) holder.itemView).setTextSize(14f);
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(View view) {
                super(view);
            }
        }
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
                R.array.history_spinner, android.R.layout.simple_spinner_item);
        hAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> aAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.activities_spinner, android.R.layout.simple_spinner_item);
        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        historySpinner.setAdapter(hAdapter);
        activitiesSpinner.setAdapter(aAdapter);
    }
}