package com.training.coach.privacy.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for a consent log entry tracking all GDPR-related actions.
 */
public record ConsentLog(
        String id,
        String athleteId,
        ConsentAction action,
        String actionType,
        Instant timestamp,
        String performedBy,
        String requestId,
        String details,
        LogMetadata metadata
) {
    public ConsentLog {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Log ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        if (actionType == null || actionType.isBlank()) {
            throw new IllegalArgumentException("Action type cannot be null or blank");
        }
        if (performedBy == null || performedBy.isBlank()) {
            throw new IllegalArgumentException("Performed by cannot be null or blank");
        }
        if (metadata == null) metadata = LogMetadata.now();
    }

    public static ConsentLog create(
            String athleteId,
            ConsentAction action,
            String actionType,
            String performedBy,
            String requestId,
            String details
    ) {
        return new ConsentLog(
                UUID.randomUUID().toString(),
                athleteId,
                action,
                actionType,
                Instant.now(),
                performedBy,
                requestId,
                details,
                LogMetadata.now()
        );
    }

    public enum ConsentAction {
        DATA_EXPORT_REQUESTED,
        DATA_EXPORT_APPROVED,
        DATA_EXPORT_COMPLETED,
        DATA_EXPORT_REJECTED,
        DATA_DELETION_REQUESTED,
        DATA_DELETION_APPROVED,
        DATA_DELETION_COMPLETED,
        DATA_DELETION_REJECTED,
        CONSENT_GRANTED,
        CONSENT_REVOKED,
        CONSENT_UPDATED,
        DATA_ACCESSED
    }

    public record LogMetadata(
            Instant loggedAt,
            String ipAddress,
            String userAgent,
            String sessionId
    ) {
        public static LogMetadata now() {
            return new LogMetadata(
                    Instant.now(),
                    null,
                    null,
                    null
            );
        }

        public static LogMetadata now(String ipAddress, String userAgent, String sessionId) {
            return new LogMetadata(
                    Instant.now(),
                    ipAddress,
                    userAgent,
                    sessionId
            );
        }
    }
}
