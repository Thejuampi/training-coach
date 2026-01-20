package com.training.coach.integration.application.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Service;

/**
 * Application service for managing integrations.
 */
@Service
public class IntegrationService {

    private String intervalsIcuApiKey;
    private final ConcurrentLinkedQueue<SyncEvent> syncEvents = new ConcurrentLinkedQueue<>();

    public void configureIntervalsIcu(String apiKey) {
        this.intervalsIcuApiKey = apiKey;
        // In real app, persist to DB or config
    }

    public String getIntervalsIcuApiKey() {
        return intervalsIcuApiKey;
    }

    /**
     * Records a sync attempt for an integration.
     */
    public void recordSyncAttempt(String platform, boolean success, String errorDetails) {
        syncEvents.add(new SyncEvent(platform, Instant.now(), success, errorDetails));
        // Keep only last 100 events to prevent memory bloat
        while (syncEvents.size() > 100) {
            syncEvents.poll();
        }
    }

    /**
     * Gets the health status of an integration based on recent sync events.
     * Returns "degraded" if 3 or more failures in the last 24 hours.
     */
    public String getIntegrationHealth(String platform) {
        Instant cutoff = Instant.now().minusSeconds(24 * 60 * 60);
        long failures = syncEvents.stream()
                .filter(e -> e.platform().equals(platform))
                .filter(e -> e.timestamp().isAfter(cutoff))
                .filter(e -> !e.success())
                .count();

        if (failures >= 3) {
            return "degraded";
        }
        return "active";
    }

    /**
     * Gets sync events for the past 24 hours.
     */
    public List<SyncEvent> getSyncHistory(String platform) {
        Instant cutoff = Instant.now().minusSeconds(24 * 60 * 60);
        List<SyncEvent> recentEvents = new ArrayList<>();
        for (SyncEvent event : syncEvents) {
            if (event.platform().equals(platform) && event.timestamp().isAfter(cutoff)) {
                recentEvents.add(event);
            }
        }
        return recentEvents;
    }

    /**
     * Returns whether an integration is configured.
     */
    public boolean isIntegrationConfigured(String platform) {
        return switch (platform) {
            case "Intervals.icu" -> intervalsIcuApiKey != null && !intervalsIcuApiKey.isBlank();
            default -> false;
        };
    }

    /**
     * Clears all sync events (for testing).
     */
    public void clearSyncEvents() {
        syncEvents.clear();
    }

    public record SyncEvent(String platform, Instant timestamp, boolean success, String errorDetails) {}
}
