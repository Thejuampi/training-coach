package com.training.coach.user.domain.model;

public enum MeasurementSystem {
    METRIC,
    IMPERIAL;

    public WeightUnit defaultWeightUnit() {
        return this == IMPERIAL ? WeightUnit.POUNDS : WeightUnit.KILOGRAMS;
    }

    public DistanceUnit defaultDistanceUnit() {
        return this == IMPERIAL ? DistanceUnit.MILES : DistanceUnit.KILOMETERS;
    }

    public HeightUnit defaultHeightUnit() {
        return this == IMPERIAL ? HeightUnit.INCHES : HeightUnit.CENTIMETERS;
    }
}
