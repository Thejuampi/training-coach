package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.Centimeters;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CentimetersConverter implements AttributeConverter<Centimeters, Double> {

    @Override
    public Double convertToDatabaseColumn(Centimeters attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Centimeters convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : Centimeters.of(dbData);
    }
}
