package it.finmatica.gestionedocumenti.commons;

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.commons.TokenIntegrazione;
import java.util.Date;

public class TokenIntegrazioneDTO implements it.finmatica.dto.DTO<TokenIntegrazione> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String idRiferimento;

    String dati;

    Date dateCreated;
    Date lastUpdated;

    String stato;
    String tipo;

    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;

    boolean valido;


    public TokenIntegrazione getDomainObject () {
        return TokenIntegrazione.get(this.id)
    }

    public TokenIntegrazione copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
