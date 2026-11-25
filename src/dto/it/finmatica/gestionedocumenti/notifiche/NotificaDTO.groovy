package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.commons.EnteDTO
import it.finmatica.gestionedocumenti.documenti.DocumentoDTO
import it.finmatica.gestionedocumenti.notifiche.calcolo.TipoNotifica

class NotificaDTO implements it.finmatica.dto.DTO<Notifica> {
    private static final long serialVersionUID = 1L

    Long id
    Long version
    String allegati
    Date dateCreated
    List<NotificaDestinatarioDTO> destinatari
    DocumentoDTO documento
    EnteDTO ente
    Date lastUpdated
    String oggetti
    String oggetto
    String testo
    String tipoNotifica
    String titolo
    String modalitaInvio
    String tipoNotificaScrivania
    Ad4UtenteDTO utenteIns
    Ad4UtenteDTO utenteUpd
    boolean valido
    Date validoAl
    Date validoDal

    void addToDestinatari (NotificaDestinatarioDTO notificaDestinatario) {
        if (this.destinatari == null) {
            this.destinatari = new ArrayList<NotificaDestinatarioDTO>()
        }
        NotificaDestinatarioDTO notificaDestinatarioDTO = null
        int index = 0
        for(NotificaDestinatarioDTO n : this.destinatari) {
            if (notificaDestinatario.id != null && n.id == notificaDestinatario.id) {
                notificaDestinatarioDTO = notificaDestinatario
                break
            }
            index = index + 1
        }
        if (notificaDestinatarioDTO == null) {
            this.destinatari.add(notificaDestinatario)
        } else {
            this.destinatari.set(index, notificaDestinatario)
        }
        notificaDestinatario.notifica = this
    }

    void removeFromDestinatari (NotificaDestinatarioDTO notificaDestinatario) {
        if (this.destinatari == null)
            this.destinatari = new ArrayList<NotificaDestinatarioDTO>()
        this.destinatari.remove (notificaDestinatario)
        notificaDestinatario.notifica = null
    }

    Notifica getDomainObject () {
        return Notifica.get(this.id)
    }

    Notifica copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
	
	TipoNotifica getTipo () {
		return null
	}

    List<String> getListaAllegati () {
		return allegati?.split(",")?:[]
	}

    List<String> getListaOggetti () {
		return oggetti?.split(",")?:[]
	}

    String getListaOggettiDescrizioni () {
		return getListaOggetti().join(", ")
	}
}
