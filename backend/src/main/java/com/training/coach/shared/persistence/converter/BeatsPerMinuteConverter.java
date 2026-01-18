package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.BeatsPerMinute;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class BeatsPerMinuteConverter implements AttributeConverter<BeatsPerMinute, Double> {

    @Override
    public Double convertToDatabaseColumn(BeatsPerMinute attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public BeatsPerMinute convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : BeatsPerMinute.of(dbData);
    }
}
