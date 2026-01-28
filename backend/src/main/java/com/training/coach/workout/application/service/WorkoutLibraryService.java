package com.training.coach.workout.application.service;

import com.training.coach.trainingplan.application.port.out.PlanRepository;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.athlete.domain.model.Workout.IntensityProfile;
import com.training.coach.athlete.domain.model.Workout.Interval;
import com.training.coach.athlete.domain.model.Workout.WorkoutType;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.shared.domain.unit.Percent;
import com.training.coach.shared.domain.unit.Watts;
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

        // Convert template type to workout type
        WorkoutType workoutType = mapWorkoutType(template.type());

        // Create a simple intensity profile based on template type
        IntensityProfile intensityProfile = createIntensityProfile(workoutType);

        // Create a single interval representing the entire workout
        Interval interval = new Interval(
            Interval.IntervalType.THRESHOLD,
            template.duration(),
            Watts.of(200), // Default power target
            BeatsPerMinute.of(150) // Default HR target
        );

        // Create workout from template
        Workout workout = new Workout(
                java.util.UUID.randomUUID().toString(),
                date,
                workoutType,
                template.duration(),
                intensityProfile,
                List.of(interval)
        );

        logger.info("Inserted template '{}' into plan {} on {}", template.name(), planId, date);
        return workout;
    }

    /**
     * Map template workout type to domain workout type.
     */
    private WorkoutType mapWorkoutType(WorkoutTemplate.WorkoutType templateType) {
        return switch (templateType) {
            case ENDURANCE -> WorkoutType.ENDURANCE;
            case INTERVALS -> WorkoutType.INTERVALS;
            case TEMPO, THRESHOLD -> WorkoutType.THRESHOLD;
            case RECOVERY -> WorkoutType.RECOVERY;
            default -> WorkoutType.ENDURANCE;
        };
    }

    /**
     * Create intensity profile based on workout type.
     */
    private IntensityProfile createIntensityProfile(WorkoutType type) {
        return switch (type) {
            case ENDURANCE -> new IntensityProfile(
                Percent.of(60), Percent.of(25), Percent.of(10), Percent.of(5), Percent.of(0)
            );
            case THRESHOLD -> new IntensityProfile(
                Percent.of(20), Percent.of(30), Percent.of(40), Percent.of(10), Percent.of(0)
            );
            case INTERVALS -> new IntensityProfile(
                Percent.of(30), Percent.of(20), Percent.of(10), Percent.of(30), Percent.of(10)
            );
            case RECOVERY -> new IntensityProfile(
                Percent.of(80), Percent.of(15), Percent.of(5), Percent.of(0), Percent.of(0)
            );
            default -> new IntensityProfile(
                Percent.of(50), Percent.of(30), Percent.of(15), Percent.of(5), Percent.of(0)
            );
        };
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