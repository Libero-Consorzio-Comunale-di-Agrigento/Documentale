package it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist

import it.finmatica.atti.integrazioniws.ads.jworkflow.MetadatoUtente

/**
 * Created by esasdelli on 07/04/2017.
 */
class BottoneNotificaJWorklist {
    String etichetta
    String tooltip
    String icona
    String tipologia    //la stessa della notifica
    int    multiplo // S/N
    String urlAzione //copia qui urlEsecuzione - in caso di TODO scrivere TODO
    String parametri
    String identificativoRiferimento //copia qui idRiferimento
}
