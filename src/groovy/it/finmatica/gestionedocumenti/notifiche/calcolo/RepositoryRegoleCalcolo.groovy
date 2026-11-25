package it.finmatica.gestionedocumenti.notifiche.calcolo

import it.finmatica.gestionedocumenti.notifiche.Allegato
import it.finmatica.gestionedocumenti.notifiche.NotificaDestinatario

interface RepositoryRegoleCalcolo {

    String getTipoDocumento(Object documento)

    String getIdRiferimento (Object documento)

    Object getDocumento (String idRiferimento)

    /**
     * Ritorna l'elenco di tipi di notifiche disponibili con questo repository
     * @return
     */
    List<TipoNotifica> getListaTipiNotifica()

    /**
     * Ritorna l'elenco delle regole di calcolo disponibili per il calcolo dei tag da sostituire nel testo e nell'oggetto della notifica.
     * @return
     */
    List<RegolaCalcoloNotifica> getRegoleCalcoloTag()

    /**
     * Ritorna l'elenco delle regole di calcolo disponibili per il calcolo dei destinatari
     * @return
     */
    List<RegolaCalcoloNotifica> getRegoleCalcoloDestinatari()

    /**
     * Ritorna l'elenco delle regole di calcolo disponibili per il calcolo degli allegati
     * @return
     */
    List<RegolaCalcoloNotifica> getRegoleCalcoloAllegati()

    /**
     * Esegue la regola di calcolo per il tag
     */
    String calcolaTag (String codiceRegolaCalcolo, Object documento)

    /**
     * Esegue la regola di calcolo dei destinatari
     */
    List<NotificaSoggetto> calcolaDestinatari (String codiceRegolaCalcolo, Object documento, NotificaDestinatario notificaDestinatario)

    /**
     * Esegue la regola di calcolo dell'allegato
     */
    List<Allegato> calcolaAllegati (String codiceRegolaCalcolo, Object documento)
}
