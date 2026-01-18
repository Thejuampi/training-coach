package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.Hours;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class HoursConverter implements AttributeConverter<Hours, Double> {

    @Override
    public Double convertToDatabaseColumn(Hours attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Hours convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : Hours.of(dbData);
    }
}
