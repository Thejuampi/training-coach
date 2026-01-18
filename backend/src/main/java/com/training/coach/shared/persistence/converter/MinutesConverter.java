package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.Minutes;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MinutesConverter implements AttributeConverter<Minutes, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Minutes attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Minutes convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : Minutes.of(dbData);
    }
}
