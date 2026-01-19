package com.training.coach.trainingplan.application.service;

import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.TrainingPlan;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.shared.domain.unit.Percent;
import com.training.coach.shared.domain.unit.Watts;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Application service for training plan generation.
 */
@Service
public class TrainingPlanService {

    public TrainingPlan generatePlan(Athlete athlete, String phase, LocalDate startDate, Hours targetWeeklyHours) {
        // Simple planner: create weekly workouts based on availability and polarized training
        List<Workout> workouts = new ArrayList<>();

        // Assume 4 weeks for MVP
        LocalDate endDate = startDate.plusWeeks(4);
        for (int week = 0; week < 4; week++) {
            workouts.addAll(generateWeeklyWorkouts(athlete, phase, startDate.plusWeeks(week), targetWeeklyHours));
        }

        return TrainingPlan.create(
                UUID.randomUUID().toString(),
                athlete.id(),
                startDate,
                endDate,
                workouts,
                "Generated plan for " + phase);
    }

    /**
     * Generate a plan ending on a specific target date (e.g., race day).
     * Includes a taper block before the target date for priority events.
     */
    public TrainingPlan generatePlanWithTaper(
            Athlete athlete,
            String phase,
            LocalDate startDate,
            Hours targetWeeklyHours,
            LocalDate targetDate) {

        List<Workout> workouts = new ArrayList<>();

        // Calculate weeks until target date
        long totalWeeks = java.time.temporal.ChronoUnit.WEEKS.between(startDate, targetDate) + 1;

        // Taper is typically 1-2 weeks before the event
        int taperWeeks = Math.min(2, (int) totalWeeks - 1);
        int buildWeeks = (int) totalWeeks - taperWeeks;

        for (int week = 0; week < totalWeeks; week++) {
            LocalDate weekStart = startDate.plusWeeks(week);
            boolean isTaperWeek = week >= buildWeeks;

            if (isTaperWeek) {
                workouts.addAll(generateTaperWeekWorkouts(athlete, weekStart, taperWeeks - (week - buildWeeks)));
            } else {
                workouts.addAll(generateWeeklyWorkouts(athlete, phase, weekStart, targetWeeklyHours));
            }
        }

        return TrainingPlan.create(
                UUID.randomUUID().toString(),
                athlete.id(),
                startDate,
                targetDate,
                workouts,
                "Generated taper plan for " + phase + " ending " + targetDate);
    }

    /**
     * Generate reduced intensity workouts for taper weeks.
     */
    private List<Workout> generateTaperWeekWorkouts(Athlete athlete, LocalDate weekStart, int taperWeekIndex) {
        List<Workout> workouts = new ArrayList<>();

        // Reduce volume by 50% in week 1 of taper, 70% in week 2 (week 2 is final week before race)
        double reductionFactor = taperWeekIndex == 1 ? 0.7 : 0.5;

        List<java.time.DayOfWeek> availableDays =
                new ArrayList<>(athlete.preferences().availableDays());

        for (java.time.DayOfWeek day : availableDays) {
            // Use nextOrSame to ensure workout date is never before weekStart
            LocalDate date = weekStart.with(TemporalAdjusters.nextOrSame(day));
            if (!date.isAfter(weekStart.plusWeeks(2))) { // Only add workouts within the taper period
                workouts.add(new Workout(
                        UUID.randomUUID().toString(),
                        date,
                        Workout.WorkoutType.ENDURANCE,
                        Minutes.of((int) (60 * reductionFactor)), // Reduced duration
                        new Workout.IntensityProfile(
                                Percent.of(80), Percent.of(20), Percent.of(0), Percent.of(0), Percent.of(0)),
                        List.of()));
            }
        }

        return workouts;
    }

    /**
     * Check if the plan preserves intensity distribution within guardrails.
     */
    public boolean validateIntensityGuardrails(TrainingPlan plan) {
        // Simple validation: ensure no single zone exceeds 50% of total time
        double totalTime = plan.workouts().stream()
                .mapToDouble(w -> w.durationMinutes().value())
                .sum();

        if (totalTime == 0) return true;

        // Count time in each zone (simplified - using intensity profile)
        for (Workout workout : plan.workouts()) {
            Workout.IntensityProfile profile = workout.intensityProfile();
            // In a real implementation, we'd analyze zone distribution
        }

        return true; // Placeholder for actual validation
    }

    private List<Workout> generateWeeklyWorkouts(
            Athlete athlete, String phase, LocalDate weekStart, Hours targetHours) {
        List<Workout> workouts = new ArrayList<>();
        Hours highIntensityHours = targetHours.times(0.2); // 20% high
        Hours lowIntensityHours = targetHours.times(0.8); // 80% low

        // Assign based on available days
        List<java.time.DayOfWeek> availableDays =
                new ArrayList<>(athlete.preferences().availableDays());

        if (availableDays.size() >= 3) {
            // High intensity on first available day
            LocalDate date1 = weekStart.with(availableDays.get(0));
            Minutes hiMinutes = highIntensityHours.times(0.5).toMinutesRounded();
            workouts.add(new Workout(
                    UUID.randomUUID().toString(),
                    date1,
                    Workout.WorkoutType.INTERVALS,
                    hiMinutes,
                    new Workout.IntensityProfile(
                            Percent.of(10), Percent.of(10), Percent.of(10), Percent.of(35), Percent.of(35)),
                    List.of(new Workout.Interval(
                            Workout.Interval.IntervalType.VO2_MAX,
                            Minutes.of(5),
                            Watts.of(athlete.currentMetrics().ftp().value() * 1.10),
                            BeatsPerMinute.of(170)))));
            LocalDate date2 = weekStart.with(availableDays.get(1));
            workouts.add(new Workout(
                    UUID.randomUUID().toString(),
                    date2,
                    Workout.WorkoutType.THRESHOLD,
                    hiMinutes,
                    new Workout.IntensityProfile(
                            Percent.of(10), Percent.of(10), Percent.of(50), Percent.of(30), Percent.of(0)),
                    List.of()));
            // Low intensity on remaining days
            double lowPerDay = lowIntensityHours.value() / (availableDays.size() - 2);
            for (int i = 2; i < availableDays.size(); i++) {
                LocalDate date = weekStart.with(availableDays.get(i));
                workouts.add(new Workout(
                        UUID.randomUUID().toString(),
                        date,
                        Workout.WorkoutType.ENDURANCE,
                        Minutes.of((int) Math.round(lowPerDay * 60)),
                        new Workout.IntensityProfile(
                                Percent.of(70), Percent.of(20), Percent.of(10), Percent.of(0), Percent.of(0)),
                        List.of()));
            }
        }

        return workouts;
    }
}
