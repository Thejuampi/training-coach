package com.training.coach.privacy.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for a data export request under GDPR.
 */
public record DataExportRequest(
        String id,
        String athleteId,
        RequestStatus status,
        Instant requestedAt,
        Instant approvedAt,
        String approvedBy,
        Instant completedAt,
        String exportFilePath,
        String rejectionReason,
        RequestMetadata metadata
) {
    public DataExportRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Request ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (status == null) status = RequestStatus.PENDING;
        if (metadata == null) metadata = RequestMetadata.now();
    }

    public static DataExportRequest create(String athleteId) {
        return new DataExportRequest(
                UUID.randomUUID().toString(),
                athleteId,
                RequestStatus.PENDING,
                Instant.now(),
                null,
                null,
                null,
                null,
                null,
                RequestMetadata.now()
        );
    }

    public DataExportRequest approve(String approvedBy) {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("Can only approve pending requests");
        }
        return new DataExportRequest(
                id,
                athleteId,
                RequestStatus.APPROVED,
                requestedAt,
                Instant.now(),
                approvedBy,
                null,
                null,
                null,
                metadata
        );
    }

    public DataExportRequest complete(String exportFilePath) {
        if (this.status != RequestStatus.APPROVED) {
            throw new IllegalStateException("Can only complete approved requests");
        }
        return new DataExportRequest(
                id,
                athleteId,
                RequestStatus.COMPLETED,
                requestedAt,
                approvedAt,
                approvedBy,
                Instant.now(),
                exportFilePath,
                null,
                metadata
        );
    }

    public DataExportRequest reject(String reason) {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("Can only reject pending requests");
        }
        return new DataExportRequest(
                id,
                athleteId,
                RequestStatus.REJECTED,
                requestedAt,
                null,
                null,
                null,
                null,
                reason,
                metadata
        );
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    public boolean isApproved() {
        return status == RequestStatus.APPROVED;
    }

    public boolean isCompleted() {
        return status == RequestStatus.COMPLETED;
    }

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, COMPLETED, FAILED
    }

    public record RequestMetadata(
            Instant createdAt,
            String requestId,
            String dataSource
    ) {
        public static RequestMetadata now() {
            return new RequestMetadata(
                    Instant.now(),
                    UUID.randomUUID().toString(),
                    "training-coach"
            );
        }

        public static RequestMetadata now(String dataSource) {
            return new RequestMetadata(
                    Instant.now(),
                    UUID.randomUUID().toString(),
                    dataSource
            );
        }
    }
}
