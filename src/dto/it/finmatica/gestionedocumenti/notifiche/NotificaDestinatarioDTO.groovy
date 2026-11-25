package it.finmatica.gestionedocumenti.notifiche;

import it.finmatica.ad4.autenticazione.Ad4RuoloDTO;
import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.as4.As4SoggettoCorrenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.commons.EnteDTO;
import it.finmatica.gestionedocumenti.notifiche.NotificaDestinatario
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO;
import java.util.Date;

public class NotificaDestinatarioDTO implements it.finmatica.dto.DTO<NotificaDestinatario> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    String email;
    EnteDTO ente;
    String funzione;
    Date lastUpdated;
    NotificaDTO notifica;
    Ad4RuoloDTO ruolo;
    As4SoggettoCorrenteDTO soggetto;
    So4UnitaPubbDTO unita;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    NotificaSoggetto.Assegnazione assegnazione;

    public NotificaDestinatario getDomainObject() {
        return NotificaDestinatario.get(this.id)
    }

    public NotificaDestinatario copyToDomainObject() {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    void setAssegnazioneString(String assegnazione) {
        this.assegnazione = NotificaSoggetto.Assegnazione.valueOf(assegnazione)
    }

    String getAssegnazioneString() {
        this.assegnazione?.toString()
    }
}
