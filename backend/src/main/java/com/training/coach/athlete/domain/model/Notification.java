package com.training.coach.athlete.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Enhanced notification model with comprehensive metadata.
 */
public record Notification(
        String id,
        String athleteId,
        String recipientType,  // "athlete", "coach", or "admin"
        NotificationType type,
        NotificationPriority priority,
        String message,
        LocalDateTime timestamp,
        LocalDateTime deliveryTime,
        NotificationStatus status,
        Map<String, Object> metadata  // Additional context-specific data
) {}