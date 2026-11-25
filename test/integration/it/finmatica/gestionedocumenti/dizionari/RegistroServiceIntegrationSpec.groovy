package it.finmatica.gestionedocumenti.dizionari

import it.finmatica.gestionedocumenti.IntegrationSpec
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.registri.Registro
import it.finmatica.gestionedocumenti.registri.RegistroService
import it.finmatica.gestionedocumenti.registri.TipoRegistro
import it.finmatica.gestionedocumenti.testutils.TestUtils
import spock.lang.Ignore

@Ignore("Ignorato perché GDO_TIPI_REGISTRO è una vista che va su GDM.")
class RegistroServiceIntegrationSpec extends IntegrationSpec {

    RegistroService registroService

    def setup() {
        TestUtils.setUpEnte()
        TestUtils.login()
    }

    def cleanup() {
        TestUtils.logout()
    }

    void "test Registro Valido"() {
        given:
        TipoRegistro tipoRegistro = new TipoRegistro(codice: 'AUTO', commento: "Automatico", rinnovoAutomatico: true, valido: true)
        tipoRegistro.save()

        Registro registro = new Registro(tipoRegistro: tipoRegistro, anno: 2016, ultimoNumero: 10, dataUltimoNumero: Date.parse("dd/MM/yyyy", "22/06/2016"), valido: true, validoDal: Date.parse("dd/MM/yyyy", "01/01/2016"))
        registro.save()

        when:
        Registro r = registroService.getRegistroAperto(tipoRegistro, Date.parse("dd/MM/yyyy", "22/06/2016"))

        then:
        r != null
        r.tipoRegistro.codice == "AUTO"
        r.ultimoNumero == 10
        r.valido == true
    }

    void "test Registro Non Presente e Non Automatico"() {
        given:
        TipoRegistro tipoRegistro = new TipoRegistro(codice: 'AUTO', commento: "Automatico", rinnovoAutomatico: false, valido: true)
        tipoRegistro.save()

        Registro registro = new Registro(tipoRegistro: tipoRegistro, anno: 2015, ultimoNumero: 10, dataUltimoNumero: Date.parse("dd/MM/yyyy", "22/06/2016"), valido: true, validoDal: Date.parse("dd/MM/yyyy", "01/01/2016"))
        registro.save()

        when:
        Registro r = registroService.getRegistroAperto(tipoRegistro, Date.parse("dd/MM/yyyy", "22/06/2016"))

        then:
        def e = thrown(GestioneDocumentiRuntimeException)
        e.message == "Non esiste nessun registro aperto con codice ${tipoRegistro.codice} per l'anno 2016. Il tipo di registro non è rinnovabile automaticamente."
    }

    void "test Registro Non Presente e Automatico"() {
        given:
        TipoRegistro tipoRegistro = new TipoRegistro(codice: 'AUTO', commento: "Automatico", rinnovoAutomatico: true, valido: true)
        tipoRegistro.save()

        Registro registro = new Registro(tipoRegistro: tipoRegistro, anno: 2015, ultimoNumero: 10, dataUltimoNumero: Date.parse("dd/MM/yyyy", "22/06/2016"), valido: true, validoDal: Date.parse("dd/MM/yyyy", "01/01/2016"))
        registro.save()

        when:
        Registro r = registroService.getRegistroAperto(tipoRegistro, Date.parse("dd/MM/yyyy", "22/06/2016"))

        then:
        r != null
        r.tipoRegistro.codice == "AUTO"
        r.ultimoNumero == 0
        r.valido == true
    }
}
