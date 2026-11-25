package it.finmatica.gestionedocumenti.multiente

import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.documenti.Allegato
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.StatoFirma
import it.finmatica.gestionedocumenti.documenti.TipoAllegato
import it.finmatica.gestionedocumenti.testutils.TestUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import spock.lang.Ignore
import spock.lang.Specification

/**
 * Created by esasdelli on 16/12/2016.
 */
@Ignore
class MultiEnteIntegrationSpec extends Specification {

    GestioneDocumentiSpringSecurityService springSecurityService
    GestioneDocumentiUserDetailService userDetailsService

    def setup() {
        TestUtils.setUpEnte()
    }

    def cleanup() {
        TestUtils.logout()
    }

    def "test calcolo enti per utenti"() {
        when: "l'utente romagnoli.p appartiene a due enti"
        TestUtils.setUpEnte("P_PC", "A", "AOOPROPC")
        springSecurityService.reauthenticate("romagnoli.p")
        springSecurityService.principal.ente = Ente.perAmministrazioneOtticaAoo("P_MO").get().toDTO()
        def enti = userDetailsService.getEntiPerUtente(springSecurityService.principal.id)

        then:
        enti.size() == 2

        when: "l'utente romagnoli.p appartiene a due enti"
        springSecurityService.reauthenticate("martinelli.b")
        springSecurityService.principal.ente = Ente.perAmministrazioneOtticaAoo("P_MO").get().toDTO()
        enti = userDetailsService.getEntiPerUtente(springSecurityService.principal.id)

        then:
        enti.size() == 1
    }

    def "ente valorizzato automaticamente al salvataggio di un Documento"() {
        setup:
        springSecurityService.reauthenticate("romagnoli.p")
        springSecurityService.principal.ente = Ente.perAmministrazioneOtticaAoo("P_MO").get().toDTO()
        Documento documento = new Documento()

        when:
        documento.statoFirma = StatoFirma.DA_FIRMARE
        documento.save()

        then:
        documento.ente.id == springSecurityService.principal.idEnte
    }

    def "ente valorizzato automaticamente al salvataggio di una classe che estende Documento"() {
        setup:
        springSecurityService.reauthenticate("romagnoli.p")
        springSecurityService.principal.ente = Ente.perAmministrazioneOtticaAoo("P_MO").get().toDTO()

        TipoAllegato tipoAllegato = new TipoAllegato(descrizione: "Test", commento: "Test")
        tipoAllegato.save()

        Allegato allegato = new Allegato()
        allegato.tipoAllegato = tipoAllegato

        when:
        allegato.commento = "Test"
        allegato.descrizione = "Test"
        allegato.statoFirma = StatoFirma.DA_FIRMARE
        allegato.save()

        then:
        allegato.ente.id == springSecurityService.principal.idEnte
    }

    def "le domain che usano lo So4UserPrincipal.getAmministrazione() devono ancora funzionare."() {
        setup:
        springSecurityService.reauthenticate("romagnoli.p")
        springSecurityService.principal.ente = Ente.perAmministrazioneOtticaAoo("P_MO").get().toDTO()

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "TEST", nome: "test", descrizione: "Test")
        tipoOggetto.save()

        when:
        WkfAttore attore = new WkfAttore(tipoOggetto: tipoOggetto, nome: "Test", descrizione: "test")
        attore.save()

        then:
        attore.ente.codice == springSecurityService.principal.ente.amministrazione.codice
    }
}
