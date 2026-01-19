package com.training.coach.testconfig.inmemory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.training.coach.athlete.application.port.out.NotificationRepository;
import com.training.coach.athlete.domain.model.Notification;

/**
 * In-memory NotificationRepository for fast tests.
 */
public class InMemoryNotificationRepository implements NotificationRepository {
    private final ConcurrentHashMap<String, java.util.List<Notification>> notifications = new ConcurrentHashMap<>();

    @Override
    public Notification save(Notification notification) {
        notifications.computeIfAbsent(notification.athleteId(), key -> new java.util.ArrayList<>()).add(notification);
        return notification;
    }

    @Override
    public List<Notification> findByAthleteId(String athleteId) {
        return notifications.getOrDefault(athleteId, List.of());
    }
}
