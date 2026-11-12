package com.ticketmaster.exp.repository;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        return attribute.toNanos();
    }
 
    @Override
    public Duration convertToEntityAttribute(Long duration) {

        return Duration.ofNanos(duration);
    }
}