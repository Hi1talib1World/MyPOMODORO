package com.denzo.mypomodoro.statistics.historychart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class MonthData extends ChartData {

    private final List<HistoryChartItem> months = new ArrayList<>();

    public MonthData(List<HistoryChartItem> data) {
        super(data);
    }

    public void generate() {
        createMonthsArray();
        prepareMonths(LocalDate.now().toString());
        createEntries(months);
    }

    @Override
    public List<HistoryChartItem> getGeneratedData() {
        return new ArrayList<>(months);
    }

    public void prepareMonths(String currentDate) {
        // The loop below this condition works only when the months array has two entries. I'm
        // using this block of code to generate the second entry if it doesn't exist.
        if (months.size() == 1) {
            LocalDate thisMonth = months.get(0).getDate();
            LocalDate currentMonth = LocalDate.parse(currentDate);

            if (thisMonth.getMonthValue() != currentMonth.getMonthValue() ||
                    thisMonth.getYear() != currentMonth.getYear()) {
                months.add(HistoryChartItem.of(currentMonth));
            }
        }

        for (int i = 0; i < months.size() - 1; i++) {
            LocalDate thisMonth = months.get(i).getDate().plusMonths(1);
            LocalDate nextMonth = months.get(i + 1).getDate();

            if (thisMonth.getMonthValue() != nextMonth.getMonthValue() ||
                    thisMonth.getYear() != nextMonth.getYear()) {
                months.add(i + 1, HistoryChartItem.of(thisMonth));
                continue;
            }

            if (i + 1 == months.size() - 1 &&
                    nextMonth.getMonthValue() != LocalDate.parse(currentDate).getMonthValue()) {
                thisMonth = thisMonth.plusMonths(1);
                months.add(HistoryChartItem.of(thisMonth));
            }
        }

        if (months.size() == 0) {
            LocalDate localDate = LocalDate.parse(currentDate).minusMonths(11);

            for (int i = 0; i < 12; i++) {
                months.add(HistoryChartItem.of(localDate));
                localDate = localDate.plusMonths(1);
            }
        }

        if (months.size() < 12) {
            LocalDate firstMonth = months.get(0).getDate();

            for (int i = 12 - months.size(); i > 0; i--) {
                firstMonth = firstMonth.minusMonths(1);
                months.add(0, HistoryChartItem.of(firstMonth));
            }
        }
    }

    // TODO: 22.09.2020 Replace this method by using GROUP BY in SQLite query
    public void createMonthsArray() {
        long totalTime = 0;
        List<HistoryChartItem> days = getData();

        for (int i = 0; i < days.size() - 1; i++) {
            LocalDate todayDate = days.get(i).getDate();
            LocalDate nextDate = days.get(i + 1).getDate();

            totalTime += days.get(i).getTime();

            if (todayDate.getMonthValue() != nextDate.getMonthValue() ||
                    todayDate.getYear() != nextDate.getYear()) {
                months.add(HistoryChartItem.of(todayDate, totalTime, 0));

                totalTime = 0;
            }

            if (i == days.size() - 2) {
                if (todayDate.getMonthValue() == nextDate.getMonthValue() &&
                        todayDate.getYear() == nextDate.getYear() &&
                        i == days.size() - 2) {

                    totalTime += days.get(i + 1).getTime();

                    months.add(HistoryChartItem.of(nextDate, totalTime, 0));
                }

                if (todayDate.getMonthValue() != nextDate.getMonthValue() ||
                        todayDate.getYear() != nextDate.getYear()) {

                    months.add(days.get(i + 1));
                }
            }
        }

        if (days.size() == 1) {
            months.add(HistoryChartItem.of(days.get(0).getDate(), days.get(0).getTime(), 0));
        }
    }
}