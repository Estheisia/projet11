package fr.uga.miashs.projetqcm.util;

import org.apache.commons.beanutils.converters.AbstractConverter;

import java.time.LocalDate;

/**
 * Permet d'effectuer la conversion chaine vars LocalDate avec la librairie commons-beanutils
 */
public class LocalDateConverter extends AbstractConverter {
    @Override
    protected <T> T convertToType(Class<T> aClass, Object value) throws Throwable {
        if (!LocalDate.class.equals(aClass)) {
            this.conversionException(aClass, value);
        }
        return (T) LocalDate.parse(value.toString());
    }

    @Override
    protected Class<?> getDefaultType() {
        return LocalDate.class;
    }
}
