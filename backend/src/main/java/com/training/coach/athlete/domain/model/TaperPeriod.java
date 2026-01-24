package com.training.coach.athlete.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Represents a taper period before a goal event.
 * Taper reduces training volume while maintaining intensity to ensure peak performance.
 */
public record TaperPeriod(
        String eventId,
        LocalDate startDate,
        LocalDate endDate,
        int durationDays,
        TaperPhase phase,
        double volumeReductionPercent,
        boolean maintainsKeyEfforts
) {
    public TaperPeriod {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Taper dates cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Taper start date must be before end date");
        }
        if (durationDays <= 0) {
            throw new IllegalArgumentException("Taper duration must be positive");
        }
        if (volumeReductionPercent < 0 || volumeReductionPercent > 100) {
            throw new IllegalArgumentException("Volume reduction must be between 0 and 100");
        }
    }

    /**
     * Create a taper period ending on the event date.
     */
    public static TaperPeriod createForEvent(Event event) {
        LocalDate endDate = event.date();
        int duration = event.taperDays();
        LocalDate startDate = endDate.minusDays(duration);

        TaperPhase phase = determinePhase(duration);
        double reduction = calculateVolumeReduction(duration, event.priority());
        boolean maintainsKeyEfforts = event.priority() == Event.EventPriority.A;

        return new TaperPeriod(
                event.id(),
                startDate,
                endDate,
                duration,
                phase,
                reduction,
                maintainsKeyEfforts
        );
    }

    /**
     * Determine the taper phase based on duration.
     */
    private static TaperPhase determinePhase(int durationDays) {
        if (durationDays >= 14) {
            return TaperPhase.FULL_TAPER;
        } else if (durationDays >= 7) {
            return TaperPhase.MODERATE_TAPER;
        } else {
            return TaperPhase.LIGHT_TAPER;
        }
    }

    /**
     * Calculate volume reduction percentage based on duration and priority.
     */
    private static double calculateVolumeReduction(int durationDays, Event.EventPriority priority) {
        // Base reduction on duration with priority adjustment
        double baseReduction = switch (priority) {
            case A -> 50.0;  // 50% reduction for A races
            case B -> 35.0;  // 35% reduction for B races
            case C -> 20.0;  // 20% reduction for C races
        };

        // Adjust based on duration (longer taper = more gradual reduction)
        return Math.min(baseReduction + (durationDays * 1.5), 65.0);
    }

    /**
     * Check if a specific date falls within the taper period.
     */
    public boolean isDateInTaper(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Get the daily volume adjustment factor for a specific day in the taper.
     * Returns a value between 0.0 and 1.0 where 1.0 = full volume.
     */
    public double getDailyVolumeFactor(LocalDate date) {
        if (!isDateInTaper(date)) {
            return 1.0;
        }

        // Calculate days into taper (0 = first day, durationDays-1 = last day)
        int daysIntoTaper = (int) ChronoUnit.DAYS.between(startDate, date);

        // Progressive reduction: more reduction as taper progresses
        // Start with minimal reduction, end with full reduction
        double progress = (double) daysIntoTaper / durationDays;
        return 1.0 - (volumeReductionPercent / 100.0 * progress);
    }

    /**
     * Taper phases describing the intensity of the taper.
     */
    public enum TaperPhase {
        FULL_TAPER,      // 2+ weeks: Comprehensive taper for A-priority events
        MODERATE_TAPER,  // 1 week: Moderate taper for B-priority events
        LIGHT_TAPER      // 3-6 days: Light taper for C-priority events
    }
}