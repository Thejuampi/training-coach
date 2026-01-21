package com.training.coach.wellness.infrastructure.adapter;

import com.training.coach.wellness.application.port.out.WellnessRepository;
import com.training.coach.wellness.domain.model.*;
import com.training.coach.wellness.infrastructure.persistence.WellnessJpaRepository;
import com.training.coach.wellness.infrastructure.persistence.entity.WellnessSnapshotEntity;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class WellnessRepositoryAdapter implements WellnessRepository {

    private final WellnessJpaRepository jpaRepository;

    public WellnessRepositoryAdapter(WellnessJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<WellnessSnapshot> findByAthleteIdAndDate(String athleteId, LocalDate date) {
        WellnessSnapshotEntity entity = jpaRepository.findByAthleteIdAndDate(athleteId, date);
        return Optional.ofNullable(entity).map(this::toDomain);
    }

    @Override
    public List<WellnessSnapshot> findByAthleteIdAndDateRange(
            String athleteId, LocalDate startDate, LocalDate endDate) {
        return jpaRepository.findByAthleteIdAndDateRange(athleteId, startDate, endDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WellnessSnapshot> findLatestByAthleteId(String athleteId) {
        List<WellnessSnapshotEntity> entities = jpaRepository.findByAthleteIdOrderByDateDesc(athleteId);
        return entities.isEmpty() ? Optional.empty() : Optional.of(toDomain(entities.get(0)));
    }

    @Override
    public WellnessSnapshot save(WellnessSnapshot snapshot) {
        WellnessSnapshotEntity entity = toEntity(snapshot);
        WellnessSnapshotEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<WellnessSnapshot> saveAll(List<WellnessSnapshot> snapshots) {
        List<WellnessSnapshotEntity> entities =
                snapshots.stream().map(this::toEntity).collect(Collectors.toList());
        List<WellnessSnapshotEntity> saved = jpaRepository.saveAll(entities);
        return saved.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteByAthleteIdAndDate(String athleteId, LocalDate date) {
        WellnessSnapshotEntity entity = jpaRepository.findByAthleteIdAndDate(athleteId, date);
        if (entity != null) {
            jpaRepository.delete(entity);
        }
    }

    @Override
    public void deleteByAthleteId(String athleteId) {
        List<WellnessSnapshotEntity> entities = jpaRepository.findByAthleteIdOrderByDateDesc(athleteId);
        jpaRepository.deleteAll(entities);
    }

    @Override
    public boolean existsByAthleteIdAndDate(String athleteId, LocalDate date) {
        return jpaRepository.findByAthleteIdAndDate(athleteId, date) != null;
    }

    private WellnessSnapshotEntity toEntity(WellnessSnapshot snapshot) {
        WellnessSnapshotEntity entity = new WellnessSnapshotEntity();
        entity.setId(snapshot.id());
        entity.setAthleteId(snapshot.athleteId());
        entity.setDate(snapshot.date());
        entity.setUpdatedAt(Instant.now());

        if (snapshot.physiological() != null) {
            if (snapshot.physiological().restingHeartRate() != null) {
                entity.setRestingHeartRate(snapshot.physiological().restingHeartRate());
            }
            if (snapshot.physiological().hrv() != null) {
                entity.setHrv(snapshot.physiological().hrv());
            }
            if (snapshot.physiological().bodyWeightKg() != null) {
                entity.setBodyWeightGrams(
                        snapshot.physiological().bodyWeightKg().toGrams());
            }
            if (snapshot.physiological().sleep() != null) {
                entity.setSleepHours(snapshot.physiological().sleep().totalSleepHours());
                entity.setSleepQuality(snapshot.physiological().sleep().qualityScore());
            }
        }

        if (snapshot.subjective() != null) {
            entity.setFatigue(snapshot.subjective().fatigueScore());
            entity.setStress(snapshot.subjective().stressScore());
            entity.setMotivation(snapshot.subjective().motivationScore());
            entity.setSoreness(snapshot.subjective().muscleSorenessScore());
        }

        entity.setReadinessScore(snapshot.readinessScore());

        if (snapshot.loadSummary() != null) {
            entity.setTss(snapshot.loadSummary().tss());
            entity.setCtl(snapshot.loadSummary().ctl());
            entity.setAtl(snapshot.loadSummary().atl());
            entity.setTsb(snapshot.loadSummary().tsb());
            entity.setTrainingMinutes(snapshot.loadSummary().trainingMinutes());
        }

        return entity;
    }

    private WellnessSnapshot toDomain(WellnessSnapshotEntity entity) {
        PhysiologicalData physiological = null;
        if (entity.getRestingHeartRate() != null
                || entity.getHrv() != null
                || entity.getBodyWeightGrams() != null
                || entity.getSleepHours() != null) {
            SleepMetrics sleep = null;
            if (entity.getSleepHours() != null) {
                int quality = entity.getSleepQuality();
                sleep = SleepMetrics.basic(entity.getSleepHours(), quality);
            }
            physiological = new PhysiologicalData(
                    entity.getRestingHeartRate(),
                    entity.getHrv(),
                    entity.getBodyWeightGrams() != null
                            ? entity.getBodyWeightGrams().toKilograms()
                            : null,
                    sleep);
        }

        SubjectiveWellness subjective = null;
        Integer fatigue = entity.getFatigue();
        Integer stress = entity.getStress();
        Integer motivation = entity.getMotivation();
        Integer soreness = entity.getSoreness();
        Integer sleepQuality = entity.getSleepQuality();
        if ((fatigue != null && fatigue > 0)
                || (stress != null && stress > 0)
                || (motivation != null && motivation > 0)
                || (soreness != null && soreness > 0)) {
            subjective = SubjectiveWellness.create(
                    fatigue != null ? fatigue : 0,
                    stress != null ? stress : 0,
                    sleepQuality != null ? sleepQuality : 0,
                    motivation != null ? motivation : 0,
                    soreness != null ? soreness : 0);
        }

        TrainingLoadSummary loadSummary = null;
        if (entity.getTss() != null || entity.getCtl() != null || entity.getAtl() != null || entity.getTsb() != null) {
            loadSummary = new TrainingLoadSummary(
                    entity.getTss() != null ? entity.getTss() : 0.0,
                    entity.getCtl() != null ? entity.getCtl() : 0.0,
                    entity.getAtl() != null ? entity.getAtl() : 0.0,
                    entity.getTsb() != null ? entity.getTsb() : 0.0,
                    entity.getTrainingMinutes() != null ? entity.getTrainingMinutes() : 0);
        }

        return new WellnessSnapshot(
                entity.getId(),
                entity.getAthleteId(),
                entity.getDate(),
                physiological,
                subjective,
                loadSummary,
                entity.getReadinessScore() != null ? entity.getReadinessScore() : 0.0);
    }
}
