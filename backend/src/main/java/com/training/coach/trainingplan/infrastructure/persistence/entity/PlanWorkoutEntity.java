package com.training.coach.trainingplan.infrastructure.persistence.entity;

import com.training.coach.shared.domain.unit.Minutes;
import com.training.coach.shared.persistence.converter.MinutesConverter;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "plan_workouts")
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class PlanWorkoutEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "plan_version_id", nullable = false, length = 255)
    private String planVersionId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "type", length = 100)
    private String type;

    @Convert(converter = MinutesConverter.class)
    @Column(name = "duration_minutes")
    private Minutes durationMinutes;

    @Column(name = "intensity_target", length = 50)
    private String intensityTarget;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "intensity_profile_json", columnDefinition = "TEXT")
    private String intensityProfileJson;

    @Column(name = "intervals_json", columnDefinition = "TEXT")
    private String intervalsJson;

    public PlanWorkoutEntity() {}

    public PlanWorkoutEntity(
            String id,
            String planVersionId,
            LocalDate date,
            String type,
            Minutes durationMinutes,
            String intensityTarget,
            String notes) {
        this.id = id;
        this.planVersionId = planVersionId;
        this.date = date;
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.intensityTarget = intensityTarget;
        this.notes = notes;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlanVersionId() {
        return planVersionId;
    }

    public void setPlanVersionId(String planVersionId) {
        this.planVersionId = planVersionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Minutes getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Minutes durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getIntensityTarget() {
        return intensityTarget;
    }

    public void setIntensityTarget(String intensityTarget) {
        this.intensityTarget = intensityTarget;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getIntensityProfileJson() {
        return intensityProfileJson;
    }

    public void setIntensityProfileJson(String intensityProfileJson) {
        this.intensityProfileJson = intensityProfileJson;
    }

    public String getIntervalsJson() {
        return intervalsJson;
    }

    public void setIntervalsJson(String intervalsJson) {
        this.intervalsJson = intervalsJson;
    }
}
