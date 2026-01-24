package com.training.coach.athlete.infrastructure.persistence;

import com.training.coach.athlete.application.port.out.AthleteRepository;
import com.training.coach.athlete.domain.model.Athlete;
import com.training.coach.athlete.domain.model.AthleteProfile;
import com.training.coach.athlete.domain.model.TrainingMetrics;
import com.training.coach.athlete.domain.model.TrainingPreferences;
import com.training.coach.athlete.infrastructure.persistence.entity.AthleteEntity;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for AthleteRepository.
 */
@Component
@Profile("!test")
public class AthleteRepositoryAdapter implements AthleteRepository {

    private final AthleteJpaRepository jpaRepository;

    public AthleteRepositoryAdapter(AthleteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Athlete save(Athlete athlete) {
        AthleteEntity entity = toEntity(athlete);
        AthleteEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Athlete> findById(String id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public java.util.List<Athlete> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteAllAthleteData(String athleteId) {
        // First delete the athlete
        deleteById(athleteId);
        // Additional data deletion would be handled by other repositories through the AthleteService
    }

    private AthleteEntity toEntity(Athlete athlete) {
        AthleteEntity entity = new AthleteEntity();
        entity.setId(athlete.id());
        entity.setName(athlete.name());
        entity.setGender(athlete.profile().gender());
        entity.setAge(athlete.profile().age());
        entity.setWeightKg(athlete.profile().weightKg());
        entity.setHeightCm(athlete.profile().heightCm());
        entity.setLevel(athlete.profile().level());
        entity.setFtp(athlete.currentMetrics().ftp());
        entity.setFthr(athlete.currentMetrics().fthr());
        entity.setVo2max(athlete.currentMetrics().vo2max());
        entity.setAvailableDays(athlete.preferences().availableDays().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(",")));
        entity.setTargetWeeklyVolumeHours(athlete.preferences().targetWeeklyVolumeHours());
        entity.setCurrentPhase(athlete.preferences().currentPhase());
        return entity;
    }

    private Athlete toDomain(AthleteEntity entity) {
        AthleteProfile profile = new AthleteProfile(
                entity.getGender(), entity.getAge(), entity.getWeightKg(), entity.getHeightCm(), entity.getLevel());
        TrainingMetrics metrics =
                new TrainingMetrics(entity.getFtp(), entity.getFthr(), entity.getVo2max(), entity.getWeightKg());
        Set<java.time.DayOfWeek> availableDays =
                entity.getAvailableDays() != null && !entity.getAvailableDays().isEmpty()
                        ? java.util.Arrays.stream(entity.getAvailableDays().split(","))
                                .map(java.time.DayOfWeek::valueOf)
                                .collect(Collectors.toCollection(java.util.HashSet::new))
                        : new java.util.HashSet<>();
        TrainingPreferences preferences =
                new TrainingPreferences(availableDays, entity.getTargetWeeklyVolumeHours(), entity.getCurrentPhase());
        return new Athlete(entity.getId(), entity.getName(), profile, metrics, preferences);
    }
}
