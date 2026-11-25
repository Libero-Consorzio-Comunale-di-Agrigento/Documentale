package it.finmatica.gestionedocumenti.registri

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.commons.EnteDTO
import it.finmatica.gestionedocumenti.registri.TipoRegistro
import java.util.Date

class TipoRegistroDTO implements DTO<TipoRegistro> {
    private static final long serialVersionUID = 1L

    Long version
    boolean chiusuraAutomatica
    String codice
    String commento
    Date dateCreated
    EnteDTO ente
    Date lastUpdated
    String registroEsterno
    boolean rinnovoAutomatico
    Ad4UtenteDTO utenteIns
    Ad4UtenteDTO utenteUpd
    boolean valido
    boolean emergenza
    boolean numerazioneIllimitata
    boolean registroAutomatico
    Long idSocieta
    boolean protocolloGenerale


    TipoRegistro getDomainObject () {
        return TipoRegistro.get(this.codice)
    }

    TipoRegistro copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
