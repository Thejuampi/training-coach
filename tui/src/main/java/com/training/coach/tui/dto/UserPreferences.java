package com.training.coach.tui.dto;

public record UserPreferences(MeasurementSystem measurementSystem) {

    public static UserPreferences metricDefaults() {
        return new UserPreferences(MeasurementSystem.METRIC);
    }

    public static UserPreferences imperialDefaults() {
        return new UserPreferences(MeasurementSystem.IMPERIAL);
    }
}