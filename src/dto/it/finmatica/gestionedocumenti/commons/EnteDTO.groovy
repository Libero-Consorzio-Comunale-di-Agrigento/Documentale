package it.finmatica.gestionedocumenti.commons;

import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.commons.Ente;
import it.finmatica.so4.struttura.So4AmministrazioneDTO;
import it.finmatica.so4.struttura.So4OtticaDTO;

public class EnteDTO implements it.finmatica.dto.DTO<Ente> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    So4AmministrazioneDTO amministrazione;
    String aoo;
    String descrizione;
    So4OtticaDTO ottica;
    int sequenza;
    boolean valido;
    boolean interno;


    public Ente getDomainObject () {
        return Ente.get(this.id)
    }

    public Ente copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
