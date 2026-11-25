package it.finmatica.gestionedocumenti.documenti

import spock.lang.Specification

class StatoFirmaSpec extends Specification {

    void "verifica che lo stato firma interrotta corrisponda agli stati IN_FIRMA e FIRMATO_DA_SBLOCCARE"() {
        expect:
        StatoFirma.IN_FIRMA.firmaInterrotta == true
        StatoFirma.FIRMATO_DA_SBLOCCARE.firmaInterrotta == true
    }
}
