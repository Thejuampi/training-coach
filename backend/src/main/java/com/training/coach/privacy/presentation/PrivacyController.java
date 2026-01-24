package com.training.coach.privacy.presentation;

import com.training.coach.privacy.application.service.PrivacyService;
import com.training.coach.privacy.domain.model.ConsentLog;
import com.training.coach.privacy.domain.model.DataDeletionRequest;
import com.training.coach.privacy.domain.model.DataExportRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * REST controller for GDPR data export and deletion operations.
 */
@RestController
@RequestMapping("/api/privacy")
public class PrivacyController {

    private final PrivacyService privacyService;

    public PrivacyController(PrivacyService privacyService) {
        this.privacyService = privacyService;
    }

    // === Data Export Endpoints ===

    /**
     * Request a data export for an athlete.
     */
    @PostMapping("/export/request")
    public ResponseEntity<DataExportRequest> requestExport(@RequestParam String athleteId) {
        DataExportRequest request = privacyService.requestExport(athleteId);
        return ResponseEntity.ok(request);
    }

    /**
     * Approve a data export request.
     */
    @PostMapping("/export/{requestId}/approve")
    public ResponseEntity<DataExportRequest> approveExport(
            @PathVariable String requestId,
            @RequestParam String approvedBy
    ) {
        DataExportRequest request = privacyService.approveExport(requestId, approvedBy);
        return ResponseEntity.ok(request);
    }

    /**
     * Reject a data export request.
     */
    @PostMapping("/export/{requestId}/reject")
    public ResponseEntity<DataExportRequest> rejectExport(
            @PathVariable String requestId,
            @RequestParam String approvedBy,
            @RequestBody Map<String, String> body
    ) {
        String reason = body.getOrDefault("reason", "No reason provided");
        DataExportRequest request = privacyService.rejectExport(requestId, approvedBy, reason);
        return ResponseEntity.ok(request);
    }

    /**
     * Process an approved export request.
     */
    @PostMapping("/export/{requestId}/process")
    public ResponseEntity<DataExportRequest> processExport(@PathVariable String requestId) {
        DataExportRequest request = privacyService.processExport(requestId);
        return ResponseEntity.ok(request);
    }

    /**
     * Get a data export request by ID.
     */
    @GetMapping("/export/{requestId}")
    public ResponseEntity<DataExportRequest> getExportRequest(@PathVariable String requestId) {
        DataExportRequest request = privacyService.getExportRequest(requestId);
        return ResponseEntity.ok(request);
    }

    /**
     * Get all export requests for an athlete.
     */
    @GetMapping("/export/athlete/{athleteId}")
    public ResponseEntity<List<DataExportRequest>> getExportRequestsForAthlete(
            @PathVariable String athleteId) {
        List<DataExportRequest> requests = privacyService.getExportRequestsForAthlete(athleteId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get all pending export requests (admin endpoint).
     */
    @GetMapping("/export/pending")
    public ResponseEntity<List<DataExportRequest>> getPendingExportRequests() {
        List<DataExportRequest> requests = privacyService.getPendingExportRequests();
        return ResponseEntity.ok(requests);
    }

    // === Data Deletion Endpoints ===

    /**
     * Request data deletion for an athlete.
     */
    @PostMapping("/deletion/request")
    public ResponseEntity<DataDeletionRequest> requestDeletion(@RequestParam String athleteId) {
        DataDeletionRequest request = privacyService.requestDeletion(athleteId);
        return ResponseEntity.ok(request);
    }

    /**
     * Approve a data deletion request.
     */
    @PostMapping("/deletion/{requestId}/approve")
    public ResponseEntity<DataDeletionRequest> approveDeletion(
            @PathVariable String requestId,
            @RequestParam String approvedBy
    ) {
        DataDeletionRequest request = privacyService.approveDeletion(requestId, approvedBy);
        return ResponseEntity.ok(request);
    }

    /**
     * Reject a data deletion request.
     */
    @PostMapping("/deletion/{requestId}/reject")
    public ResponseEntity<DataDeletionRequest> rejectDeletion(
            @PathVariable String requestId,
            @RequestParam String approvedBy,
            @RequestBody Map<String, String> body
    ) {
        String reason = body.getOrDefault("reason", "No reason provided");
        DataDeletionRequest request = privacyService.rejectDeletion(requestId, approvedBy, reason);
        return ResponseEntity.ok(request);
    }

    /**
     * Process an approved deletion request.
     */
    @PostMapping("/deletion/{requestId}/process")
    public ResponseEntity<DataDeletionRequest> processDeletion(@PathVariable String requestId) {
        DataDeletionRequest request = privacyService.processDeletion(requestId);
        return ResponseEntity.ok(request);
    }

    /**
     * Get a data deletion request by ID.
     */
    @GetMapping("/deletion/{requestId}")
    public ResponseEntity<DataDeletionRequest> getDeletionRequest(@PathVariable String requestId) {
        DataDeletionRequest request = privacyService.getDeletionRequest(requestId);
        return ResponseEntity.ok(request);
    }

    /**
     * Get all deletion requests for an athlete.
     */
    @GetMapping("/deletion/athlete/{athleteId}")
    public ResponseEntity<List<DataDeletionRequest>> getDeletionRequestsForAthlete(
            @PathVariable String athleteId) {
        List<DataDeletionRequest> requests = privacyService.getDeletionRequestsForAthlete(athleteId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get all pending deletion requests (admin endpoint).
     */
    @GetMapping("/deletion/pending")
    public ResponseEntity<List<DataDeletionRequest>> getPendingDeletionRequests() {
        List<DataDeletionRequest> requests = privacyService.getPendingDeletionRequests();
        return ResponseEntity.ok(requests);
    }

    // === Consent Log Endpoints ===

    /**
     * Get consent logs for an athlete.
     */
    @GetMapping("/consent-logs/athlete/{athleteId}")
    public ResponseEntity<List<ConsentLog>> getConsentLogsForAthlete(
            @PathVariable String athleteId) {
        List<ConsentLog> logs = privacyService.getConsentLogsForAthlete(athleteId);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get consent logs for an athlete within a date range.
     */
    @GetMapping("/consent-logs/athlete/{athleteId}/range")
    public ResponseEntity<List<ConsentLog>> getConsentLogsForDateRange(
            @PathVariable String athleteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate
    ) {
        List<ConsentLog> logs = privacyService.getConsentLogsForAthleteInDateRange(
                athleteId, startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get recent consent logs for an athlete.
     */
    @GetMapping("/consent-logs/athlete/{athleteId}/recent")
    public ResponseEntity<List<ConsentLog>> getRecentConsentLogs(
            @PathVariable String athleteId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        List<ConsentLog> logs = privacyService.getRecentConsentLogsForAthlete(athleteId, limit);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get all logs for a specific request.
     */
    @GetMapping("/consent-logs/request/{requestId}")
    public ResponseEntity<List<ConsentLog>> getLogsForRequest(@PathVariable String requestId) {
        List<ConsentLog> logs = privacyService.getLogsForRequest(requestId);
        return ResponseEntity.ok(logs);
    }
}
