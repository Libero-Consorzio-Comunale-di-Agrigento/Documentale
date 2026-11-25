package it.finmatica.gestionedocumenti.testutils

import it.finmatica.gestionedocumenti.IntegrationSpec
import it.finmatica.so4.login.So4SpringSecurityService
import spock.lang.Ignore
import spock.lang.Specification
import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.impostazioni.Impostazione
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.impostazioni.ImpostazioniMap

@Ignore
class TestUtilsIntegrationSpec extends IntegrationSpec {

    So4SpringSecurityService springSecurityService
    ImpostazioniMap impostazioniMap

    def setup () {
        TestUtils.logout()
        impostazioniMap.setCodiceEnteSenzaLogin(ImpostazioniMap.ENTE_FALLBACK)
        impostazioniMap.refresh()
    }

    def cleanup () {
        TestUtils.logout()
        impostazioniMap.setCodiceEnteSenzaLogin(ImpostazioniMap.ENTE_FALLBACK)
        impostazioniMap.refresh()
    }

    void "test side effect login" () {
        when:
        Ente ente = TestUtils.setUpEnte()
        TestUtils.login("romagnoli.p", "P_MO")
        impostazioniMap.setCodiceEnteSenzaLogin(ente.id)
        TestUtils.setImpostazioni([ENTI_SO4:"P_MO"], ente)
        then:
        springSecurityService.isLoggedIn() == true
    }

    void "test reset" () {
        when:
        Ente ente = TestUtils.setUpEnte()
        TestUtils.setImpostazioni([ENTI_SO4: "P_MO"], ente)
        then:
        impostazioniMap.getCodiceEnteSenzaLogin() == ImpostazioniMap.ENTE_FALLBACK
        springSecurityService.isLoggedIn() == false
        Impostazioni.ENTI_SO4.valore == Impostazioni.ENTI_SO4.predefinito
    }

    void "test impostazioni" () {
        setup:
        Ente ente = new Ente(amministrazione: "P_MO", ottica: "1", descrizione: "Modena", aoo:"AOOPMO", sequenza: 1)
        ente.save()

        when:
        Impostazione impostazione = new Impostazione(codice:"ENTI_SO4", descrizione: "Ente So4", valore: "P_MO", ente:ente)
        impostazione.save()
        Impostazione i = Impostazione.findByCodiceAndEnte("ENTI_SO4", ente)
        then:
        i               != null
        i.codice        == "ENTI_SO4"
        i.descrizione   == "Ente So4"
        i.valore        == "P_MO"

        when:
        Impostazione impostazioneSenzaEnte = new Impostazione(codice:"ENTI_SO4", descrizione: "Ente So4", valore: "P_MO")
        impostazioneSenzaEnte.save()
        Impostazione senzaEnte = Impostazione.findByCodiceAndEnteIsNull("ENTI_SO4")
        then:
        senzaEnte               != null
        senzaEnte.codice        == "ENTI_SO4"
        senzaEnte.descrizione   == "Ente So4"
        senzaEnte.valore        == "P_MO"
    }

    void "test impostazioni non valide" () {
        setup:
        Ente enteValido     = new Ente(amministrazione: "P_MO", ottica: "1", descrizione: "Ente Valido", aoo:"VLD", sequenza: 1, valido: true)
        enteValido.save()

        Ente enteNonValido  = new Ente(amministrazione: "P_RA", ottica: "2", descrizione: "Non valido", aoo:"NNVLD", sequenza:2, valido: false)
        enteNonValido.save()

        new Impostazione(codice:"TEST_VALIDA", descrizione: "Test impostazione non valida per ente valido",     valore: "non valida", ente:enteValido).save()
        new Impostazione(codice:"TEST_NON_VALIDA", descrizione: "Test impostazione non valida per ente non valido", valore: "non valida", ente:enteNonValido).save()
        new Impostazione(codice:"TEST_VALIDA", descrizione: "Test impostazione non valida per ente null",       valore: "non valida", ente:null).save()

        new Impostazione(codice:"TEST_VALIDA", descrizione: "Test impostazione valida per ente valido",     valore: "non valida", ente:enteValido).save()
        new Impostazione(codice:"TEST_VALIDA", descrizione: "Test impostazione valida per ente non valido", valore: "non valida", ente:enteNonValido).save()
        new Impostazione(codice:"TEST_VALIDA", descrizione: "Test impostazione valida per ente null",       valore: "non valida", ente:null).save()

        when: "ottengo le impostazioni con ente valido o nullo"
        def impostazioniValide = impostazioniMap.getListaImpostazioni()
        then: "devo trovare solo le impostazioni con ente valido o nullo"
        impostazioniValide.size() == 4
        impostazioniValide.findAll {it.codice == "TEST_VALIDA" }.size() == 4
    }

    void "test login e setImpostazioni"() {
        when:
        Ente ente = TestUtils.setUpEnte()
        then:
        impostazioniMap.map.size() == 0

		when: "con una impostazione per un ente"
        TestUtils.setImpostazioni([ENTI_SO4: "P_MO"], ente)
		then: "senza utente loggato, il valore dell'impostazione è quello predefinito"
        impostazioniMap.getCodiceEnteSenzaLogin() == ImpostazioniMap.ENTE_FALLBACK
        springSecurityService.isLoggedIn() == false
        Impostazioni.ENTI_SO4.valore == Impostazioni.ENTI_SO4.predefinito

        when: "con una impostazione per un ente e il set dell'ente non autenticato"
        impostazioniMap.setCodiceEnteSenzaLogin(ente.id)
        then: "il valore dell'impostazione è quello corretto"
        Impostazioni.ENTI_SO4.valore == "P_MO"

		when: "con una impostazione per un ente e l'utente loggato"
        impostazioniMap.setCodiceEnteSenzaLogin(ImpostazioniMap.ENTE_FALLBACK)
        TestUtils.login("romagnoli.p", "P_MO")
		then: "il valore dell'impostazione è quello corretto"
        springSecurityService.loggedIn == true
        Impostazioni.ENTI_SO4.valore == "P_MO"
    }

    void "test impostazioni per ente null e per ente esistente"() {
        setup:
        Ente ente1 = TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        Ente ente2 = TestUtils.setUpEnte("P_RA", "2", "AOOPRA")
//        Ente ente2 = TestUtils.setUpEnte("P_BO", "3", "AOOPBO")
        TestUtils.setImpostazioni([OTTICA_SO4: "IST"])
        TestUtils.setImpostazioni([OTTICA_SO4: "1"], ente1)
        TestUtils.setImpostazioni([OTTICA_SO4: "2"], ente2)

		when: "con una impostazione per un ente"
		then: "senza utente loggato, il valore dell'impostazione è quello dell'ente null"
        Impostazioni.OTTICA_SO4.valore == "IST"

        when: "con una impostazione per un ente e il set dell'ente non autenticato"
        impostazioniMap.setCodiceEnteSenzaLogin(ente1.id)
        then: "il valore dell'impostazione è quello corretto"
        Impostazioni.OTTICA_SO4.valore == "1"

		when: "con una impostazione per un ente e l'utente loggato"
        impostazioniMap.setCodiceEnteSenzaLogin(ImpostazioniMap.ENTE_FALLBACK)
        TestUtils.login("romagnoli.p", "P_RA")
		then: "il valore dell'impostazione è quello corretto"
        springSecurityService.loggedIn == true
        Impostazioni.OTTICA_SO4.valore == "2"
    }

	void "test setup defaults" () {
		when:
		TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
		then:
		Ente.perAmministrazioneOtticaAoo("P_MO", "1", "AOOPMO").get() != null
	}

    void "test login con ente" () {
        when:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")
        then:
        springSecurityService.loggedIn == true
        springSecurityService.principal.ente != null
        springSecurityService.principal.ente.amministrazione.codice == "P_MO"
    }

    void "test logout" () {
        when:
        TestUtils.setUpEnte("P_MO", "1", "AOOPMO")
        TestUtils.login("romagnoli.p", "P_MO")
        then:
        springSecurityService.loggedIn == true

        when:
        TestUtils.logout()
        then:
        springSecurityService.loggedIn  == false
        springSecurityService.principal == null
    }
}
