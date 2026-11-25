package it.finmatica.gestionedocumenti

import it.finmatica.gestionedocumenti.soggetti.LayoutSoggetti
import it.finmatica.gestionedocumenti.soggetti.MetodoCalcoloSoggetti
import it.finmatica.gestionedocumenti.soggetti.RegolaCalcoloSoggetti
import it.finmatica.gestionedocumenti.soggetti.TipoSoggetto
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoDTO
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoDTOService
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoRegolaDTO
import it.finmatica.gestionedocumenti.testutils.TestUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import org.springframework.context.ApplicationContext
import spock.lang.Ignore

@Ignore
class SoggettiTestIntegrationSpec extends IntegrationSpec {

    ApplicationContext applicationContext
    TipologiaSoggettoDTOService tipologiaSoggettoDTOService

    def setup() {
        // attraverso il resource.groovy viene caricato il resource-test.xml per l'ambiente di test
        TestUtils.setUpEnte()
        TestUtils.login("romagnoli.p", "P_MO")
    }

    def cleanup() {
        TestUtils.logout()
    }

    void "configurazione dei bean"() {

        given:
        def regoleDiCalcoloSoggettiConfig = applicationContext.getBean('regoleDiCalcoloSoggettiConfigTest')
        List<LayoutSoggetti> listaZulPossibili = regoleDiCalcoloSoggettiConfig.getLayoutSoggettiList()
        List<RegolaCalcoloSoggetti> listaRegoleComponenteDefault = regoleDiCalcoloSoggettiConfig.getListaRegoleComponenteDefault()
        List<RegolaCalcoloSoggetti> listaRegoleUnitaDefault = regoleDiCalcoloSoggettiConfig.getListaRegoleUnitaDefault()
        List<RegolaCalcoloSoggetti> listaRegoleComponenteLista = regoleDiCalcoloSoggettiConfig.getListaRegoleComponenteLista()
        List<RegolaCalcoloSoggetti> listaRegoleUnitaLista = regoleDiCalcoloSoggettiConfig.getListaRegoleUnitaLista()

        expect:
        listaZulPossibili == [[label       : "label test",
                               tipoOggetto : "LETTERA",
                               descrizione : "descr test",
                               url         : "/test.zul",
                               suggerimento: "suggerimento test",
                               soggetti    : [[categoria  : MetodoCalcoloSoggetti.Categoria.UNITA,
                                               codice     : TipoSoggetto.REDATTORE,
                                               titolo     : "titolo1",
                                               descrizione: "descrizione1"],
                                              [categoria  : MetodoCalcoloSoggetti.Categoria.COMPONENTE,
                                               codice     : TipoSoggetto.FIRMATARIO,
                                               titolo     : "titolo2",
                                               descrizione: "descrizione2"]]]]

        listaRegoleComponenteDefault.get(0).id == "regoleCalcoloSoggettiRepositoryTest.getDefaultComponentiTest"
        listaRegoleUnitaDefault.get(0).id == "regoleCalcoloSoggettiRepositoryTest.getDefaultUnitaTest"
        listaRegoleComponenteLista.get(0).id == "regoleCalcoloSoggettiRepositoryTest.getListaComponentiTest"
        listaRegoleUnitaLista.get(0).id == "regoleCalcoloSoggettiRepositoryTest.getListaUnitaTest"
    }

    void "creazione tipologia soggetto"() {

        given:
        def regoleDiCalcoloConfig = applicationContext.getBean('regoleDiCalcoloSoggettiConfigTest')
        List<LayoutSoggetti> listaZulPossibili = regoleDiCalcoloConfig.getLayoutSoggettiList()
        List<TipologiaSoggettoRegolaDTO> listaCarTipiSoggetto = new ArrayList<TipologiaSoggettoRegolaDTO>()
        TipologiaSoggettoRegolaDTO tipologiaSoggettoRegolaDTO = new TipologiaSoggettoRegolaDTO(sequenza: 0, tipoSoggetto: "LETTERA",)
        listaCarTipiSoggetto.add(tipologiaSoggettoRegolaDTO)
        new WkfTipoOggetto(codice: "LETTERA", nome: "test").save()

        when:
        TipologiaSoggettoDTO tipologia = new TipologiaSoggettoDTO(commento: "titolo test", descrizione: "descr test", valido: true, layoutSoggetti: listaZulPossibili.get(0).url, tipoOggetto: WkfTipoOggetto.get("LETTERA").toDTO())
        def idTipologia = tipologiaSoggettoDTOService.salva(tipologia, listaCarTipiSoggetto)

        then:
        idTipologia != null
    }
}
