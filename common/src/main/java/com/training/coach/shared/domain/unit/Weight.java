package com.training.coach.shared.domain.unit;

public sealed interface Weight permits Kilograms, Grams, Pounds {
    Kilograms toKilograms();

    Grams toGrams();

    Pounds toPounds();
}