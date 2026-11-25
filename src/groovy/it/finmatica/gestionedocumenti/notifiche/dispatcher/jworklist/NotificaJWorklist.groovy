package it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist

import it.finmatica.atti.integrazioniws.ads.jworkflow.MetadatoUtente
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto

import javax.xml.datatype.XMLGregorianCalendar

/**
 * Created by esasdelli on 07/04/2017.
 */
class NotificaJWorklist {
    String idRiferimento
    String testo
    String tooltip
    String urlRiferimento
    String tooltipUrlRiferimento
    String urlEsecuzione
    String tooltipUrlEsecuzione
    Date dataScadenza
    String nomeIter
    String descrizioneIter
    String colore
    String ordinamento
    MetadatoUtente utenteEsterno
    List<MetadatoUtente> utenteEsternoList
    List<NotificaSoggetto> notificaSoggettoList
    String categoria
    String desktop
    String stato
    String tipologia
    String tipologiaDescrizione //serve per i filtri in SmartDesktop
    String datiApplicativi1
    String datiApplicativi2
    String datiApplicativi3
    String messaggioToDo
    String priorita
    String note
    String paramInitIter
    String espressione
    List<DettaglioNotificaJWorklist> dettagliNotificaJWorklistList
    List<AllegatoNotificaJWorklist> allegatiNotificaJWorklistList
    List<BottoneNotificaJWorklist> bottoniNotificaJWorklistList
    String ordinamentoStringaLabel
    String ordinamentoStringaValore
    String ordinamentoDataLabel
    Date ordinamentoDataValore
    String ordinamentoNumeroLabel
    int ordinamentoNumeroValore
    String urlAllegatiDinamici
}
