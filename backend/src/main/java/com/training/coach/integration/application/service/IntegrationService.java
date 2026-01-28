package com.training.coach.integration.application.service;

import com.training.coach.shared.functional.Result;
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

    /**
     * Get status for a specific integration without exposing secrets.
     */
    public Result<IntegrationStatus> getIntegrationStatus(String platformId) {
        try {
            String status = getIntegrationHealth(platformId);
            Instant lastSync = syncEvents.stream()
                    .filter(e -> e.platform().equals(platformId))
                    .map(SyncEvent::timestamp)
                    .max(Instant::compareTo)
                    .orElse(null);

            Instant cutoff = Instant.now().minusSeconds(24 * 60 * 60);
            long errorCount = syncEvents.stream()
                    .filter(e -> e.platform().equals(platformId))
                    .filter(e -> e.timestamp().isAfter(cutoff))
                    .filter(e -> !e.success())
                    .count();

            // API key is intentionally hidden (masked) in the response
            String maskedApiKey = null;
            if ("Intervals.icu".equals(platformId) && intervalsIcuApiKey != null && !intervalsIcuApiKey.isBlank()) {
                maskedApiKey = "***" + intervalsIcuApiKey.substring(
                    Math.max(0, intervalsIcuApiKey.length() - 4)
                );
            }

            return Result.success(new IntegrationStatus(
                platformId,
                status,
                lastSync,
                (int) errorCount,
                maskedApiKey
            ));
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    /**
     * Get health status for all integrations.
     */
    public Result<List<IntegrationHealth>> getAllIntegrationHealth() {
        try {
            List<IntegrationHealth> health = List.of(
                getIntegrationHealthFor("Intervals.icu")
                // Add more platforms here as they're implemented
            );
            return Result.success(health);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    private IntegrationHealth getIntegrationHealthFor(String platform) {
        Instant cutoff = Instant.now().minusSeconds(24 * 60 * 60);
        long failures = syncEvents.stream()
                .filter(e -> e.platform().equals(platform))
                .filter(e -> e.timestamp().isAfter(cutoff))
                .filter(e -> !e.success())
                .count();

        Instant lastSync = syncEvents.stream()
                .filter(e -> e.platform().equals(platform))
                .map(SyncEvent::timestamp)
                .max(Instant::compareTo)
                .orElse(null);

        String status = failures >= 3 ? "degraded" : "active";

        List<String> remediationSteps = List.of();
        if ("degraded".equals(status)) {
            remediationSteps = List.of(
                "Check API key configuration",
                "Verify platform API is accessible",
                "Review sync error logs"
            );
        }

        return new IntegrationHealth(
            platform,
            status,
            lastSync,
            (int) failures,
            remediationSteps
        );
    }

    public record SyncEvent(String platform, Instant timestamp, boolean success, String errorDetails) {}

    public record IntegrationStatus(
            String platformId,
            String status,
            Instant lastSync,
            int errorCount,
            String apiKey
    ) {}

    public record IntegrationHealth(
            String platformId,
            String status,
            Instant lastSync,
            int errorCount,
            List<String> remediationSteps
    ) {}
}
