package com.denzo.mypomodoro.statistics.historychart;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;;

final class CustomXAxisFormatter extends ValueFormatter {

    private final List<HistoryChartItem> historyChartItems;
    private final SpinnerOption spinnerOption;

    CustomXAxisFormatter(List<HistoryChartItem> historyChartItems, SpinnerOption spinnerOption) {
        this.historyChartItems = historyChartItems;
        this.spinnerOption = spinnerOption;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        if (value >= historyChartItems.size()) {
            return "";
        }

        LocalDate date = historyChartItems.get((int) value).getDate();

        String result = "";

        switch (spinnerOption) {
            case DAYS: {
                if (date.getMonthValue() == 1 && date.getDayOfMonth() == 1 || value == axis.mEntries[0]) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date) + "\n" + date.getYear();
                    break;
                }

                if (date.getDayOfMonth() == 1) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date);
                    break;
                }

                result = String.valueOf(date.getDayOfMonth());
                break;
            }
            case WEEKS: {
                if (value == axis.mEntries[0]) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date) + "\n" + date.getYear();
                    break;
                }

                if (date.getDayOfMonth() >= 1 && date.getDayOfMonth() <= 7) {
                    result = DateTimeFormatter.ofPattern("MMM").format(date);
                    break;
                }

                result = String.valueOf(date.getDayOfMonth());
                break;
            }
            case MONTHS: {
                result = DateTimeFormatter.ofPattern("MMM").format(date);

                if (date.getMonthValue() == 1 || value == axis.mEntries[0]) {
                    result += "\n" + date.getYear();
                }
                break;
            }
        }
        return result;
    }
}