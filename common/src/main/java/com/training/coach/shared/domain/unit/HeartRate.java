package com.training.coach.shared.domain.unit;

public sealed interface HeartRate permits BeatsPerMinute {
    BeatsPerMinute toBeatsPerMinute();
}