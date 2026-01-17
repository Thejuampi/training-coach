package com.training.coach.wellness.infrastructure.persistence;

import com.training.coach.wellness.infrastructure.persistence.entity.WellnessSnapshotEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WellnessJpaRepository extends JpaRepository<WellnessSnapshotEntity, String> {

    List<WellnessSnapshotEntity> findByAthleteIdOrderByDateDesc(String athleteId);

    List<WellnessSnapshotEntity> findByAthleteIdAndDateBetweenOrderByDateAsc(
            String athleteId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT w FROM WellnessSnapshotEntity w WHERE w.athleteId = :athleteId "
            + "AND w.date >= :startDate "
            + "AND w.date <= :endDate ORDER BY w.date ASC")
    List<WellnessSnapshotEntity> findByAthleteIdAndDateRange(
            @Param("athleteId") String athleteId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    WellnessSnapshotEntity findByAthleteIdAndDate(String athleteId, LocalDate date);

    void deleteByAthleteId(String athleteId);
}
