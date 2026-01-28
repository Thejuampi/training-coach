package com.training.coach.athlete.application.service;

import com.training.coach.athlete.application.port.out.NotificationRepository;
import com.training.coach.athlete.domain.model.Notification;
import com.training.coach.athlete.domain.model.NotificationType;
import com.training.coach.athlete.domain.model.NotificationPriority;
import com.training.coach.athlete.domain.model.NotificationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Service for managing athlete notifications.
 */
@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public void notifyCoach(String athleteId, String message) {
        String id = UUID.randomUUID().toString();
        Notification notification = new Notification(
            id,
            athleteId,
            "coach",
            NotificationType.WORKOUT_REMINDER,
            NotificationPriority.NORMAL,
            message,
            LocalDateTime.now(),
            LocalDateTime.now(),
            NotificationStatus.DELIVERED,
            Map.of()
        );
        repository.save(notification);
    }

    public void notifyAthlete(String athleteId, String message) {
        String id = UUID.randomUUID().toString();
        Notification notification = new Notification(
            id,
            athleteId,
            "athlete",
            NotificationType.WORKOUT_REMINDER,
            NotificationPriority.NORMAL,
            message,
            LocalDateTime.now(),
            LocalDateTime.now(),
            NotificationStatus.DELIVERED,
            Map.of()
        );
        repository.save(notification);
    }

    public void notifyAdmin(String message) {
        String id = UUID.randomUUID().toString();
        Notification notification = new Notification(
            id,
            "system",
            "admin",
            NotificationType.WELCOME_MESSAGE,
            NotificationPriority.HIGH,
            message,
            LocalDateTime.now(),
            LocalDateTime.now(),
            NotificationStatus.DELIVERED,
            Map.of()
        );
        repository.save(notification);
    }

    public List<Notification> getNotifications(String athleteId) {
        return repository.findByAthleteId(athleteId);
    }
}
