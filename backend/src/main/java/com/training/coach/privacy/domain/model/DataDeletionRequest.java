package com.training.coach.privacy.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for a data deletion request under GDPR.
 */
public record DataDeletionRequest(
        String id,
        String athleteId,
        RequestStatus status,
        Instant requestedAt,
        Instant approvedAt,
        String approvedBy,
        Instant completedAt,
        int recordsDeleted,
        int recordsAnonymized,
        String rejectionReason,
        RequestMetadata metadata
) {
    public DataDeletionRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Request ID cannot be null or blank");
        }
        if (athleteId == null || athleteId.isBlank()) {
            throw new IllegalArgumentException("Athlete ID cannot be null or blank");
        }
        if (status == null) status = RequestStatus.PENDING;
        if (metadata == null) metadata = RequestMetadata.now();
        if (recordsDeleted < 0) {
            throw new IllegalArgumentException("Records deleted cannot be negative");
        }
        if (recordsAnonymized < 0) {
            throw new IllegalArgumentException("Records anonymized cannot be negative");
        }
    }

    public static DataDeletionRequest create(String athleteId) {
        return new DataDeletionRequest(
                UUID.randomUUID().toString(),
                athleteId,
                RequestStatus.PENDING,
                Instant.now(),
                null,
                null,
                null,
                0,
                0,
                null,
                RequestMetadata.now()
        );
    }

    public DataDeletionRequest approve(String approvedBy) {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("Can only approve pending requests");
        }
        return new DataDeletionRequest(
                id,
                athleteId,
                RequestStatus.APPROVED,
                requestedAt,
                Instant.now(),
                approvedBy,
                null,
                0,
                0,
                null,
                metadata
        );
    }

    public DataDeletionRequest complete(int recordsDeleted, int recordsAnonymized) {
        if (this.status != RequestStatus.APPROVED) {
            throw new IllegalStateException("Can only complete approved requests");
        }
        return new DataDeletionRequest(
                id,
                athleteId,
                RequestStatus.COMPLETED,
                requestedAt,
                approvedAt,
                approvedBy,
                Instant.now(),
                recordsDeleted,
                recordsAnonymized,
                null,
                metadata
        );
    }

    public DataDeletionRequest reject(String reason) {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("Can only reject pending requests");
        }
        return new DataDeletionRequest(
                id,
                athleteId,
                RequestStatus.REJECTED,
                requestedAt,
                null,
                null,
                null,
                0,
                0,
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

    public int totalRecordsProcessed() {
        return recordsDeleted + recordsAnonymized;
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
