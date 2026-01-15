package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.Vo2Max;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class Vo2MaxConverter implements AttributeConverter<Vo2Max, Double> {

    @Override
    public Double convertToDatabaseColumn(Vo2Max attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Vo2Max convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : Vo2Max.of(dbData);
    }
}
