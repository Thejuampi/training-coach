package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.HeartRateVariability;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class HeartRateVariabilityConverter implements AttributeConverter<HeartRateVariability, Double> {

    @Override
    public Double convertToDatabaseColumn(HeartRateVariability attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public HeartRateVariability convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : HeartRateVariability.of(dbData);
    }
}
