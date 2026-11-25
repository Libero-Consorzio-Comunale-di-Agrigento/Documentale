package it.finmatica.gestionedocumenti.impostazioni

import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.commons.EnteDTO

class ImpostazioneDTO implements it.finmatica.dto.DTO<Impostazione> {

    private static final long serialVersionUID = 1L

    Long id
    Long version
    boolean valido
    String codice
    String descrizione
    String etichetta
    String valore
    String predefinito
    String caratteristiche
    EnteDTO ente

    Impostazione getDomainObject () {
        return Impostazione.get(this.id)
    }

    Impostazione copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
