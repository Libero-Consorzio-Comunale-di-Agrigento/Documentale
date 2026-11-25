package it.finmatica.gestionedocumenti.documenti

import it.finmatica.dto.DtoUtils

public class AllegatoDTO extends DocumentoDTO implements it.finmatica.dto.DTO<Allegato> {
    private static final long serialVersionUID = 1L;

    TipoAllegatoDTO tipoAllegato;

    String descrizione;
    String commento;

    String origine;
    String ubicazione;

    Integer quantita;
    Integer numPagine;
    int sequenza;

    boolean stampaUnica;


    public Allegato getDomainObject () {
        return Allegato.get(this.id)
    }

    public Allegato copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
