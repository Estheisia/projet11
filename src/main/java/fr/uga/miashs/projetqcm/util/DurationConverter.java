package fr.uga.miashs.projetqcm.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Time;
import java.time.Duration;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Time> {


    @Override
    public Time convertToDatabaseColumn(Duration attribute) {
        return new Time(attribute.toMillis());
    }

    @Override
    public Duration convertToEntityAttribute(Time duration) {
        return Duration.ofMillis(duration.getTime());
    }
}
