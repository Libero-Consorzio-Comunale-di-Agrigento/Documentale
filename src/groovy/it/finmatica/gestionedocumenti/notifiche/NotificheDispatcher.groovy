package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto

/**
 * Created by esasdelli on 07/04/2017.
 */
interface NotificheDispatcher {

    /**
     * Invia la notifica: il metodo deve essere implementato nella classe che effetivamente invia la notifica
     *
     * @param notifica
     * @param documento
     * @param destinatari
     * @param oggetto
     * @param testo
     * @param messaggioTODO : questo campo permette di inviare delle notifiche di tipo JWORKLIST-TODO (popup di presa visione)
     */
    void invia(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async)

    void aggiorna(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async)

    void elimina(String tipoNotifica, String idRiferimento, Ad4Utente utente)

    boolean isNotificaPresente(String tipoNotifica, Object documento, Ad4Utente utente)
}