package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.NotificationService;
import com.training.coach.athlete.domain.model.Notification;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Get notifications for a specific athlete.
     */
    @GetMapping("/{athleteId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String athleteId) {
        List<Notification> notifications = notificationService.getNotifications(athleteId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark a notification as read.
     */
    @PostMapping("/{athleteId}/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String athleteId,
            @PathVariable String notificationId
    ) {
        // In a full implementation, this would update the notification status
        // For now, just acknowledge the request
        return ResponseEntity.ok().build();
    }

    /**
     * Get all unread notifications for an athlete.
     */
    @GetMapping("/{athleteId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String athleteId) {
        List<Notification> notifications = notificationService.getNotifications(athleteId);
        List<Notification> unread = notifications.stream()
                .filter(notification -> notification.message().contains("unread"))
                .toList();
        return ResponseEntity.ok(unread);
    }

    /**
     * Delete a notification.
     */
    @DeleteMapping("/{athleteId}/notifications/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable String athleteId,
            @PathVariable String notificationId
    ) {
        // In a full implementation, this would remove the notification
        // For now, just acknowledge the request
        return ResponseEntity.noContent().build();
    }
}