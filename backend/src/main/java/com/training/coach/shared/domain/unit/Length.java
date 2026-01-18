package com.training.coach.shared.domain.unit;

public sealed interface Length permits Centimeters, Inches {
    Centimeters toCentimeters();

    Inches toInches();
}
