package it.finmatica.gestionedocumenti.test

import it.finmatica.gestionedocumenti.integrazioni.firma.FirmaEvent
import it.finmatica.gestionedocumenti.integrazioni.firma.FirmaEventListener

class VerificaFirmaEventListener implements FirmaEventListener {
    @Override
    void dopoFirma(FirmaEvent event) {
        // mock
    }
}
