package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gorm.criteria.PagedResultList
import org.hibernate.FetchMode
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class NotificaDTOService {

    @Autowired
    private SessionFactory sessionFactory

    NotificaDTO salva(NotificaDTO notificaDto) {
        Notifica notifica = Notifica.get(notificaDto.id) ?: new Notifica()
        if (notifica.version != notificaDto.version) {
            throw new GestioneDocumentiRuntimeException(GestioneDocumentiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
        }

        notifica.tipoNotifica = notificaDto.tipoNotifica
        notifica.oggetti = notificaDto.oggetti
        notifica.oggetto = notificaDto.oggetto
        notifica.tipoNotificaScrivania = notificaDto.tipoNotificaScrivania
        notifica.titolo = notificaDto.titolo
        notifica.testo = notificaDto.testo
        notifica.valido = notificaDto.valido
        notifica.allegati = notificaDto.allegati
        notifica.modalitaInvio = notificaDto.modalitaInvio

        notifica.save()

        return reloadFromDb(notifica.id).toDTO()
    }

    void elimina(NotificaDTO notificaDto) {
        Notifica.get(notificaDto.id).delete()
    }

    NotificaDTO salva(NotificaDestinatarioDTO notificaDestinatarioDto) {
        NotificaDestinatario notificaDestinatario = notificaDestinatarioDto.domainObject ?: new NotificaDestinatario()
        notificaDestinatario.email = notificaDestinatarioDto.email
        notificaDestinatario.ruolo = notificaDestinatarioDto.ruolo?.domainObject
        notificaDestinatario.soggetto = notificaDestinatarioDto.soggetto?.domainObject
        notificaDestinatario.unita = notificaDestinatarioDto.unita?.domainObject
        notificaDestinatario.funzione = notificaDestinatarioDto.funzione
        notificaDestinatario.assegnazione = notificaDestinatarioDto.assegnazione

        Notifica notifica = notificaDestinatarioDto.notifica.domainObject
        notifica.addToDestinatari(notificaDestinatario)
        notifica.save()

        return notifica.toDTO()
    }

    NotificaDTO elimina(NotificaDestinatarioDTO notificaDestinatarioDto) {
        Notifica notifica = notificaDestinatarioDto.notifica.domainObject
        NotificaDestinatario notificaDestinatario = notificaDestinatarioDto.domainObject
        notifica.removeFromDestinatari(notificaDestinatario)
        notificaDestinatario.delete()
        notifica.save()
        return notifica.toDTO()
    }

    NotificaDTO duplica(NotificaDTO notificaDto) {
        Notifica notificaOriginale = Notifica.get(notificaDto.id)

        Notifica notificaDuplicata = new Notifica()
        notificaDuplicata.tipoNotifica = notificaOriginale.tipoNotifica
        notificaDuplicata.oggetti = notificaOriginale.oggetti
        notificaDuplicata.oggetto = notificaOriginale.oggetto
        notificaDuplicata.titolo = notificaOriginale.titolo + " - Duplica"
        notificaDuplicata.testo = notificaOriginale.testo
        notificaDuplicata.valido = notificaOriginale.valido
        notificaDuplicata.allegati = notificaOriginale.allegati
        notificaDuplicata.modalitaInvio = notificaOriginale.modalitaInvio

        for (NotificaDestinatario notificaDestinatario : notificaOriginale.destinatari) {
            NotificaDestinatario destinatarioDuplicata = new NotificaDestinatario()

            destinatarioDuplicata.email = notificaDestinatario.email
            destinatarioDuplicata.ruolo = notificaDestinatario.ruolo
            destinatarioDuplicata.soggetto = notificaDestinatario.soggetto
            destinatarioDuplicata.unita = notificaDestinatario.unita
            destinatarioDuplicata.funzione = notificaDestinatario.funzione
            destinatarioDuplicata.assegnazione = notificaDestinatario.assegnazione

            notificaDuplicata.addToDestinatari(destinatarioDuplicata)
        }

        notificaDuplicata.save()

        return notificaDuplicata.toDTO()
    }

    void salvaIndirizzoEmail (EmailDTO emailDTO) {
        Email email = emailDTO.domainObject ?: new Email()

        email.nome = emailDTO.nome
        email.cognome = emailDTO.cognome
        email.ragioneSociale = emailDTO.ragioneSociale
        email.indirizzoEmail = emailDTO.indirizzoEmail

        email.save()
    }

    PagedResultList<NotificaDTO> list(int pageSize, int activePage, boolean visualizzaTutti, String filtro) {
        if(visualizzaTutti) {
            sessionFactory.getCurrentSession().disableFilter("soloValidiFilter")
        }
        try {
            PagedResultList lista = Notifica.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
                if (!visualizzaTutti)
                    eq("valido", true)

                if (filtro != null) {
                    ilike("oggetto", "%" + filtro + "%")
                }

                fetchMode("commissione", FetchMode.JOIN)
                order('tipoNotifica', 'asc')
            }
            return new PagedResultList<NotificaDTO>(lista?.toDTO(),lista.totalCount)
        } finally {
            if(visualizzaTutti) {
                sessionFactory.getCurrentSession().enableFilter("soloValidiFilter")
            }
        }
    }

    /**
     * carica record anche se non validi
     * @param id l'id da cercare
     * @return il record richiesto
     */
    NotificaDTO forceLoadFromDb(Long id) {
        Notifica notifica = null
        sessionFactory.getCurrentSession().disableFilter("soloValidiFilter")
        try {
            notifica = Notifica.createCriteria().get {
                eq("id", id)
                fetchMode("utenteIns", FetchMode.JOIN)
                fetchMode("utenteUpd", FetchMode.JOIN)
            }
        } finally {
            sessionFactory.getCurrentSession().enableFilter("soloValidiFilter")
        }
        return notifica?.toDTO()
    }

    private Notifica reloadFromDb(Long id) {
        Notifica notifica = null
        sessionFactory.getCurrentSession().disableFilter("soloValidiFilter")
        try {
            notifica = Notifica.get(id)
        } finally {
            sessionFactory.getCurrentSession().enableFilter("soloValidiFilter")
        }
        return notifica
    }
}
