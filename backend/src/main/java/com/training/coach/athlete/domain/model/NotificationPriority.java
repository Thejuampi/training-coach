package com.training.coach.athlete.domain.model;

/**
 * Priority levels for notifications.
 */
public enum NotificationPriority {
    LOW("Low - Informational"),
    NORMAL("Normal - Standard priority"),
    HIGH("High - Important action required"),
    URGENT("Urgent - Immediate attention needed");

    private final String description;

    NotificationPriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}