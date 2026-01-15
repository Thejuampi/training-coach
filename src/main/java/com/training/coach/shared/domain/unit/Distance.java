package com.training.coach.shared.domain.unit;

public sealed interface Distance permits Meters, Kilometers, Miles {
    Meters toMeters();

    Kilometers toKilometers();

    Miles toMiles();
}
