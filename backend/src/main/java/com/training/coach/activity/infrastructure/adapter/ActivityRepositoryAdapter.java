package com.training.coach.activity.infrastructure.adapter;

import com.training.coach.activity.application.port.out.ActivityRepository;
import com.training.coach.activity.domain.model.ActivityLight;
import com.training.coach.activity.infrastructure.persistence.ActivityJpaRepository;
import com.training.coach.activity.infrastructure.persistence.entity.ActivityLightEntity;
import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Kilometers;
import com.training.coach.shared.domain.unit.Seconds;
import com.training.coach.shared.domain.unit.Watts;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class ActivityRepositoryAdapter implements ActivityRepository {

    private final ActivityJpaRepository jpaRepository;

    public ActivityRepositoryAdapter(ActivityJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ActivityLight save(ActivityLight activity) {
        ActivityLightEntity entity = toEntity(activity);
        ActivityLightEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ActivityLight> saveAll(List<ActivityLight> activities) {
        List<ActivityLightEntity> entities =
                activities.stream().map(this::toEntity).collect(Collectors.toList());
        List<ActivityLightEntity> saved = jpaRepository.saveAll(entities);
        return saved.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<ActivityLight> findByAthleteIdAndExternalActivityId(String athleteId, String externalActivityId) {
        return Optional.ofNullable(jpaRepository.findByAthleteIdAndExternalActivityId(athleteId, externalActivityId))
                .map(this::toDomain);
    }

    @Override
    public Optional<ActivityLight> findByAthleteIdAndDate(String athleteId, LocalDate date) {
        return Optional.ofNullable(jpaRepository.findByAthleteIdAndDate(athleteId, date))
                .map(this::toDomain);
    }

    @Override
    public List<ActivityLight> findByAthleteIdAndDateRange(String athleteId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByAthleteId(String athleteId) {
        jpaRepository.deleteByAthleteId(athleteId);
    }

    private ActivityLightEntity toEntity(ActivityLight activity) {
        ActivityLightEntity entity = new ActivityLightEntity();
        entity.setId(activity.id());
        entity.setAthleteId(activity.athleteId());
        entity.setExternalActivityId(activity.externalActivityId());
        entity.setDate(activity.date());
        entity.setName(activity.name());
        entity.setType(activity.type());
        entity.setDurationSeconds(
                activity.durationSeconds() != null ? activity.durationSeconds().value() : null);
        entity.setDistanceKm(
                activity.distanceKm() != null ? activity.distanceKm().value() : null);
        entity.setAveragePower(
                activity.averagePower() != null ? activity.averagePower().value() : null);
        entity.setAverageHeartRate(
                activity.averageHeartRate() != null
                        ? activity.averageHeartRate().value()
                        : null);
        entity.setTrainingStressScore(activity.trainingStressScore());
        entity.setIntensityFactor(activity.intensityFactor());
        entity.setNormalizedPower(
                activity.normalizedPower() != null ? activity.normalizedPower().value() : null);
        entity.setUpdatedAt(Instant.now());
        return entity;
    }

    private ActivityLight toDomain(ActivityLightEntity entity) {
        return new ActivityLight(
                entity.getId(),
                entity.getAthleteId(),
                entity.getExternalActivityId(),
                entity.getDate(),
                entity.getName(),
                entity.getType(),
                entity.getDurationSeconds() != null ? Seconds.of(entity.getDurationSeconds()) : null,
                entity.getDistanceKm() != null ? Kilometers.of(entity.getDistanceKm()) : null,
                entity.getAveragePower() != null ? Watts.of(entity.getAveragePower()) : null,
                entity.getAverageHeartRate() != null ? BeatsPerMinute.of(entity.getAverageHeartRate()) : null,
                entity.getTrainingStressScore(),
                entity.getIntensityFactor(),
                entity.getNormalizedPower() != null ? Watts.of(entity.getNormalizedPower()) : null);
    }
}
