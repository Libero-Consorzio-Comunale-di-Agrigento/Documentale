package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.gestionedocumenti.test.GestioneDocumentiSpecification
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.so4.login.So4UserDetail
import org.apache.commons.lang.math.RandomUtils

class As4SoggettoCorrenteSpec extends GestioneDocumentiSpecification {

    void "è possibile fare query su as4_v_soggetti_correnti"() {
        setup:
        As4SoggettoCorrente soggettoCorrente = new As4SoggettoCorrente(nome: "mario", cognome: "rossi")
        soggettoCorrente.id = RandomUtils.nextLong()
        soggettoCorrente.save(failOnError: true, flush: true)

        when:
        As4SoggettoCorrente trovato = As4SoggettoCorrente.findByNome("mario")

        then:
        trovato != null
    }

    void "test so4 user detail"() {
        setup:
        Ad4Utente utente = new Ad4Utente(nominativo: "re.n", utente: "re.n").save(failOnError: true, flush: true)

        As4SoggettoCorrente soggettoCorrente = new As4SoggettoCorrente(nome: "mario", cognome: "rossi", utenteAd4: utente)
        soggettoCorrente.id = RandomUtils.nextLong()
        soggettoCorrente.save(failOnError: true, flush: true)

        So4SpringSecurityService springSecurityService = Mock(So4SpringSecurityService)
        So4UserDetail so4UserDetail = new So4UserDetail(utente, [], [])
        springSecurityService.getPrincipal() >> so4UserDetail

        expect:
        so4UserDetail.getUtente() != null
    }
}
