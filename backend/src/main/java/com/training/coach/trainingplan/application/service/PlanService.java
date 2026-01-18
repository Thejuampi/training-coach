package com.training.coach.trainingplan.application.service;

import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.trainingplan.application.port.out.PlanRepository;
import com.training.coach.trainingplan.domain.model.PlanSummary;
import com.training.coach.trainingplan.domain.model.PlanVersion;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import java.time.Instant;
import java.time.LocalDate;
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

    public PlanService(PlanRepository planRepository, TrainingPlanService trainingPlanService) {
        this.planRepository = planRepository;
        this.trainingPlanService = trainingPlanService;
    }

    public PlanSummary createPlan(CreatePlanCommand command) {
        // For MVP, assume athlete is fetched by ID; here we create a dummy or assume provided
        // In real impl, inject AthleteRepository and fetch by command.athleteId
        Athlete dummyAthlete = createDummyAthlete(command.athleteId()); // Placeholder

        TrainingPlan generatedPlan = trainingPlanService.generatePlan(
                dummyAthlete, command.phase(), command.startDate(), command.targetWeeklyHours());

        String planId = UUID.randomUUID().toString();
        PlanSummary planSummary =
                new PlanSummary(planId, command.athleteId(), 1, PlanVersionStatus.DRAFT, Instant.now());
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
        return new PlanSummary(
                plan.id(), plan.athleteId(), plan.currentVersion(), PlanVersionStatus.PUBLISHED, plan.createdAt());
    }

    public PlanSummary revisePlan(String planId) {
        PlanSummary plan =
                planRepository.findById(planId).orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        if (plan.status() != PlanVersionStatus.PUBLISHED) {
            throw new IllegalStateException("Only published plans can be revised");
        }
        PlanVersion latest = planRepository
                .findVersion(planId, plan.currentVersion())
                .orElseThrow(() -> new IllegalArgumentException("Latest version not found"));
        int newVersion = plan.currentVersion() + 1;
        PlanVersion newVersionObj = PlanVersion.create(planId, newVersion, latest.workouts());
        planRepository.saveVersion(newVersionObj);
        PlanSummary updated =
                new PlanSummary(plan.id(), plan.athleteId(), newVersion, PlanVersionStatus.DRAFT, plan.createdAt());
        planRepository.save(updated);
        return updated;
    }

    public List<PlanSummary> listPlans() {
        return planRepository.findAll();
    }

    public PlanVersion getPlanVersion(String planId, int version) {
        return planRepository
                .findVersion(planId, version)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));
    }

    // Placeholder for dummy athlete; in real impl, fetch from AthleteRepository
    private Athlete createDummyAthlete(String athleteId) {
        // Dummy implementation; replace with actual fetch
        return new Athlete(athleteId, "Dummy Athlete", null, null, null);
    }

    public record CreatePlanCommand(String athleteId, String phase, LocalDate startDate, Hours targetWeeklyHours) {}
}
