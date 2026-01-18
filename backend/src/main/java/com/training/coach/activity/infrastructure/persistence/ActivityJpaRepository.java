package com.training.coach.activity.infrastructure.persistence;

import com.training.coach.activity.infrastructure.persistence.entity.ActivityLightEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityJpaRepository extends JpaRepository<ActivityLightEntity, String> {

    ActivityLightEntity findByAthleteIdAndExternalActivityId(String athleteId, String externalActivityId);

    ActivityLightEntity findByAthleteIdAndDate(String athleteId, LocalDate date);

    @Query("SELECT a FROM ActivityLightEntity a WHERE a.athleteId = :athleteId "
            + "AND a.date >= :startDate "
            + "AND a.date <= :endDate ORDER BY a.date ASC")
    List<ActivityLightEntity> findByAthleteIdAndDateRange(
            @Param("athleteId") String athleteId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
