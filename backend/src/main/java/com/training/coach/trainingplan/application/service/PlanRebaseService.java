package com.training.coach.trainingplan.application.service;

import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.trainingplan.domain.model.PlanRebase;
import com.training.coach.trainingplan.domain.model.PlanVersion;
import com.training.coach.trainingplan.domain.model.PlanRebase.WorkoutAdjustment.AdjustmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for rebasing training plans when event dates change.
 * Preserves workout history while shifting future workouts.
 */
@Service
public class PlanRebaseService {

    private static final Logger logger = LoggerFactory.getLogger(PlanRebaseService.class);

    private final PlanService planService;
    private final PlanRebaseRepository rebaseRepository;

    public PlanRebaseService(PlanService planService, PlanRebaseRepository rebaseRepository) {
        this.planService = planService;
        this.rebaseRepository = rebaseRepository;
    }

    /**
     * Rebase a plan to a new end date.
     * Shifts all workouts by the difference between old and new dates.
     */
    public PlanRebase rebasePlanToDate(
            String planId,
            int planVersion,
            LocalDate newEndDate,
            String reason,
            String performedBy
    ) {
        // Get current plan version
        PlanVersion currentVersion = planService.getPlanVersion(planId, planVersion);

        // Calculate shift amount
        LocalDate originalEndDate = getPlanEndDate(currentVersion);
        int daysShift = (int) ChronoUnit.DAYS.between(originalEndDate, newEndDate);

        // Create workout adjustments
        List<PlanRebase.WorkoutAdjustment> adjustments = createWorkoutAdjustments(
            currentVersion.workouts(),
            daysShift,
            LocalDate.now()
        );

        // Create rebase record
        PlanRebase rebase = PlanRebase.create(
            planId,
            planVersion,
            originalEndDate,
            newEndDate,
            reason,
            performedBy
        );

        // Save rebase history
        rebaseRepository.save(rebase);

        // Create new plan version with rebased workouts
        List<Workout> rebasedWorkouts = rebaseWorkouts(currentVersion.workouts(), daysShift, LocalDate.now());
        int newVersion = planVersion + 1;
        PlanVersion newVersionObj = PlanVersion.create(planId, newVersion, rebasedWorkouts);
        // In a real implementation, this would save the new version through PlanService

        logger.info("Rebased plan {} from {} to {} ({} day shift)",
            planId, originalEndDate, newEndDate, daysShift);

        return new PlanRebase(
            rebase.id(),
            rebase.planId(),
            rebase.planVersion(),
            rebase.originalEndDate(),
            rebase.newEndDate(),
            rebase.reason(),
            rebase.rebasedAt(),
            rebase.performedBy(),
            adjustments
        );
    }

    /**
     * Get rebase history for a plan.
     */
    public List<PlanRebase> getRebaseHistory(String planId) {
        return rebaseRepository.findByPlanId(planId);
    }

    /**
     * Create workout adjustments based on date shift and current date.
     * Preserves completed workouts, shifts future workouts.
     */
    private List<PlanRebase.WorkoutAdjustment> createWorkoutAdjustments(
        List<Workout> workouts,
        int daysShift,
        LocalDate currentDate
    ) {
        List<PlanRebase.WorkoutAdjustment> adjustments = new ArrayList<>();

        for (Workout workout : workouts) {
            if (workout.date().isBefore(currentDate)) {
                // Completed workout - preserve
                adjustments.add(new PlanRebase.WorkoutAdjustment(
                    workout.id(),
                    workout.date(),
                    workout.date(),
                    AdjustmentType.PRESERVED.name()
                ));
            } else {
                // Future workout - shift
                LocalDate newDate = workout.date().plusDays(daysShift);
                adjustments.add(new PlanRebase.WorkoutAdjustment(
                    workout.id(),
                    workout.date(),
                    newDate,
                    AdjustmentType.SHIFTED.name()
                ));
            }
        }

        return adjustments;
    }

    /**
     * Rebase workouts by shifting dates.
     * Completed workouts are preserved at their original dates.
     */
    private List<Workout> rebaseWorkouts(
        List<Workout> workouts,
        int daysShift,
        LocalDate currentDate
    ) {
        return workouts.stream()
            .map(workout -> {
                if (workout.date().isBefore(currentDate)) {
                    // Preserve completed workouts
                    return workout;
                } else {
                    // Shift future workouts
                    return new Workout(
                        workout.id(),
                        workout.date().plusDays(daysShift),
                        workout.type(),
                        workout.durationMinutes(),
                        workout.intensityProfile(),
                        workout.intervals()
                    );
                }
            })
            .toList();
    }

    /**
     * Extract the end date from a plan version.
     */
    private LocalDate getPlanEndDate(PlanVersion version) {
        return version.workouts().stream()
            .map(Workout::date)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now());
    }
}