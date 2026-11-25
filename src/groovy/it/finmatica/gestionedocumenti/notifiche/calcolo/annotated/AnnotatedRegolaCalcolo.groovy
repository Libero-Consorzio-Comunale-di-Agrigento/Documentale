package it.finmatica.gestionedocumenti.notifiche.calcolo.annotated

import it.finmatica.gestionedocumenti.notifiche.calcolo.TipoNotifica

/**
 * Created by esasdelli on 07/04/2017.
 */
interface AnnotatedRegolaCalcolo {

    /**
     * Ritorna l'elenco di tipi di notifiche disponibili con questo repository
     * @return
     */
    List<TipoNotifica> getListaTipiNotifica()

    String getTipoDocumento (Object documento);

    String getIdRiferimento (Object documento);

    Object getDocumento (String idRiferimento);

}