package com.training.coach.shared.domain.unit;

public sealed interface Power permits Watts, Kilowatts {
    Watts toWatts();

    Kilowatts toKilowatts();
}
