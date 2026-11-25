package it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist

import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto

/**
 * Created by esasdelli on 07/04/2017.
 */
interface NotificaJWorklistBuilder {

    NotificaJWorklist creaNotificaJWorklist (Notifica notifica, Object documento, NotificaSoggetto soggetto, String testo, String oggetto, String messaggioTODO)

    List<NotificaJWorklist> creaNotificaJWorklist (Notifica notifica, Object documento, List<NotificaSoggetto> soggetto, String testo, String oggetto, String messaggioTODO)
}