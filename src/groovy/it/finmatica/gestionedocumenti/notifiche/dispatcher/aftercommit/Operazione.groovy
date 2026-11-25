package it.finmatica.gestionedocumenti.notifiche.dispatcher.aftercommit

import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto

/**
 * Created by esasdelli on 11/04/2017.
 */
class Operazione {

    enum Tipo { INVIA, ELIMINA, AGGIORNA }

    String idUtente
    String codiceNotifica
    Tipo tipo
    String nominativoUtente
    long idEnte
    long idNotifica
    String idRiferimento
    List<NotificaSoggetto> destinatari
    String oggetto
    String testo
    String messaggioTODO
    boolean async = true
    long sequenza

    long idDestinatarioNotificaAttivita
}
