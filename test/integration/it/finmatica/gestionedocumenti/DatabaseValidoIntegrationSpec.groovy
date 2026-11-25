package it.finmatica.gestionedocumenti

import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.gestionedocumenti.documenti.DocumentoService
import it.finmatica.gestionedocumenti.integrazioni.firma.GestioneDocumentiFirmaService
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate

class DatabaseValidoIntegrationSpec extends IntegrationSpec {

    @Autowired
    private SessionFactory sessionFactory

    @Autowired
    private TransactionTemplate transactionTemplate

    @Autowired
    private DocumentoService documentoService

    @Autowired
    private GestioneDocumentiFirmaService gestioneDocumentiFirmaService

    @Autowired
    private SpringSecurityService springSecurityService

    void "il db è valido e i bean sono inizializzati"() {
        expect:
        sessionFactory != null
        documentoService != null
        gestioneDocumentiFirmaService != null
        transactionTemplate != null
    }
}
