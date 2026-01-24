package com.training.coach.workout.application.service;

import com.training.coach.athlete.application.port.out.PlanRepository;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.workout.application.port.out.WorkoutTemplateRepository;
import com.training.coach.workout.domain.model.WorkoutTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing workout template library.
 */
@Service
public class WorkoutLibraryService {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutLibraryService.class);

    private final WorkoutTemplateRepository templateRepository;
    private final PlanRepository planRepository;

    public WorkoutLibraryService(
            WorkoutTemplateRepository templateRepository,
            PlanRepository planRepository) {
        this.templateRepository = templateRepository;
        this.planRepository = planRepository;
    }

    /**
     * Create a new workout template.
     */
    public WorkoutTemplate createTemplate(
            String name,
            String description,
            WorkoutTemplate.WorkoutType type,
            Minutes duration,
            String intensityProfile,
            String coachId
    ) {
        WorkoutTemplate template = WorkoutTemplate.create(
                name,
                description,
                type,
                duration,
                intensityProfile,
                coachId
        );

        WorkoutTemplate saved = templateRepository.save(template);
        logger.info("Created workout template '{}' for coach {}", name, coachId);
        return saved;
    }

    /**
     * Update template with tags, phases, and purposes.
     */
    public WorkoutTemplate updateTemplateTags(
            String templateId,
            Set<String> tags,
            Set<String> phases,
            Set<String> purposes
    ) {
        WorkoutTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        if (!tags.isEmpty()) {
            template = template.withTags(tags);
        }
        if (!phases.isEmpty()) {
            template = template.withPhases(phases);
        }
        if (!purposes.isEmpty()) {
            template = template.withPurposes(purposes);
        }

        WorkoutTemplate saved = templateRepository.save(template);
        logger.info("Updated tags for template {}", templateId);
        return saved;
    }

    /**
     * Mark template as key session.
     */
    public WorkoutTemplate markAsKeySession(String templateId) {
        WorkoutTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        WorkoutTemplate updated = template.markAsKeySession();
        WorkoutTemplate saved = templateRepository.save(updated);
        logger.info("Marked template {} as key session", templateId);
        return saved;
    }

    /**
     * Publish a template.
     */
    public WorkoutTemplate publishTemplate(String templateId) {
        WorkoutTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        WorkoutTemplate published = template.publish();
        WorkoutTemplate saved = templateRepository.save(published);
        logger.info("Published workout template '{}'", template.name());
        return saved;
    }

    /**
     * Deprecate a template.
     */
    public WorkoutTemplate deprecateTemplate(String templateId) {
        WorkoutTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        WorkoutTemplate deprecated = template.deprecate();
        WorkoutTemplate saved = templateRepository.save(deprecated);
        logger.info("Deprecated workout template '{}'", template.name());
        return saved;
    }

    /**
     * Create a new version of an existing template.
     */
    public WorkoutTemplate createNewVersion(
            String templateId,
            String name,
            String description,
            Minutes duration,
            String intensityProfile
    ) {
        WorkoutTemplate original = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        WorkoutTemplate newVersion = original.createNewVersion(
                name,
                description,
                duration,
                intensityProfile
        );

        WorkoutTemplate saved = templateRepository.save(newVersion);
        logger.info("Created version {} of template '{}'", newVersion.version(), original.name());
        return saved;
    }

    /**
     * Insert a template into a plan as a workout.
     */
    public Workout insertTemplateIntoPlan(
            String templateId,
            String planId,
            LocalDate date
    ) {
        WorkoutTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        if (!template.isAvailable()) {
            throw new IllegalStateException("Template is not available for use");
        }

        // Create workout from template
        Workout workout = new Workout(
                java.util.UUID.randomUUID().toString(),
                date,
                template.type().name(),
                template.duration(),
                template.intensityProfile()
        );

        logger.info("Inserted template '{}' into plan {} on {}", template.name(), planId, date);
        return workout;
    }

    /**
     * Enable global sharing for a template.
     */
    public WorkoutTemplate enableSharing(String templateId) {
        WorkoutTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        if (template.status() != WorkoutTemplate.TemplateStatus.PUBLISHED) {
            throw new IllegalStateException("Only published templates can be shared");
        }

        WorkoutTemplate shared = template.enableSharing();
        WorkoutTemplate saved = templateRepository.save(shared);
        logger.info("Enabled global sharing for template '{}'", template.name());
        return saved;
    }

    /**
     * Copy a template from another coach to your library.
     */
    public WorkoutTemplate copyTemplate(String templateId, String toCoachId) {
        WorkoutTemplate original = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        // Create a copy with new ID and coach
        WorkoutTemplate copy = new WorkoutTemplate(
                java.util.UUID.randomUUID().toString(),
                original.name(),
                original.description(),
                original.type(),
                original.duration(),
                original.intensityProfile(),
                WorkoutTemplate.TemplateStatus.DRAFT,
                1,
                toCoachId,
                new HashSet<>(original.tags()),
                new HashSet<>(original.phases()),
                new HashSet<>(original.purposes()),
                original.isKeySession(),
                java.time.Instant.now(),
                java.time.Instant.now(),
                null,
                templateId,  // Track original template
                false,
                false
        );

        WorkoutTemplate saved = templateRepository.save(copy);
        logger.info("Copied template '{}' from {} to {}", original.name(), original.coachId(), toCoachId);
        return saved;
    }

    /**
     * Get all templates for a coach.
     */
    public List<WorkoutTemplate> getCoachTemplates(String coachId) {
        return templateRepository.findByCoachId(coachId);
    }

    /**
     * Get all available templates (published and not deprecated).
     */
    public List<WorkoutTemplate> getAvailableTemplates() {
        return templateRepository.findAvailable();
    }

    /**
     * Get globally shared templates.
     */
    public List<WorkoutTemplate> getSharedTemplates() {
        return templateRepository.findGloballyShared();
    }

    /**
     * Filter templates by tags.
     */
    public List<WorkoutTemplate> filterByTags(Set<String> tags) {
        return templateRepository.findByTags(tags);
    }

    /**
     * Filter templates by phase.
     */
    public List<WorkoutTemplate> filterByPhase(String phase) {
        return templateRepository.findByPhase(phase);
    }

    /**
     * Filter templates by purpose.
     */
    public List<WorkoutTemplate> filterByPurpose(String purpose) {
        return templateRepository.findByPurpose(purpose);
    }

    /**
     * Get all versions of a template.
     */
    public List<WorkoutTemplate> getTemplateVersions(String templateId) {
        // Include the current template
        WorkoutTemplate current = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));

        List<WorkoutTemplate> versions = templateRepository.findVersionsByParentId(templateId);
        List<WorkoutTemplate> allVersions = new java.util.ArrayList<>(versions);
        allVersions.add(current);

        return allVersions.stream()
                .sorted((a, b) -> Integer.compare(b.version(), a.version()))
                .toList();
    }

    /**
     * Get a specific template by ID.
     */
    public Optional<WorkoutTemplate> getTemplate(String templateId) {
        return templateRepository.findById(templateId);
    }
}