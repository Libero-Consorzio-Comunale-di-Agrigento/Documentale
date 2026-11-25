package it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist

import it.finmatica.atti.integrazioniws.ads.jworkflow.MetadatoUtente

/**
 * Created by esasdelli on 07/04/2017.
 */
class AllegatoNotificaJWorklist {
    String etichetta
    String tooltip //<!--Optional:-->
    String nomeFile
    String url
    String mimetype //<!--Optional:-->
    int priorita
    int firmato // 0/1
}
