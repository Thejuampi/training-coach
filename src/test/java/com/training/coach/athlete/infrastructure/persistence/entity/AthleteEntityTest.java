package com.training.coach.athlete.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.training.coach.shared.domain.unit.Centimeters;
import com.training.coach.shared.domain.unit.Kilograms;
import com.training.coach.shared.domain.unit.Watts;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Athlete Entity Tests")
class AthleteEntityTest {

    @Test
    @DisplayName("Should create athlete entity with required fields")
    void shouldCreateAthleteEntity() {
        AthleteEntity entity = new AthleteEntity();

        assertThat(entity.getId()).isNull();

        entity.setName("John Doe");
        entity.setGender("male");
        entity.setAge(30);
        entity.setWeightKg(Kilograms.of(75.0));
        entity.setHeightCm(Centimeters.of(175.0));
        entity.setLevel("intermediate");

        assertThat(entity.getName()).isEqualTo("John Doe");
        assertThat(entity.getGender()).isEqualTo("male");
        assertThat(entity.getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should generate ID on pre-persist")
    void shouldGenerateIdOnPrePersist() {
        AthleteEntity entity = new AthleteEntity();
        entity.onCreate();

        assertThat(entity.getId()).isNotNull();
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update timestamp on pre-update")
    void shouldUpdateTimestampOnPreUpdate() {
        AthleteEntity entity = new AthleteEntity();
        entity.onCreate();
        Instant beforeUpdate = entity.getUpdatedAt();
        entity.onUpdate();

        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(entity.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }

    @Test
    @DisplayName("Should set and get available days as string")
    void shouldSetAndGetAvailableDays() {
        AthleteEntity entity = new AthleteEntity();
        String days = "MONDAY,WEDNESDAY,FRIDAY";

        entity.setAvailableDays(days);

        assertThat(entity.getAvailableDays()).isEqualTo("MONDAY,WEDNESDAY,FRIDAY");
    }

    @Test
    @DisplayName("Should set and get FTP")
    void shouldSetAndGetFtp() {
        AthleteEntity entity = new AthleteEntity();

        entity.setFtp(Watts.of(250.0));

        assertThat(entity.getFtp()).isEqualTo(Watts.of(250.0));
    }
}
