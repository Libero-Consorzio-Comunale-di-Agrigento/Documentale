package it.finmatica.gestionedocumenti.testutils

import it.finmatica.gestionedocumenti.impostazioni.ImpostazioniMap

class MockImpostazioni extends ImpostazioniMap {

    private final Map<String, String> impostazioni
    private final ImpostazioniMap originali

    MockImpostazioni(ImpostazioniMap originali) {
        this.impostazioni = [:]
        this.originali = originali
    }

    void addImpostazioni (Map<String, String> impostazioni) {
        this.impostazioni.putAll(impostazioni)
    }

    @Override
    String getValore(String impostazione, String valoreDefault) {
        if (impostazioni.containsKey(impostazione)) {
            return impostazioni[impostazione]
        }

        return originali.getValore(impostazione, valoreDefault)
    }
}
