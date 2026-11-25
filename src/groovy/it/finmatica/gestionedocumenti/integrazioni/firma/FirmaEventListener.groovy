package it.finmatica.gestionedocumenti.integrazioni.firma

import groovy.transform.CompileStatic

/**
 * Al termine della firma di un file, viene lanciato questo evento per consentire alle applicazioni che usano GestioneDocumenti
 * di intervenire sul singolo file e fare delle operazioni, ad esempio il Protocollo lo utilizza per verificare la firma del file.
 */
@CompileStatic
interface FirmaEventListener {

    /**
     * Viene eseguito dopo la firma del documento.
     *
     * @param event
     */
    void dopoFirma(FirmaEvent event)
}