package com.training.coach.athlete.domain.model;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.shared.domain.unit.Percent;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import java.util.List;

/**
 * Immutable value object representing a training workout.
 */
public record Workout(
        String id,
        LocalDate date,
        WorkoutType type,
        Minutes durationMinutes,
        IntensityProfile intensityProfile,
        List<Interval> intervals) {
    public Workout {
        if (durationMinutes == null || durationMinutes.value() <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (intervals == null) {
            throw new IllegalArgumentException("Intervals cannot be null");
        }
    }

    public enum WorkoutType {
        ENDURANCE, // Low intensity, long duration
        THRESHOLD, // Sweet spot training
        INTERVALS, // HIIT or VO2 max intervals
        RECOVERY, // Easy ride
        TEST // FTP/VO2 test
    }

    public record IntensityProfile(
            Percent zone1Percent, // Time in zone 1 (% FTP)
            Percent zone2Percent,
            Percent zone3Percent,
            Percent zone4Percent,
            Percent zone5Percent) {
        public IntensityProfile {
            double total = zone1Percent.value()
                    + zone2Percent.value()
                    + zone3Percent.value()
                    + zone4Percent.value()
                    + zone5Percent.value();
            if (total > 100.0) {
                throw new IllegalArgumentException("Zone percentages cannot exceed 100%");
            }
        }
    }

    public record Interval(
            IntervalType type,
            Minutes durationMinutes,
            Watts powerTargetWatts, // Or % FTP, but using absolute for simplicity
            BeatsPerMinute heartRateTargetBpm // Or % FTHR
            ) {
        public Interval {
            if (durationMinutes == null || durationMinutes.value() <= 0) {
                throw new IllegalArgumentException("Interval duration must be positive");
            }
            if (powerTargetWatts == null || powerTargetWatts.value() < 0) {
                throw new IllegalArgumentException("Power target must be non-negative");
            }
            if (heartRateTargetBpm == null || heartRateTargetBpm.value() < 0) {
                throw new IllegalArgumentException("Heart rate target must be non-negative");
            }
        }

        public enum IntervalType {
            RECOVERY,
            THRESHOLD,
            VO2_MAX
        }
    }
}
