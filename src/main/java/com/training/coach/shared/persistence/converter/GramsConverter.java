package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.Grams;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class GramsConverter implements AttributeConverter<Grams, Double> {

    @Override
    public Double convertToDatabaseColumn(Grams attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Grams convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : Grams.of(dbData);
    }
}
