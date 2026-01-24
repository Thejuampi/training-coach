package com.training.coach.trainingplan.application.service;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.athlete.domain.model.TrainingMetrics;
import com.training.coach.athlete.domain.model.TrainingPreferences;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.shared.domain.unit.*;
import com.training.coach.trainingplan.application.port.out.PlanRepository;
import com.training.coach.trainingplan.domain.model.PlanSummary;
import com.training.coach.trainingplan.domain.model.PlanVersion;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Application service for plan lifecycle management.
 */
@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final TrainingPlanService trainingPlanService;
    private final AthleteRepository athleteRepository;

    public PlanService(PlanRepository planRepository, TrainingPlanService trainingPlanService, AthleteRepository athleteRepository) {
        this.planRepository = planRepository;
        this.trainingPlanService = trainingPlanService;
        this.athleteRepository = athleteRepository;
    }

    public PlanSummary createPlan(CreatePlanCommand command) {
        Athlete athlete = athleteRepository
                .findById(command.athleteId())
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found"));

        TrainingPlan generatedPlan = trainingPlanService.generatePlan(
                athlete, command.phase(), command.startDate(), command.targetWeeklyHours());

        String planId = UUID.randomUUID().toString();
        PlanSummary planSummary =
                new PlanSummary(planId, command.athleteId(), 1, PlanVersionStatus.DRAFT, Instant.now(), null);
        planRepository.save(planSummary);

        PlanVersion version = PlanVersion.create(planId, 1, generatedPlan.workouts());
        planRepository.saveVersion(version);

        return planSummary;
    }

    public PlanSummary publishPlan(String planId) {
        PlanSummary plan =
                planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        if (plan.status() != PlanVersionStatus.DRAFT) {
            throw new IllegalStateException("Only draft plans can be published");
        }
        planRepository.updateVersionStatus(planId, plan.currentVersion(), PlanVersionStatus.PUBLISHED);
        PlanSummary updated = new PlanSummary(
                plan.id(), plan.athleteId(), plan.currentVersion(), PlanVersionStatus.PUBLISHED, plan.createdAt(), Instant.now());
        planRepository.save(updated);
        return updated;
    }

    public PlanSummary revisePlan(RevisePlanCommand command) {
        PlanSummary plan =
                planRepository.findById(command.planId()).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        if (plan.status() != PlanVersionStatus.PUBLISHED) {
            throw new IllegalStateException("Only published plans can be revised");
        }
        // Regenerate with new weekly hours
        Athlete dummy = createDummyAthlete(command.planId());
        TrainingPreferences newPrefs = new TrainingPreferences(
                dummy.preferences().availableDays(),
                command.newWeeklyHours(),
                dummy.preferences().currentPhase());
        Athlete updatedDummy = new Athlete(dummy.id(), dummy.name(), dummy.profile(), dummy.currentMetrics(), newPrefs);
        TrainingPlan newPlan = trainingPlanService.generatePlan(
                updatedDummy, "base", LocalDate.of(2026, 1, 1), command.newWeeklyHours());
        int newVersion = plan.currentVersion() + 1;
        PlanVersion newVersionObj = PlanVersion.create(plan.id(), newVersion, newPlan.workouts());
        planRepository.saveVersion(newVersionObj);
        PlanSummary updated =
                new PlanSummary(plan.id(), plan.athleteId(), newVersion, PlanVersionStatus.DRAFT, plan.createdAt(), plan.publishedAt());
        planRepository.save(updated);
        return updated;
    }

    public List<PlanSummary> getPlansForAthlete(String athleteId) {
        return planRepository.findAll().stream()
                .filter(plan -> plan.athleteId().equals(athleteId) && plan.status() == PlanVersionStatus.PUBLISHED)
                .toList();
    }

    public List<PlanSummary> listPlans() {
        return planRepository.findAll();
    }

    public PlanVersion getPlanVersion(String planId, int version) {
        return planRepository
                .findVersion(planId, version)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));
    }

    /**
     * Get workout for a specific date from the published plan.
     */
    public Workout getWorkoutForDate(String athleteId, LocalDate date) {
        // Get published plans for the athlete
        var plans = getPlansForAthlete(athleteId);
        if (plans.isEmpty()) {
            return null;
        }

        // Get the latest published plan
        var latestPlan = plans.get(0);
        var planVersion = getPlanVersion(latestPlan.id(), latestPlan.currentVersion());

        // Find workout for the requested date
        return planVersion.workouts().stream()
                .filter(workout -> workout.date().equals(date))
                .findFirst()
                .orElse(null);
    }

    public PlanSummary archivePlan(String planId) {
        PlanSummary plan =
                planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        if (plan.status() != PlanVersionStatus.PUBLISHED) {
            throw new IllegalStateException("Only published plans can be archived");
        }
        planRepository.updateVersionStatus(planId, plan.currentVersion(), PlanVersionStatus.ARCHIVED);
        PlanSummary updated = new PlanSummary(
                plan.id(), plan.athleteId(), plan.currentVersion(), PlanVersionStatus.ARCHIVED, plan.createdAt(), plan.publishedAt());
        planRepository.save(updated);
        return updated;
    }

    // Placeholder for dummy athlete; in real impl, fetch from AthleteRepository
    private Athlete createDummyAthlete(String athleteId) {
        // Dummy implementation; replace with actual fetch
        TrainingPreferences preferences = new TrainingPreferences(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), Hours.of(8.0), "base");
        TrainingMetrics metrics = new TrainingMetrics(Watts.of(250.0), BeatsPerMinute.of(180.0), Vo2Max.of(45.0), Kilograms.of(75.0));
        return new Athlete(athleteId, "Dummy Athlete", null, metrics, preferences);
    }

    public record CreatePlanCommand(String athleteId, String phase, LocalDate startDate, Hours targetWeeklyHours) {}

    public record RevisePlanCommand(String planId, Hours newWeeklyHours) {}
}
