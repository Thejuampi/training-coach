package com.training.coach.athlete.presentation;

import com.training.coach.athlete.application.service.AvailabilityService;
import com.training.coach.athlete.domain.model.AvailabilityTemplate;
import com.training.coach.athlete.domain.model.TravelException;
import com.training.coach.shared.domain.unit.Hours;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * REST controller for availability and calendar operations.
 */
@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Create a new availability template.
     */
    @PostMapping("/templates")
    public ResponseEntity<AvailabilityTemplate> createTemplate(
            @RequestParam String athleteId,
            @RequestBody Set<DayOfWeek> availableDays,
            @RequestParam Hours weeklyTargetHours,
            @RequestParam String name
    ) {
        AvailabilityTemplate template = availabilityService.createTemplate(
                athleteId,
                availableDays,
                weeklyTargetHours,
                name
        );
        return ResponseEntity.ok(template);
    }

    /**
     * Update an availability template.
     */
    @PutMapping("/templates/{templateId}")
    public ResponseEntity<AvailabilityTemplate> updateTemplate(
            @PathVariable String templateId,
            @RequestBody Set<DayOfWeek> availableDays,
            @RequestParam Hours weeklyTargetHours
    ) {
        AvailabilityTemplate template = availabilityService.updateTemplate(
                templateId,
                availableDays,
                weeklyTargetHours
        );
        return ResponseEntity.ok(template);
    }

    /**
     * Get the active template for an athlete.
     */
    @GetMapping("/templates/active/{athleteId}")
    public ResponseEntity<AvailabilityTemplate> getActiveTemplate(@PathVariable String athleteId) {
        AvailabilityTemplate template = availabilityService.getActiveTemplate(athleteId);
        return ResponseEntity.ok(template);
    }

    /**
     * Add a travel exception.
     */
    @PostMapping("/exceptions")
    public ResponseEntity<TravelException> addException(
            @RequestParam String athleteId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam TravelException.ExceptionType type,
            @RequestParam(required = false) String description
    ) {
        TravelException exception = availabilityService.addException(
                athleteId,
                startDate,
                endDate,
                type,
                description
        );
        return ResponseEntity.ok(exception);
    }

    /**
     * Find conflicting workouts for a date range.
     */
    @GetMapping("/conflicts/{athleteId}")
    public ResponseEntity<List<LocalDate>> findConflictingWorkouts(
            @PathVariable String athleteId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<LocalDate> conflicts = availabilityService.findConflictingWorkouts(
                athleteId,
                startDate,
                endDate
        );
        return ResponseEntity.ok(conflicts);
    }

    /**
     * Auto-reschedule workouts for exception dates.
     */
    @PostMapping("/reschedule/{athleteId}")
    public ResponseEntity<TravelAvailabilityService.RescheduleResult> autoReschedule(
            @PathVariable String athleteId,
            @RequestParam LocalDate exceptionStart,
            @RequestParam LocalDate exceptionEnd
    ) {
        TravelAvailabilityService.RescheduleResult result = availabilityService.autoReschedule(
                athleteId,
                exceptionStart,
                exceptionEnd
        );
        return ResponseEntity.ok(result);
    }

    /**
     * Resolve a travel exception.
     */
    @PostMapping("/exceptions/{exceptionId}/resolve")
    public ResponseEntity<TravelException> resolveException(@PathVariable String exceptionId) {
        TravelException exception = availabilityService.resolveException(exceptionId);
        return ResponseEntity.ok(exception);
    }

    /**
     * Cancel a travel exception.
     */
    @PostMapping("/exceptions/{exceptionId}/cancel")
    public ResponseEntity<TravelException> cancelException(@PathVariable String exceptionId) {
        TravelException exception = availabilityService.cancelException(exceptionId);
        return ResponseEntity.ok(exception);
    }

    /**
     * Get all exceptions for an athlete.
     */
    @GetMapping("/exceptions/{athleteId}")
    public ResponseEntity<List<TravelException>> getExceptions(@PathVariable String athleteId) {
        List<TravelException> exceptions = availabilityService.getExceptions(athleteId);
        return ResponseEntity.ok(exceptions);
    }

    /**
     * Get active exceptions for an athlete.
     */
    @GetMapping("/exceptions/{athleteId}/active")
    public ResponseEntity<List<TravelException>> getActiveExceptions(@PathVariable String athleteId) {
        List<TravelException> exceptions = availabilityService.getActiveExceptions(athleteId);
        return ResponseEntity.ok(exceptions);
    }

    /**
     * Check if a date is available for training.
     */
    @GetMapping("/available/{athleteId}")
    public ResponseEntity<Boolean> isAvailable(
            @PathVariable String athleteId,
            @RequestParam LocalDate date
    ) {
        boolean available = availabilityService.isAvailable(athleteId, date);
        return ResponseEntity.ok(available);
    }
}