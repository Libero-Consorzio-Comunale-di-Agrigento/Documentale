package it.finmatica.gestionedocumenti.test

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.gestionedocumenti.Holders
import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiUserDetails
import it.finmatica.gestionedocumenti.testutils.TestUtils
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.so4.login.detail.Amministrazione
import it.finmatica.so4.struttura.So4Amministrazione
import org.apache.commons.lang.math.RandomUtils
import org.springframework.context.ApplicationContext
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.Ignore

/**
 * Questo test è ignorato perché è la classe base per gli unit-test di questo progetto.
 */
@Ignore("ignorato perché è la classe base degli unit-test di questo progetto.")
class GestioneDocumentiSpecification extends HibernateSpecification {

    GestioneDocumentiSpringSecurityService springSecurityService

    private Holders holders
    private it.finmatica.gestionetesti.Holders holders1

    void setup() {

        String nominativo = "mario.rossi"
        String codiceAmm = "TEST_AMM"

        Ad4Utente utente = new Ad4Utente(nominativo: nominativo, utente: nominativo).save(failOnError: true, flush: true)

        As4SoggettoCorrente soggettoCorrente = new As4SoggettoCorrente(nome: nominativo, cognome: nominativo, utenteAd4: utente)
        soggettoCorrente.id = RandomUtils.nextLong()
        soggettoCorrente.save(failOnError: true, flush: true)

        As4SoggettoCorrente soggettoAmm = new As4SoggettoCorrente()
        soggettoAmm.denominazione = codiceAmm
        soggettoAmm.nome = codiceAmm
        soggettoAmm.cognome = codiceAmm
        soggettoAmm.id = RandomUtils.nextLong()
        soggettoAmm.save(failOnError: true, flush: true)

        So4Amministrazione amm = new So4Amministrazione()
        amm.codice = codiceAmm
        amm.soggetto = soggettoAmm
        amm.save(failOnError: true, flush: true)

        Ente ente = new Ente(descrizione: codiceAmm, amministrazione: amm).save(failOnError:true, flush:true)

        GestioneDocumentiUserDetails so4UserDetail = Mock(GestioneDocumentiUserDetails)
        so4UserDetail.getId() >> utente.id
        so4UserDetail.getUsername() >> utente.nominativo
        so4UserDetail.getAmministrazione() >> amm
        so4UserDetail.getUtente() >> utente
        so4UserDetail.getEnte() >> ente
        so4UserDetail.amm() >> new Amministrazione(codice: amm.codice, descrizione: amm.codice)

        GestioneDocumentiSpringSecurityService springSecurityService = Mock(GestioneDocumentiSpringSecurityService)
        springSecurityService.getPrincipal() >> so4UserDetail
        springSecurityService.getCurrentUser() >> utente

        SecurityContext securityContext = Mock(SecurityContext)
        Authentication authentication = Mock(Authentication)
        authentication.getPrincipal() >> so4UserDetail
        authentication.isAuthenticated() >> true
        authentication.getDetails() >> so4UserDetail
        securityContext.getAuthentication() >> authentication
        SecurityContextHolder.setContext(securityContext)

        // inizializzo l'holder
        ApplicationContext applicationContext = Mock(ApplicationContext)
        applicationContext.getBean(SpringSecurityService) >> springSecurityService
        applicationContext.getBean(So4SpringSecurityService) >> springSecurityService
        applicationContext.getBean(GestioneDocumentiSpringSecurityService) >> springSecurityService

        this.springSecurityService = springSecurityService

        holders = new Holders()
        holders.setApplicationContext(applicationContext)

        holders1 = new it.finmatica.gestionetesti.Holders()
        holders1.setApplicationContext(applicationContext)
    }

    void cleanup () {
        holders.setApplicationContext(null)
        holders1.setApplicationContext(null)
    }
}
