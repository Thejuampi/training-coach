package com.training.coach.trainingplan.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.coach.athlete.domain.model.Workout;
import com.training.coach.shared.domain.unit.Percent;
import com.training.coach.trainingplan.application.port.out.PlanRepository;
import com.training.coach.trainingplan.domain.model.PlanSummary;
import com.training.coach.trainingplan.domain.model.PlanVersion;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionEntity;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanVersionStatus;
import com.training.coach.trainingplan.infrastructure.persistence.entity.PlanWorkoutEntity;
import com.training.coach.trainingplan.infrastructure.persistence.entity.TrainingPlanEntity;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * Adapter for plan repository.
 */
@Repository
public class PlanRepositoryAdapter implements PlanRepository {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final TrainingPlanJpaRepository trainingPlanRepo;
    private final PlanVersionJpaRepository planVersionRepo;
    private final PlanWorkoutJpaRepository planWorkoutRepo;

    public PlanRepositoryAdapter(
            TrainingPlanJpaRepository trainingPlanRepo,
            PlanVersionJpaRepository planVersionRepo,
            PlanWorkoutJpaRepository planWorkoutRepo) {
        this.trainingPlanRepo = trainingPlanRepo;
        this.planVersionRepo = planVersionRepo;
        this.planWorkoutRepo = planWorkoutRepo;
    }

    @Override
    public PlanSummary save(PlanSummary plan) {
        TrainingPlanEntity entity = new TrainingPlanEntity();
        entity.setId(plan.id());
        entity.setAthleteId(plan.athleteId());
        entity.setTitle(defaultTitle(plan));
        entity.setCreatedAt(plan.createdAt());
        trainingPlanRepo.save(entity);
        return plan;
    }

    @Override
    public Optional<PlanSummary> findById(String id) {
        return trainingPlanRepo.findById(id).map(entity -> {
            // Find latest version for status
            Optional<PlanVersionEntity> latestVersion = planVersionRepo.findFirstByPlanIdOrderByVersionDesc(id);
            PlanVersionStatus status =
                    latestVersion.map(PlanVersionEntity::getStatus).orElse(PlanVersionStatus.DRAFT);
            int currentVersion = latestVersion.map(PlanVersionEntity::getVersion).orElse(1);
            return new PlanSummary(
                    entity.getId(), entity.getAthleteId(), currentVersion, status, entity.getCreatedAt());
        });
    }

    @Override
    public List<PlanSummary> findAll() {
        return trainingPlanRepo.findAll().stream()
                .map(entity -> {
                    Optional<PlanVersionEntity> latestVersion =
                            planVersionRepo.findFirstByPlanIdOrderByVersionDesc(entity.getId());
                    PlanVersionStatus status =
                            latestVersion.map(PlanVersionEntity::getStatus).orElse(PlanVersionStatus.DRAFT);
                    int currentVersion = latestVersion.map(PlanVersionEntity::getVersion).orElse(1);
                    return new PlanSummary(
                            entity.getId(), entity.getAthleteId(), currentVersion, status, entity.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    @Override
    public PlanVersion saveVersion(PlanVersion version) {
        PlanVersionEntity entity = new PlanVersionEntity();
        entity.setId(version.planId() + "-" + version.versionNumber());
        entity.setPlanId(version.planId());
        entity.setVersion(version.versionNumber());
        entity.setStatus(version.status());
        entity.setCreatedAt(version.createdAt());
        planVersionRepo.save(entity);

        // Save workouts
        for (Workout workout : version.workouts()) {
            PlanWorkoutEntity workoutEntity = new PlanWorkoutEntity();
            workoutEntity.setId(workout.id());
            workoutEntity.setPlanVersionId(entity.getId());
            workoutEntity.setDate(workout.date());
            workoutEntity.setType(workout.type().name());
            workoutEntity.setDurationMinutes(workout.durationMinutes());
            workoutEntity.setIntensityProfileJson(writeJson(workout.intensityProfile()));
            workoutEntity.setIntervalsJson(writeJson(workout.intervals()));
            planWorkoutRepo.save(workoutEntity);
        }

        return version;
    }

    @Override
    public Optional<PlanVersion> findVersion(String planId, int version) {
        return planVersionRepo.findByPlanIdAndVersion(planId, version).map(entity -> {
            List<Workout> workouts = planWorkoutRepo.findByPlanVersionId(entity.getId()).stream()
                    .map(this::mapToWorkout)
                    .collect(Collectors.toList());
            return new PlanVersion(
                    entity.getPlanId(), entity.getVersion(), entity.getStatus(), workouts, entity.getCreatedAt());
        });
    }

    @Override
    public List<PlanVersion> findVersions(String planId) {
        return planVersionRepo.findByPlanIdOrderByVersionDesc(planId).stream()
                .map(entity -> {
                    List<Workout> workouts = planWorkoutRepo.findByPlanVersionId(entity.getId()).stream()
                            .map(this::mapToWorkout)
                            .collect(Collectors.toList());
                    return new PlanVersion(
                            entity.getPlanId(),
                            entity.getVersion(),
                            entity.getStatus(),
                            workouts,
                            entity.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateVersionStatus(String planId, int version, PlanVersionStatus status) {
        planVersionRepo.findByPlanIdAndVersion(planId, version).ifPresent(entity -> {
            entity.setStatus(status);
            planVersionRepo.save(entity);
        });
    }

    private Workout mapToWorkout(PlanWorkoutEntity entity) {
        Workout.IntensityProfile intensityProfile = readIntensityProfile(entity.getIntensityProfileJson());
        List<Workout.Interval> intervals = readIntervals(entity.getIntervalsJson());
        return new Workout(
                entity.getId(),
                entity.getDate(),
                Workout.WorkoutType.valueOf(entity.getType()),
                entity.getDurationMinutes(),
                intensityProfile,
                intervals);
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize workout data", ex);
        }
    }

    private Workout.IntensityProfile readIntensityProfile(String json) {
        if (json == null || json.isBlank()) {
            return defaultIntensityProfile();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Workout.IntensityProfile.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize intensity profile", ex);
        }
    }

    private List<Workout.Interval> readIntervals(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize intervals", ex);
        }
    }

    private Workout.IntensityProfile defaultIntensityProfile() {
        return new Workout.IntensityProfile(
                Percent.of(0), Percent.of(0), Percent.of(0), Percent.of(0), Percent.of(0));
    }
}
