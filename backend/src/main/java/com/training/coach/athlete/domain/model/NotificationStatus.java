package com.training.coach.athlete.domain.model;

/**
 * Status of a notification.
 */
public enum NotificationStatus {
    PENDING("Pending - not yet sent"),
    SENT("Sent - delivered to recipient"),
    DELIVERED("Delivered - confirmed receipt"),
    READ("Read - recipient has viewed"),
    ACKNOWLEDGED("Acknowledged - recipient has taken action"),
    EXPIRED("Expired - no longer relevant"),
    CANCELLED("Cancelled - manually removed");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}