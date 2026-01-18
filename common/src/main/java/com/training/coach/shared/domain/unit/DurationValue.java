package com.training.coach.shared.domain.unit;

public sealed interface DurationValue permits Seconds, Minutes, Hours {
    Seconds toSeconds();

    Minutes toMinutes();

    Hours toHours();
}