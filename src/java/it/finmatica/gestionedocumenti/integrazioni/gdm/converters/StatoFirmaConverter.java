package it.finmatica.gestionedocumenti.integrazioni.gdm.converters;

import it.finmatica.gestionedocumenti.documenti.StatoFirma;
import it.finmatica.gestionedocumenti.integrazioni.gdm.Converter;

public class StatoFirmaConverter extends Converter<Object, String> {

    public static final StatoFirmaConverter INSTANCE = new StatoFirmaConverter();

    /**
     * Stato della firma vale 'DF' - 'F' - 'NF' (Da firmare, Firmato, Da non firmare) (true = Bozza, false =
     * Cancellato)
     */
    @Override
    public String convert(Object input) {

        if (input == null) {
            return "";
        }

        String statoFirma = "";
        StatoFirma stato = (StatoFirma) input;

        switch (stato) {
            case DA_FIRMARE:
                statoFirma = "DF";
                break;
            case IN_FIRMA:
                statoFirma = "DF";
                break;
            case DA_NON_FIRMARE:
                statoFirma = "NF";
                break;
            case FIRMATO:
                statoFirma = "F";
                break;
            case FIRMATO_DA_SBLOCCARE:
                statoFirma = "F";
                break;
        }

        return statoFirma;
    }
}