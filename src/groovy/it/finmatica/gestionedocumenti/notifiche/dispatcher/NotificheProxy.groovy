package it.finmatica.gestionedocumenti.notifiche.dispatcher

import groovy.util.logging.Slf4j
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.NotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.dispatcher.email.EmailNotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist.JWorklistNotificheDispatcher
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by rdestasio on 17/08/2017.
 */
@Slf4j
class NotificheProxy implements NotificheDispatcher {

    private final EmailNotificheDispatcher     emailNotificheDispatcher
    private final JWorklistNotificheDispatcher jWorklistNotificheDispatcher

    NotificheProxy(EmailNotificheDispatcher emailNotificheDispatcher, JWorklistNotificheDispatcher jWorklistNotificheDispatcher) {
        this.emailNotificheDispatcher = emailNotificheDispatcher
        this.jWorklistNotificheDispatcher = jWorklistNotificheDispatcher
    }

    @Override
    void invia(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {

        if(notifica.modalitaInvio == Notifica.MODALITA_JWORKLIST){
            log.info(getLogNotifica(documento,"invia"))
            jWorklistNotificheDispatcher.invia(notifica, documento, destinatari, oggetto, testo, messaggioTODO, async)
        }
        else if(notifica.modalitaInvio == Notifica.MODALITA_EMAIL){
            emailNotificheDispatcher.invia(notifica, documento, destinatari, oggetto, testo, null, async)
        }
        else{
            log.error("Non esiste il dispatcher per questa modalità di invio della notifica: " + notifica.modalitaInvio)
        }
    }

    @Override
    void aggiorna(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {
        log.info(getLogNotifica(documento,"aggiornare"))
        jWorklistNotificheDispatcher.aggiorna(notifica, documento, destinatari, oggetto, testo, messaggioTODO, async)
    }

    @Override
    void elimina(String tipoNotifica, String idRiferimento, Ad4Utente utente) {

        String modalitaInvio = Notifica.findByTipoNotificaAndValido(tipoNotifica, true)?.modalitaInvio

        if(modalitaInvio == Notifica.MODALITA_JWORKLIST || tipoNotifica == null){
            jWorklistNotificheDispatcher.elimina(tipoNotifica, idRiferimento, utente)
        }
        else if(modalitaInvio == Notifica.MODALITA_EMAIL){
            emailNotificheDispatcher.elimina(tipoNotifica, idRiferimento, utente)
        }
        else{
            log.error("Non esiste il dispatcher per questa modalità di invio della notifica: " + modalitaInvio)
        }
    }

    @Override
    boolean isNotificaPresente(String tipoNotifica, Object documento, Ad4Utente utente) {

        String modalitaInvio = Notifica.findByTipoNotificaAndValido(tipoNotifica, true)?.modalitaInvio

        if(modalitaInvio == Notifica.MODALITA_JWORKLIST){
            jWorklistNotificheDispatcher.isNotificaPresente(tipoNotifica, documento, utente)
        }
        else if(modalitaInvio == Notifica.MODALITA_EMAIL){
            emailNotificheDispatcher.isNotificaPresente(tipoNotifica, documento, utente)
        }
        else{
            log.error("Non esiste il dispatcher per questa modalità di invio della notifica: " + modalitaInvio)
        }
    }

    // #80630
    private String getLogNotifica(Object documento, String action) {
        if (documento.hasProperty("id")) {
            return "NotificheProxy: Classe del documento ${documento.id} per cui ${action} la notifica: ${documento.class.canonicalName}"
        } else {
            return "NotificheProxy: Classe del documento per cui ${action} la notifica: ${documento.class.canonicalName}"
        }
    }
}
