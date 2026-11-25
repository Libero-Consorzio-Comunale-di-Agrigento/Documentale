package it.finmatica.gestionedocumenti.test

import it.finmatica.gestionedocumenti.documenti.Documento

class AbstractDomainSpec extends GestioneDocumentiSpecification {

    void "il bean springSecurityService deve essere utilizzabile dalle domain-class che estendono AbstractDomain"() {
        when:
        Documento doc = new Documento().save(failOnError: true, flush:true)
        then:
        doc.id > 0
        doc.utenteIns.nominativo == "mario.rossi"
        doc.ente.descrizione == "TEST_AMM"
    }
}
