package it.finmatica.gestionedocumenti.integrazioni.gdm.converters;

import it.finmatica.gestionedocumenti.integrazioni.gdm.Converter;

public class StatoDocumentoConverter extends Converter<Object, String> {

    public static final StatoDocumentoConverter INSTANCE = new StatoDocumentoConverter();

    /**
     * Stato del documento vale 'CA' - 'BO' (Cancellato - Bozza) su Gdm e viene tradotto con 'valido' (true = Bozza,
     * false = Cancellato)
     */
    @Override
    public String convert(Object input) {
        Boolean in = (Boolean) input;
        return in.booleanValue() ? "BO" : "CA";
    }
}