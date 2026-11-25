package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.notifiche.Email

class EmailDTO implements it.finmatica.dto.DTO<Email> {
    private static final long serialVersionUID = 1L

    Long id
    Long version
    String cognome
    Date dateCreated
    String indirizzoEmail
    Date lastUpdated
    String nome
    String ragioneSociale
    Ad4UtenteDTO utenteIns
    Ad4UtenteDTO utenteUpd
    boolean valido


    Email getDomainObject () {
        return Email.get(this.id)
    }

    Email copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    String getDescrizione () {
        if (ragioneSociale != null) {
            return ragioneSociale
        } else {
            return "$cognome $nome"
        }
    }
}
