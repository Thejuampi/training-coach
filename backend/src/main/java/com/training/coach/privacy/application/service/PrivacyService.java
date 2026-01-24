package com.training.coach.privacy.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.privacy.application.port.out.ConsentLogRepository;
import com.training.coach.privacy.application.port.out.DataDeletionRequestRepository;
import com.training.coach.privacy.application.port.out.DataExportRequestRepository;
import com.training.coach.privacy.domain.model.ConsentLog;
import com.training.coach.privacy.domain.model.DataDeletionRequest;
import com.training.coach.privacy.domain.model.DataExportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling GDPR data export and deletion requests.
 */
@Service
public class PrivacyService {

    private static final Logger logger = LoggerFactory.getLogger(PrivacyService.class);

    private final DataExportRequestRepository exportRequestRepository;
    private final DataDeletionRequestRepository deletionRequestRepository;
    private final ConsentLogRepository consentLogRepository;
    private final AthleteRepository athleteRepository;

    public PrivacyService(
            DataExportRequestRepository exportRequestRepository,
            DataDeletionRequestRepository deletionRequestRepository,
            ConsentLogRepository consentLogRepository,
            AthleteRepository athleteRepository) {
        this.exportRequestRepository = exportRequestRepository;
        this.deletionRequestRepository = deletionRequestRepository;
        this.consentLogRepository = consentLogRepository;
        this.athleteRepository = athleteRepository;
    }

    // === Data Export Operations ===

    /**
     * Request a data export for an athlete.
     */
    public DataExportRequest requestExport(String athleteId) {
        athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        DataExportRequest request = DataExportRequest.create(athleteId);
        DataExportRequest saved = exportRequestRepository.save(request);

        // Log the request
        ConsentLog log = ConsentLog.create(
                athleteId,
                ConsentLog.ConsentAction.DATA_EXPORT_REQUESTED,
                "EXPORT_REQUEST",
                athleteId,
                request.id(),
                "Athlete requested data export"
        );
        consentLogRepository.save(log);

        logger.info("Data export requested for athlete: {}", athleteId);
        return saved;
    }

    /**
     * Approve a data export request.
     */
    public DataExportRequest approveExport(String requestId, String approvedBy) {
        DataExportRequest request = exportRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Export request not found: " + requestId));

        DataExportRequest approved = request.approve(approvedBy);
        DataExportRequest saved = exportRequestRepository.save(approved);

        // Log the approval
        ConsentLog log = ConsentLog.create(
                request.athleteId(),
                ConsentLog.ConsentAction.DATA_EXPORT_APPROVED,
                "EXPORT_APPROVED",
                approvedBy,
                requestId,
                String.format("Export approved by %s", approvedBy)
        );
        consentLogRepository.save(log);

        logger.info("Data export request {} approved by {}", requestId, approvedBy);
        return saved;
    }

    /**
     * Reject a data export request.
     */
    public DataExportRequest rejectExport(String requestId, String approvedBy, String reason) {
        DataExportRequest request = exportRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Export request not found: " + requestId));

        DataExportRequest rejected = request.reject(reason);
        DataExportRequest saved = exportRequestRepository.save(rejected);

        // Log the rejection
        ConsentLog log = ConsentLog.create(
                request.athleteId(),
                ConsentLog.ConsentAction.DATA_EXPORT_REJECTED,
                "EXPORT_REJECTED",
                approvedBy,
                requestId,
                String.format("Export rejected by %s. Reason: %s", approvedBy, reason)
        );
        consentLogRepository.save(log);

        logger.info("Data export request {} rejected by {}", requestId, approvedBy);
        return saved;
    }

    /**
     * Process an approved export request and generate the export file.
     */
    public DataExportRequest processExport(String requestId) {
        DataExportRequest request = exportRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Export request not found: " + requestId));

        if (!request.isApproved()) {
            throw new IllegalStateException("Export request must be approved before processing");
        }

        // In a real implementation, this would:
        // 1. Gather all athlete data (profile, wellness, activities, plans, etc.)
        // 2. Create a ZIP archive with JSON files for each data type
        // 3. Upload to secure storage
        // 4. Return the file path

        String exportFilePath = String.format("/exports/athlete_%s_%s.zip",
                request.athleteId(), UUID.randomUUID());

        DataExportRequest completed = request.complete(exportFilePath);
        DataExportRequest saved = exportRequestRepository.save(completed);

        // Log the completion
        ConsentLog log = ConsentLog.create(
                request.athleteId(),
                ConsentLog.ConsentAction.DATA_EXPORT_COMPLETED,
                "EXPORT_COMPLETED",
                "system",
                requestId,
                String.format("Export file generated: %s", exportFilePath)
        );
        consentLogRepository.save(log);

        logger.info("Data export request {} completed. File: {}", requestId, exportFilePath);
        return saved;
    }

    /**
     * Get a data export request by ID.
     */
    public DataExportRequest getExportRequest(String requestId) {
        return exportRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Export request not found: " + requestId));
    }

    /**
     * Get all export requests for an athlete.
     */
    public List<DataExportRequest> getExportRequestsForAthlete(String athleteId) {
        return exportRequestRepository.findByAthleteId(athleteId);
    }

    /**
     * Get all pending export requests.
     */
    public List<DataExportRequest> getPendingExportRequests() {
        return exportRequestRepository.findPending();
    }

    // === Data Deletion Operations ===

    /**
     * Request data deletion for an athlete.
     */
    public DataDeletionRequest requestDeletion(String athleteId) {
        athleteRepository.findById(athleteId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found: " + athleteId));

        DataDeletionRequest request = DataDeletionRequest.create(athleteId);
        DataDeletionRequest saved = deletionRequestRepository.save(request);

        // Log the request
        ConsentLog log = ConsentLog.create(
                athleteId,
                ConsentLog.ConsentAction.DATA_DELETION_REQUESTED,
                "DELETION_REQUEST",
                athleteId,
                request.id(),
                "Athlete requested data deletion"
        );
        consentLogRepository.save(log);

        logger.info("Data deletion requested for athlete: {}", athleteId);
        return saved;
    }

    /**
     * Approve a data deletion request.
     */
    public DataDeletionRequest approveDeletion(String requestId, String approvedBy) {
        DataDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found: " + requestId));

        DataDeletionRequest approved = request.approve(approvedBy);
        DataDeletionRequest saved = deletionRequestRepository.save(approved);

        // Log the approval
        ConsentLog log = ConsentLog.create(
                request.athleteId(),
                ConsentLog.ConsentAction.DATA_DELETION_APPROVED,
                "DELETION_APPROVED",
                approvedBy,
                requestId,
                String.format("Deletion approved by %s", approvedBy)
        );
        consentLogRepository.save(log);

        logger.info("Data deletion request {} approved by {}", requestId, approvedBy);
        return saved;
    }

    /**
     * Reject a data deletion request.
     */
    public DataDeletionRequest rejectDeletion(String requestId, String approvedBy, String reason) {
        DataDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found: " + requestId));

        DataDeletionRequest rejected = request.reject(reason);
        DataDeletionRequest saved = deletionRequestRepository.save(rejected);

        // Log the rejection
        ConsentLog log = ConsentLog.create(
                request.athleteId(),
                ConsentLog.ConsentAction.DATA_DELETION_REJECTED,
                "DELETION_REJECTED",
                approvedBy,
                requestId,
                String.format("Deletion rejected by %s. Reason: %s", approvedBy, reason)
        );
        consentLogRepository.save(log);

        logger.info("Data deletion request {} rejected by {}", requestId, approvedBy);
        return saved;
    }

    /**
     * Process an approved deletion request.
     */
    public DataDeletionRequest processDeletion(String requestId) {
        DataDeletionRequest request = deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found: " + requestId));

        if (!request.isApproved()) {
            throw new IllegalStateException("Deletion request must be approved before processing");
        }

        // In a real implementation, this would:
        // 1. Identify all data for the athlete across all bounded contexts
        // 2. Delete or anonymize records based on retention requirements
        // 3. Keep audit logs (consent logs) for legal compliance
        // 4. Return counts of deleted and anonymized records

        int recordsDeleted = 0;  // Placeholder
        int recordsAnonymized = 0;  // Placeholder

        DataDeletionRequest completed = request.complete(recordsDeleted, recordsAnonymized);
        DataDeletionRequest saved = deletionRequestRepository.save(completed);

        // Log the completion
        ConsentLog log = ConsentLog.create(
                request.athleteId(),
                ConsentLog.ConsentAction.DATA_DELETION_COMPLETED,
                "DELETION_COMPLETED",
                "system",
                requestId,
                String.format("Deletion completed. Deleted: %d, Anonymized: %d",
                        recordsDeleted, recordsAnonymized)
        );
        consentLogRepository.save(log);

        logger.info("Data deletion request {} completed. Deleted: {}, Anonymized: {}",
                requestId, recordsDeleted, recordsAnonymized);
        return saved;
    }

    /**
     * Get a data deletion request by ID.
     */
    public DataDeletionRequest getDeletionRequest(String requestId) {
        return deletionRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Deletion request not found: " + requestId));
    }

    /**
     * Get all deletion requests for an athlete.
     */
    public List<DataDeletionRequest> getDeletionRequestsForAthlete(String athleteId) {
        return deletionRequestRepository.findByAthleteId(athleteId);
    }

    /**
     * Get all pending deletion requests.
     */
    public List<DataDeletionRequest> getPendingDeletionRequests() {
        return deletionRequestRepository.findPending();
    }

    // === Consent Log Operations ===

    /**
     * Get consent logs for an athlete.
     */
    public List<ConsentLog> getConsentLogsForAthlete(String athleteId) {
        return consentLogRepository.findByAthleteId(athleteId);
    }

    /**
     * Get consent logs for an athlete within a date range.
     */
    public List<ConsentLog> getConsentLogsForAthleteInDateRange(
            String athleteId, Instant startDate, Instant endDate) {
        return consentLogRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate);
    }

    /**
     * Get recent consent logs for an athlete.
     */
    public List<ConsentLog> getRecentConsentLogsForAthlete(String athleteId, int limit) {
        return consentLogRepository.findRecentByAthleteId(athleteId, limit);
    }

    /**
     * Get all logs for a specific request.
     */
    public List<ConsentLog> getLogsForRequest(String requestId) {
        return consentLogRepository.findByRequestId(requestId);
    }
}
