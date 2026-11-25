package it.finmatica.gestionedocumenti.integrazioni.gdm

import it.finmatica.gestionedocumenti.Config
import it.finmatica.gestionedocumenti.IntegrationSpec
import it.finmatica.gestionedocumenti.documenti.Allegato
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(properties = [
        "finmatica.gestionedocumenti.modellogdm.it.finmatica.gestionedocumenti.documenti.Allegato.area=SEGRETERIA",
        "finmatica.gestionedocumenti.modellogdm.it.finmatica.gestionedocumenti.documenti.Allegato.modello=ALLEGATO"
], classes = Config)

class IntegrazioneGdmServiceIntegrationSpec extends IntegrationSpec {

    void "verifica il caricamento dei dati del ConfigurableModelloGdmAdapter"() {
        setup:
        Allegato allegato = new Allegato()

        expect:
        new ConfigurableModelloGdmAdapter().getCodiceArea(allegato) == "SEGRETERIA"
        new ConfigurableModelloGdmAdapter().getCodiceModello(allegato) == "ALLEGATO"
    }
}
