package com.denzo.mypomodoro.statistics.activitychart;

import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.DecimalFormat;

public final class CustomPercentFormatter extends ValueFormatter {

    private final DecimalFormat decimalFormat;

    public CustomPercentFormatter() {
        decimalFormat = new DecimalFormat("#0");
    }

    @Override
    public String getFormattedValue(float value) {
        return decimalFormat.format(value) + "%";
    }

    @Override
    public String getPieLabel(float value, PieEntry pieEntry) {
        return getFormattedValue(value);
    }
}