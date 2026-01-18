package com.training.coach.shared.persistence.converter;

import com.training.coach.shared.domain.unit.Watts;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class WattsConverter implements AttributeConverter<Watts, Double> {

    @Override
    public Double convertToDatabaseColumn(Watts attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Watts convertToEntityAttribute(Double dbData) {
        return dbData == null ? null : Watts.of(dbData);
    }
}
