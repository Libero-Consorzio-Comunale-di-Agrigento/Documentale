package it.finmatica.gestionedocumenti.notifiche.dispatcher.email

import groovy.util.logging.Slf4j
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.notifiche.Allegato
import it.finmatica.gestionedocumenti.notifiche.Mail
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.NotificaEmail
import it.finmatica.gestionedocumenti.notifiche.NotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource

@Slf4j
class EmailNotificheDispatcher implements NotificheDispatcher {

    @Qualifier("ad4DataSource")
    @Autowired
    DataSource ad4DataSource

    @Autowired
    So4SpringSecurityService springSecurityService

    @Autowired
    RepositoryRegoleCalcolo repositoryRegoleCalcolo

    @Override
    @Transactional
    void invia(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {

        if (destinatari == null || destinatari.size() == 0) {
            return
        }

        // se mi vengono passati i destinatari, invio la notifica solo a loro:
        List<String> listaIndirizzi = (destinatari ?: destinatari(notifica, documento))?.findAll {
            it.email != null
        }?.email?.unique()
        if (listaIndirizzi?.size() > 0) {
            List<Allegato> allegatiEmail = calcolaAllegati(documento, notifica.listaAllegati)
            invioNotificaEmail(notifica, oggetto, testo, listaIndirizzi, allegatiEmail, documento)
        }
    }

    void invioNotificaEmail(Notifica notifica, String oggettoNotifica, String testoNotifica, List<String> destinatariEmail, List<Allegato> allegatiEmail = null, Object documento) {
        if (destinatariEmail.size() == 0) {
            // non ho email da inviare, non faccio neanche la fatica di provarci.
            log.info("la notifica di tipo EMAIL: " + notifica.titolo + " non ha destinatari con mail, quindi non viene inviata")
            return
        }

        Mail.invia(Impostazioni.TAG_INVIO_MAIL.valore, Impostazioni.MITTENTE_INVIO_MAIL.valore, destinatariEmail, testoNotifica, oggettoNotifica, allegatiEmail)

        NotificaEmail notificaEmail = new NotificaEmail(tipoNotifica: notifica.tipoNotifica, idRiferimento: repositoryRegoleCalcolo.getIdRiferimento(documento), testo: testoNotifica, oggetto: oggettoNotifica, destinatari: StringUtils.join(destinatariEmail, ","))
        notificaEmail.save()
    }

    @Override
    void elimina(String tipoNotifica, String idRiferimento, Ad4Utente utente) {
        return
    }

    @Override
    void aggiorna(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {
        return
    }

    @Override
    boolean isNotificaPresente(String tipoNotifica, Object documento, Ad4Utente utente) {
        return false
    }

    List<Allegato> calcolaAllegati(documento, List<String> allegati) {
        List<Allegato> allegatiMail = []

        if (allegati == null) {
            return allegatiMail
        }

        for (String allegato : allegati) {
            allegatiMail.addAll(repositoryRegoleCalcolo.calcolaAllegati(allegato, documento))
        }

        return allegatiMail
    }
}
