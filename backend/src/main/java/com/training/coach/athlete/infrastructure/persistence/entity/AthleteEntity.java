package com.training.coach.athlete.infrastructure.persistence.entity;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import com.training.coach.shared.domain.unit.Centimeters;
import com.training.coach.shared.domain.unit.Hours;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.domain.unit.Vo2Max;
import com.training.coach.shared.domain.unit.Watts;
import com.training.coach.shared.persistence.converter.BeatsPerMinuteConverter;
import com.training.coach.shared.persistence.converter.CentimetersConverter;
import com.training.coach.shared.persistence.converter.HoursConverter;
import com.training.coach.shared.persistence.converter.KilogramsConverter;
import com.training.coach.shared.persistence.converter.Vo2MaxConverter;
import com.training.coach.shared.persistence.converter.WattsConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "athletes")
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class AthleteEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "age")
    private Integer age;

    @Convert(converter = KilogramsConverter.class)
    @Column(name = "weight_kg")
    private Kilograms weightKg;

    @Convert(converter = CentimetersConverter.class)
    @Column(name = "height_cm")
    private Centimeters heightCm;

    @Column(name = "level", length = 50)
    private String level;

    @Convert(converter = WattsConverter.class)
    @Column(name = "ftp")
    private Watts ftp;

    @Convert(converter = BeatsPerMinuteConverter.class)
    @Column(name = "fthr")
    private BeatsPerMinute fthr;

    @Convert(converter = Vo2MaxConverter.class)
    @Column(name = "vo2max")
    private Vo2Max vo2max;

    @Column(name = "available_days")
    private String availableDays;

    @Convert(converter = HoursConverter.class)
    @Column(name = "target_weekly_volume_hours")
    private Hours targetWeeklyVolumeHours;

    @Column(name = "current_phase", length = 50)
    private String currentPhase;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Kilograms getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Kilograms weightKg) {
        this.weightKg = weightKg;
    }

    public Centimeters getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(Centimeters heightCm) {
        this.heightCm = heightCm;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Watts getFtp() {
        return ftp;
    }

    public void setFtp(Watts ftp) {
        this.ftp = ftp;
    }

    public BeatsPerMinute getFthr() {
        return fthr;
    }

    public void setFthr(BeatsPerMinute fthr) {
        this.fthr = fthr;
    }

    public Vo2Max getVo2max() {
        return vo2max;
    }

    public void setVo2max(Vo2Max vo2max) {
        this.vo2max = vo2max;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public Hours getTargetWeeklyVolumeHours() {
        return targetWeeklyVolumeHours;
    }

    public void setTargetWeeklyVolumeHours(Hours targetWeeklyVolumeHours) {
        this.targetWeeklyVolumeHours = targetWeeklyVolumeHours;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
