package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.Kilograms;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class KilogramsConverter implements AttributeConverter<Kilograms, Double> {

    @Override
    public Double convertToDatabaseColumn(Kilograms attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Kilograms convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : Kilograms.of(dbData);
    }
}
