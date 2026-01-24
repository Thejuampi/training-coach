package com.training.coach.admin.presentation;

import com.training.coach.athlete.application.service.AthleteService;
import com.training.coach.athlete.application.port.out.ActivityRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.integration.application.service.IntegrationService;
import com.training.coach.shared.functional.Result;
import com.training.coach.user.application.service.SystemUserService;
import com.training.coach.user.domain.model.SystemUser;
import com.training.coach.wellness.application.port.out.WellnessRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for admin operations.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SystemUserService userService;
    private final AthleteService athleteService;
    private final ActivityRepository activityRepository;
    private final WellnessRepository wellnessRepository;
    private final IntegrationService integrationService;

    public AdminController(
            SystemUserService userService,
            AthleteService athleteService,
            ActivityRepository activityRepository,
            WellnessRepository wellnessRepository,
            IntegrationService integrationService) {
        this.userService = userService;
        this.athleteService = athleteService;
        this.activityRepository = activityRepository;
        this.wellnessRepository = wellnessRepository;
        this.integrationService = integrationService;
    }

    /**
     * Create a new user with specified role.
     */
    @PostMapping("/users")
    public ResponseEntity<SystemUser> createUser(@RequestBody AdminCreateUserRequest request) {
        var result = userService.createUser(
                request.name(),
                request.role(),
                request.preferences(),
                request.username(),
                request.password());

        if (result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get all users in the system.
     */
    @GetMapping("/users")
    public ResponseEntity<List<SystemUser>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * View user credentials status without exposing secrets.
     */
    @GetMapping("/users/{id}/credentials")
    public ResponseEntity<SystemUserService.UserCredentialsSummary> viewUserCredentials(@PathVariable String id) {
        var result = userService.getCredentialsSummary(id);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Delete an athlete and all associated personal data (GDPR compliance).
     */
    @DeleteMapping("/athletes/{id}")
    public ResponseEntity<Void> deleteAthleteData(@PathVariable String id) {
        // First verify the athlete exists
        var athleteResult = athleteService.getAthlete(id);
        if (!athleteResult.isSuccess()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Delete all associated data
        activityRepository.deleteByAthleteId(id);
        wellnessRepository.deleteByAthleteId(id);

        // Then delete the athlete record
        athleteService.deleteAthlete(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * View integration status without exposing API keys.
     */
    @GetMapping("/integrations/{platformId}/status")
    public ResponseEntity<IntegrationStatusResponse> viewIntegrationStatus(@PathVariable String platformId) {
        var result = integrationService.getIntegrationStatus(platformId);
        if (result.isSuccess()) {
            var status = result.value().get();
            return ResponseEntity.ok(new IntegrationStatusResponse(
                    status.platformId(),
                    status.status(),
                    status.lastSync(),
                    status.errorCount(),
                    status.apiKey() == null || status.apiKey().isEmpty()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get integration health dashboard.
     */
    @GetMapping("/integrations/health")
    public ResponseEntity<List<IntegrationHealthResponse>> getIntegrationHealthDashboard() {
        var result = integrationService.getAllIntegrationHealth();
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.value().get().stream()
                    .map(health -> new IntegrationHealthResponse(
                            health.platformId(),
                            health.status(),
                            health.lastSync(),
                            health.errorCount(),
                            health.remediationSteps()
                    ))
                    .toList());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get organization summary report.
     */
    @GetMapping("/reports/organization")
    public ResponseEntity<OrganizationReportResponse> getOrganizationReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        var result = integrationService.getOrganizationReport(startDate, endDate);
        if (result.isSuccess()) {
            return ResponseEntity.ok(new OrganizationReportResponse(
                    result.value().get().totalAthletes(),
                    result.value().get().activeAthletes(),
                    result.value().get().averageReadiness(),
                    result.value().get().averageCompliance(),
                    result.value().get().recentActivities()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Request/Response DTOs
    public record AdminCreateUserRequest(
            String name,
            SystemUser.UserRole role,
            SystemUser.UserPreferences preferences,
            String username,
            String password) {}

    public record IntegrationStatusResponse(
            String platformId,
            String status,
            java.time.Instant lastSync,
            int errorCount,
            boolean apiKeyHidden) {}

    public record IntegrationHealthResponse(
            String platformId,
            String status,
            java.time.Instant lastSync,
            int errorCount,
            List<String> remediationSteps) {}

    public record OrganizationReportResponse(
            int totalAthletes,
            int activeAthletes,
            double averageReadiness,
            double averageCompliance,
            int recentActivities) {}
}