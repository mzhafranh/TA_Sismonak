package com.mzhtech.sismonakdev.utils;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class CustomValueFormatter extends ValueFormatter {
    private float threshold; // Minimum value to display

    public CustomValueFormatter(float threshold) {
        this.threshold = threshold;
    }

    @Override
    public String getFormattedValue(float value) {
        if (value > threshold) {
            return String.format("%.1f%%", value); // Format as percentage
        } else {
            return ""; // Return empty string for values below the threshold
        }
    }
}
