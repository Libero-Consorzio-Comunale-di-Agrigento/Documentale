package it.finmatica.gestionedocumenti.integrazioni.gdm.converters;

import it.finmatica.gestionedocumenti.integrazioni.gdm.Converter;
import java.util.Date;

public class DateConverter extends Converter<Date, java.sql.Date> {

    public static final DateConverter INSTANCE = new DateConverter();

    @Override
    public java.sql.Date convert(Date date) {
        return (date == null) ? null : new java.sql.Date(date.getTime());
    }
}