package it.finmatica.gestionedocumenti.documenti;

/*
DC = Da conservare
CC = CONSERVATO
FC = CONSERVAZIONE_FALLITA
IC = In conservazione (Iniziato il processo di conservazione)
IN = Inviato (per quei conservatori che hanno ricevuto il documento ma non danno una risposta sincrona es. Scryba)
SO = Sospeso (Per rimuovere temporaneamente un documento dal processo di conservazione )
CP = conservazione parziale (Infocert) documento conservato ma uno o più allegati ancora no
 */
public enum StatoConservazione {
    CC("CC", "Conservato"),
    FC("FC", "Conservazione fallita"),
    CP("CP", "Conservazione parziale"),
    IN("IN", "Inviato"),
    IJ("IJ", "Inviato a JCONS"),
    IC("IC", "In conservazione"),
    DC("DC", "Da Conservare"),
    SO("SO", "Sospeso");

    public static final StatoConservazione CONSERVATO = CC;
    public static final StatoConservazione CONSERVAZIONE_FALLITA = FC;
    public static final StatoConservazione CONSERVAZIONE_PARZIALE = CP;
    public static final StatoConservazione INVIATO = IN;
    public static final StatoConservazione INVIATO_A_JCONS = IJ;
    public static final StatoConservazione IN_CONSERVAZIONE = IC;
    public static final StatoConservazione SOSPESO = SO;
    public static final StatoConservazione PRESO_IN_CARICO = DC;

    private final String codice;
    private final String descrizione;

    StatoConservazione(String codice, String descrizione) {
        this.codice = codice;
        this.descrizione = descrizione;
    }

    @Override
    public String toString() {
        return descrizione;
    }

    public String getCodice() {
        return codice;
    }

    public String getDescrizione() {
        return descrizione;
    }
}

