package it.finmatica.gestionedocumenti.integrazioni.firma

import it.finmatica.firmadigitale.FirmaDigitaleFineFirmaCallback
import it.finmatica.firmadigitale.async.FirmaDigitaleProgressStatus
import it.finmatica.firmadigitale.async.FirmaDigitaleProgressStatusHolder
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.zkutils.SuccessHandler
import org.springframework.transaction.annotation.Transactional

class FineFirmaCallback implements FirmaDigitaleFineFirmaCallback {

    private final GestioneDocumentiFirmaService gestioneDocumentiFirmaService
    private final FirmaDigitaleProgressStatusHolder firmaDigitaleProgressStatusHolder
    private final SuccessHandler successHandler

    FineFirmaCallback(GestioneDocumentiFirmaService gestioneDocumentiFirmaService, SuccessHandler successHandler, FirmaDigitaleProgressStatusHolder firmaDigitaleProgressStatusHolder) {
        this.gestioneDocumentiFirmaService = gestioneDocumentiFirmaService
        this.firmaDigitaleProgressStatusHolder = firmaDigitaleProgressStatusHolder
        this.successHandler = successHandler
    }

    @Override
    void fineFirma(long idTransazioneFirma, boolean saltaVerifica=false) {
        try {
            // ottengo la lista dei documenti da sbloccare
            List<Long> idDocumentiDaSbloccare = gestioneDocumentiFirmaService.getDocumentiDaSbloccare(idTransazioneFirma)
            int incrementoParziale = 100 / (idDocumentiDaSbloccare.size() + 2);
            int percentualeAvanzamento = incrementoParziale

            firmaDigitaleProgressStatusHolder.setStatus(idTransazioneFirma, new FirmaDigitaleProgressStatus(percentualeAvanzamento, "Salvataggio dei documenti firmati in corso..."))

            // per prima cosa, salvo tutti file firmati sui relativi documenti:
            gestioneDocumentiFirmaService.fineFirmaSalvaFileFirmati(idTransazioneFirma,saltaVerifica)

            // per ogni file firmato, se è un documento da sbloccare, lo sblocco:
            int i = 0
            for (Long idDocumento : idDocumentiDaSbloccare) {

                percentualeAvanzamento += incrementoParziale
                i++
                firmaDigitaleProgressStatusHolder.setStatus(idTransazioneFirma, new FirmaDigitaleProgressStatus(percentualeAvanzamento, "Processo il documento n. ${i} / ${idDocumentiDaSbloccare.size()}..."))

                // svuoto la sessione hibernate per ogni documento.
                // Questo lo faccio perché potrei dover processare molti documenti e non voglio che si generi
                // troppo casino con una sola sessione hibernate più grande e più transazioni diverse
                // (siccome ogni invocazione al metodo fineFirmaSbloccaFlusso è una nuova transazione)
                gestioneDocumentiFirmaService.fineFirmaSbloccaFlusso(idDocumento)
            }
        } finally {
            // svuoto i messaggi del message handler:
            successHandler.clearMessages()
        }
    }
}

