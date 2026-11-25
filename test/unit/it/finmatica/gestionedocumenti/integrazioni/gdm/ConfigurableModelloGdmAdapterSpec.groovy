package it.finmatica.gestionedocumenti.integrazioni.gdm

import it.finmatica.gestionedocumenti.Holders
import it.finmatica.gestionedocumenti.documenti.Allegato
import org.springframework.context.ApplicationContext
import org.springframework.mock.env.MockEnvironment
import org.springframework.mock.env.MockPropertySource
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
class ConfigurableModelloGdmAdapterSpec extends Specification {

    private void setupEnvironment(String testConfig) {
        // inizializzo l'holder
        ApplicationContext applicationContext = Mock(ApplicationContext)

        Properties properties = new Properties()
        properties.load(new FileInputStream(testConfig))

        MockEnvironment env = new MockEnvironment()
        env.getPropertySources().addFirst(new MockPropertySource(properties))
        applicationContext.getEnvironment() >> env
        new Holders().setApplicationContext(applicationContext)
    }

    void "configurazione scritta fissa con area e modello"() {
        setup:
        setupEnvironment("test/unit/it/finmatica/gestionedocumenti/integrazioni/gdm/testconfig.properties")
        Allegato allegato = new Allegato()
        when:
        ConfigurableModelloGdmAdapter adapter = new ConfigurableModelloGdmAdapter()
        then:
        adapter.getConfigPerClass(Allegato, "area") == "SEGRETERIA"
        adapter.getConfigPerClass(Allegato, "modello") == "M_ALLEGATO"
        adapter.getCodiceModello(allegato) == "M_ALLEGATO"
        adapter.getCodiceArea(allegato) == "SEGRETERIA"
    }

}
