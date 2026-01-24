package com.training.coach.workout.presentation;

import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.workout.application.service.WorkoutLibraryService;
import com.training.coach.workout.domain.model.WorkoutTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * REST controller for workout library operations.
 */
@RestController
@RequestMapping("/api/workout-library")
public class WorkoutLibraryController {

    private final WorkoutLibraryService workoutLibraryService;

    public WorkoutLibraryController(WorkoutLibraryService workoutLibraryService) {
        this.workoutLibraryService = workoutLibraryService;
    }

    /**
     * Create a new workout template.
     */
    @PostMapping("/templates")
    public ResponseEntity<WorkoutTemplate> createTemplate(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam WorkoutTemplate.WorkoutType type,
            @RequestParam Minutes duration,
            @RequestParam String intensityProfile,
            @RequestParam String coachId
    ) {
        WorkoutTemplate template = workoutLibraryService.createTemplate(
                name,
                description,
                type,
                duration,
                intensityProfile,
                coachId
        );
        return ResponseEntity.ok(template);
    }

    /**
     * Update template tags.
     */
    @PutMapping("/templates/{templateId}/tags")
    public ResponseEntity<WorkoutTemplate> updateTemplateTags(
            @PathVariable String templateId,
            @RequestBody Set<String> tags
    ) {
        WorkoutTemplate template = workoutLibraryService.updateTemplateTags(
                templateId,
                tags,
                Set.of(),
                Set.of()
        );
        return ResponseEntity.ok(template);
    }

    /**
     * Update template phases and purposes.
     */
    @PutMapping("/templates/{templateId}/metadata")
    public ResponseEntity<WorkoutTemplate> updateTemplateMetadata(
            @PathVariable String templateId,
            @RequestParam(required = false) Set<String> phases,
            @RequestParam(required = false) Set<String> purposes,
            @RequestParam(required = false) Boolean keySession
    ) {
        WorkoutTemplate template = workoutLibraryService.updateTemplateTags(
                templateId,
                Set.of(),
                phases != null ? phases : Set.of(),
                purposes != null ? purposes : Set.of()
        );

        if (Boolean.TRUE.equals(keySession)) {
            template = workoutLibraryService.markAsKeySession(templateId);
        }

        return ResponseEntity.ok(template);
    }

    /**
     * Publish a template.
     */
    @PostMapping("/templates/{templateId}/publish")
    public ResponseEntity<WorkoutTemplate> publishTemplate(@PathVariable String templateId) {
        WorkoutTemplate template = workoutLibraryService.publishTemplate(templateId);
        return ResponseEntity.ok(template);
    }

    /**
     * Deprecate a template.
     */
    @PostMapping("/templates/{templateId}/deprecate")
    public ResponseEntity<WorkoutTemplate> deprecateTemplate(@PathVariable String templateId) {
        WorkoutTemplate template = workoutLibraryService.deprecateTemplate(templateId);
        return ResponseEntity.ok(template);
    }

    /**
     * Create a new version of a template.
     */
    @PostMapping("/templates/{templateId}/versions")
    public ResponseEntity<WorkoutTemplate> createNewVersion(
            @PathVariable String templateId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam Minutes duration,
            @RequestParam String intensityProfile
    ) {
        WorkoutTemplate newVersion = workoutLibraryService.createNewVersion(
                templateId,
                name,
                description,
                duration,
                intensityProfile
        );
        return ResponseEntity.ok(newVersion);
    }

    /**
     * Get all versions of a template.
     */
    @GetMapping("/templates/{templateId}/versions")
    public ResponseEntity<List<WorkoutTemplate>> getTemplateVersions(@PathVariable String templateId) {
        List<WorkoutTemplate> versions = workoutLibraryService.getTemplateVersions(templateId);
        return ResponseEntity.ok(versions);
    }

    /**
     * Insert template into plan.
     */
    @PostMapping("/templates/{templateId}/insert")
    public ResponseEntity<Workout> insertTemplateIntoPlan(
            @PathVariable String templateId,
            @RequestParam String planId,
            @RequestParam LocalDate date
    ) {
        Workout workout = workoutLibraryService.insertTemplateIntoPlan(templateId, planId, date);
        return ResponseEntity.ok(workout);
    }

    /**
     * Enable sharing for a template.
     */
    @PostMapping("/templates/{templateId}/share")
    public ResponseEntity<WorkoutTemplate> enableSharing(@PathVariable String templateId) {
        WorkoutTemplate template = workoutLibraryService.enableSharing(templateId);
        return ResponseEntity.ok(template);
    }

    /**
     * Copy a template to your library.
     */
    @PostMapping("/templates/{templateId}/copy")
    public ResponseEntity<WorkoutTemplate> copyTemplate(
            @PathVariable String templateId,
            @RequestParam String toCoachId
    ) {
        WorkoutTemplate copy = workoutLibraryService.copyTemplate(templateId, toCoachId);
        return ResponseEntity.ok(copy);
    }

    /**
     * Get all templates for a coach.
     */
    @GetMapping("/templates/coach/{coachId}")
    public ResponseEntity<List<WorkoutTemplate>> getCoachTemplates(@PathVariable String coachId) {
        List<WorkoutTemplate> templates = workoutLibraryService.getCoachTemplates(coachId);
        return ResponseEntity.ok(templates);
    }

    /**
     * Get all available templates.
     */
    @GetMapping("/templates/available")
    public ResponseEntity<List<WorkoutTemplate>> getAvailableTemplates() {
        List<WorkoutTemplate> templates = workoutLibraryService.getAvailableTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * Get globally shared templates.
     */
    @GetMapping("/templates/shared")
    public ResponseEntity<List<WorkoutTemplate>> getSharedTemplates() {
        List<WorkoutTemplate> templates = workoutLibraryService.getSharedTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * Filter templates by tags.
     */
    @GetMapping("/templates/filter/tags")
    public ResponseEntity<List<WorkoutTemplate>> filterByTags(@RequestParam Set<String> tags) {
        List<WorkoutTemplate> templates = workoutLibraryService.filterByTags(tags);
        return ResponseEntity.ok(templates);
    }

    /**
     * Filter templates by phase.
     */
    @GetMapping("/templates/filter/phase")
    public ResponseEntity<List<WorkoutTemplate>> filterByPhase(@RequestParam String phase) {
        List<WorkoutTemplate> templates = workoutLibraryService.filterByPhase(phase);
        return ResponseEntity.ok(templates);
    }

    /**
     * Filter templates by purpose.
     */
    @GetMapping("/templates/filter/purpose")
    public ResponseEntity<List<WorkoutTemplate>> filterByPurpose(@RequestParam String purpose) {
        List<WorkoutTemplate> templates = workoutLibraryService.filterByPurpose(purpose);
        return ResponseEntity.ok(templates);
    }

    /**
     * Get a specific template.
     */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<WorkoutTemplate> getTemplate(@PathVariable String templateId) {
        return workoutLibraryService.getTemplate(templateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}