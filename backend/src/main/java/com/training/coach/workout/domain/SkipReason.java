package com.training.coach.workout.domain;

/**
 * Enumeration of possible reasons for skipping a workout.
 */
public enum SkipReason {
    // Health-related reasons
    ILLNESS("Medical illness or injury"),
    FATIGUE("Excessive fatigue or overtraining"),
    PAIN("Muscle or joint pain"),

    // Environmental reasons
    WEATHER("Inclement weather conditions"),
    TRAVEL("Travel or logistical issues"),
    EQUIPMENT("Equipment failure or unavailability"),

    // Performance reasons
    EXHAUSTED("Completely exhausted from previous days"),
    OVERREACHED("Overreached from training load"),

    // Personal reasons
    PERSONAL("Personal emergency or family matters"),
    STRESSMENTAL("Mental health or stress management"),

    // Training-related reasons
    SUBSTITUTION("Alternative activity planned by coach"),
    TAPERING("Following taper protocol"),

    // Other
    MISSED("Missed without specific reason");

    private final String description;

    SkipReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static SkipReason fromDescription(String description) {
        for (SkipReason reason : values()) {
            if (reason.description.equalsIgnoreCase(description)) {
                return reason;
            }
        }
        return MISSED;
    }
}