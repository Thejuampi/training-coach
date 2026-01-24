package com.training.coach.user.domain.model;

public record UserPreferences(
        MeasurementSystem measurementSystem, WeightUnit weightUnit, DistanceUnit distanceUnit, HeightUnit heightUnit,
        ActivityVisibility activityVisibility, WellnessDataSharing wellnessDataSharing) {
    public UserPreferences {
        if (measurementSystem == null) {
            measurementSystem = MeasurementSystem.METRIC;
        }
        if (weightUnit == null) {
            weightUnit = measurementSystem.defaultWeightUnit();
        }
        if (distanceUnit == null) {
            distanceUnit = measurementSystem.defaultDistanceUnit();
        }
        if (heightUnit == null) {
            heightUnit = measurementSystem.defaultHeightUnit();
        }
        if (activityVisibility == null) {
            activityVisibility = ActivityVisibility.PUBLIC;
        }
        if (wellnessDataSharing == null) {
            wellnessDataSharing = WellnessDataSharing.COACH_ONLY;
        }
    }

    public static UserPreferences metricDefaults() {
        return new UserPreferences(MeasurementSystem.METRIC, null, null, null, null, null);
    }

    public UserPreferences withDistanceUnit(DistanceUnit distanceUnit) {
        return new UserPreferences(measurementSystem, weightUnit, distanceUnit, heightUnit, activityVisibility, wellnessDataSharing);
    }

    public UserPreferences withWeightUnit(WeightUnit weightUnit) {
        return new UserPreferences(measurementSystem, weightUnit, distanceUnit, heightUnit, activityVisibility, wellnessDataSharing);
    }

    public UserPreferences withActivityVisibility(ActivityVisibility activityVisibility) {
        return new UserPreferences(measurementSystem, weightUnit, distanceUnit, heightUnit, activityVisibility, wellnessDataSharing);
    }

    public UserPreferences withWellnessDataSharing(WellnessDataSharing wellnessDataSharing) {
        return new UserPreferences(measurementSystem, weightUnit, distanceUnit, heightUnit, activityVisibility, wellnessDataSharing);
    }

    public UserPreferences withMeasurementSystem(MeasurementSystem measurementSystem) {
        return new UserPreferences(measurementSystem, weightUnit, distanceUnit, heightUnit, activityVisibility, wellnessDataSharing);
    }

    public UserPreferences withTargetWeeklyVolumeHours(com.training.coach.shared.domain.unit.Hours hours) {
        return new UserPreferences(measurementSystem, weightUnit, distanceUnit, heightUnit, activityVisibility, wellnessDataSharing);
    }

    public static UserPreferences imperialDefaults() {
        return new UserPreferences(MeasurementSystem.IMPERIAL, null, null, null, null, null);
    }
}
