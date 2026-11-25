package it.finmatica.gestionedocumenti.documenti

import spock.lang.Specification
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import spock.lang.Ignore

@Ignore
class TipoDocumentoIntegrationSpec extends Specification {

    def setup () {
        TestUtils.setUpEnte()
        TestUtils.login("romagnoli.p", "P_MO")
    }

    def cleanup () {
        TestUtils.logout()
    }

    void "namedQuery modelliTesto e modelloTestoPredefinito" () {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")

        TipoDocumento tipoDocumento = new TipoDocumento(codice: "LETTERA_ENTRATA", descrizione: "Lettera in Entrata", commento: "Lettera in Entrata")
        tipoDocumento.save()

        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: "TEST_LETTERA", query: "test".getBytes(), descrizione: "test")
        tipoModello.save()
        GestioneTestiModello modelloTesto = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test", descrizione: "test")
        modelloTesto.save()

        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto, predefinito: true, codice: "TESTO_PRINCIPALE")
        tipoDocumento.save()

        when:
        List<GestioneTestiModello> modelliTesto = TipoDocumento.modelliTesto(tipoDocumento.id, "TESTO_PRINCIPALE").list()
        then:
        modelliTesto.size() == 1
        TipoDocumento.modelloTestoPredefinito(tipoDocumento.id, "TESTO_PRINCIPALE").get().id == modelloTesto.id

        when:
        GestioneTestiModello modelloTesto2 = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test 2", descrizione: "test 2")
        modelloTesto2.save()
        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto2, predefinito: false, codice: "TESTO_PRINCIPALE")
        tipoDocumento.save()
        then:
        TipoDocumento.modelliTesto(tipoDocumento.id, "TESTO_PRINCIPALE").list().size() == 2
        TipoDocumento.modelloTestoPredefinito(tipoDocumento.id, "TESTO_PRINCIPALE").get().id == modelloTesto.id

        when:
        GestioneTestiModello modelloTesto3 = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test 2", descrizione: "test 2")
        modelloTesto3.save()
        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto3, predefinito: true, codice: "FRONTESPIZIO")
        tipoDocumento.save()
        then:
        TipoDocumento.modelliTesto(tipoDocumento.id, "TESTO_PRINCIPALE").list().size() == 2
        TipoDocumento.modelloTestoPredefinito(tipoDocumento.id, "TESTO_PRINCIPALE").get().id == modelloTesto.id
    }

    void "Non deve essere possibile collegare due volte lo stesso modello testo per lo stesso codice" () {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")

        TipoDocumento tipoDocumento = new TipoDocumento(codice: "LETTERA_ENTRATA", descrizione: "Lettera in Entrata", commento: "Lettera in Entrata")
        tipoDocumento.save()

        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: "TEST_LETTERA", query: "test".getBytes(), descrizione: "test")
        tipoModello.save()
        GestioneTestiModello modelloTesto = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test", descrizione: "test")
        modelloTesto.save()

        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto, predefinito: true, codice: "TESTO_PRINCIPALE")
        tipoDocumento.save()

        GestioneTestiModello modelloTesto2 = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test 2", descrizione: "test 2")
        modelloTesto2.save()
        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto2, predefinito: false, codice: "TESTO_PRINCIPALE")
        tipoDocumento.save()

        when: "tento di aggiungere lo stesso modello di testo per un codice diverso"
        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto, predefinito: true, codice: "FRONTESPIZIO")
        tipoDocumento.save()
        then: "deve essere possibile avere lo stesso modello testo per codici diversi"
        TipoDocumento.modelliTesto(tipoDocumento.id, "TESTO_PRINCIPALE").list().size()  == 2
        TipoDocumento.modelliTesto(tipoDocumento.id, "FRONTESPIZIO").list().size()      == 1
        TipoDocumento.modelloTestoPredefinito(tipoDocumento.id, "TESTO_PRINCIPALE").get().id == modelloTesto.id
        TipoDocumento.modelloTestoPredefinito(tipoDocumento.id, "FRONTESPIZIO").get().id     == modelloTesto.id
    }

    void "Deve esistere almeno un modello associato predefinito per ogni codice di modello associato" () {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")
        TipoDocumento tipoDocumento = new TipoDocumento(codice: "LETTERA_ENTRATA", descrizione: "Lettera in Entrata", commento: "Lettera in Entrata")
        tipoDocumento.save()
        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: "TEST_LETTERA", query: "test".getBytes(), descrizione: "test")
        tipoModello.save()
        GestioneTestiModello modelloTesto = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test", descrizione: "test")
        modelloTesto.save()

        when: "aggiungo solo un modello di testo non predefinito, do' errore"
        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto, predefinito: false, codice: "TESTO_PRINCIPALE")
        tipoDocumento.save()
        then:
        thrown(ValidationException) // non deve essere possibile salvare
    }

    void "Deve esistere al massimo un modello associato predefinito per ogni codice di modello associato" () {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")
        TipoDocumento tipoDocumento = new TipoDocumento(codice: "LETTERA_ENTRATA", descrizione: "Lettera in Entrata", commento: "Lettera in Entrata")
        tipoDocumento.save()
        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: "TEST_LETTERA", query: "test".getBytes(), descrizione: "test")
        tipoModello.save()
        GestioneTestiModello modelloTesto = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test", descrizione: "test")
        modelloTesto.save()
        GestioneTestiModello modelloTesto2 = new GestioneTestiModello(tipoModello: tipoModello, fileTemplate: "test".getBytes(), tipo:"odt", nome:"test 2", descrizione: "test 2")
        modelloTesto.save()

        when: "aggiungo due modelli predefiniti per lo stesso codice"
        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto, predefinito: true, codice: "TESTO_PRINCIPALE")
        tipoDocumento.addToModelliAssociati(modelloTesto: modelloTesto2, predefinito: true, codice: "TESTO_PRINCIPALE")
        tipoDocumento.save()
        then:
        thrown() // non deve essere possibile salvare
    }
}
