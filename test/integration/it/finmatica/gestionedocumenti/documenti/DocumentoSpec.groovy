package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gestionedocumenti.IntegrationSpec
import it.finmatica.gestionedocumenti.testutils.TestUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import spock.lang.Ignore

@Ignore("ignorato a causa del trigger GDO_TIPI_DOCUMENTO_TIU che impedisce l'insert sulla tabella")
class DocumentoSpec extends IntegrationSpec {

    def setup () {
        TestUtils.setUpEnte()
        TestUtils.login()
    }

    def cleanup () {
        TestUtils.logout()
    }

    def "gestione documenti collegati" () {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")
        TestUtils.setUpTipoCollegamento("NON_ESISTE", "Non Esiste")

        TipoDocumento tipoDocumento = new TipoDocumento(codice: "LETTERA_ENTRATA", descrizione: "Lettera in Entrata", commento: "Lettera in Entrata")
        tipoDocumento.save()

        WkfTipoOggetto tipoOggetto = TestUtils.setUpTipoOggetto("LETTERA", "Lettera")

        Documento documento = new Documento(tipoOggetto: tipoOggetto)
        documento.save()

        TipoAllegato tipoAllegato = new TipoAllegato(codice: "ALLEGATO", descrizione: "Allegato", commento: "Allegato")
        tipoAllegato.save()

        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato")
        allegato.save()

        Allegato allegato2 = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato 2")
        allegato2.save()

        documento.save()

        when: "non ho documenti collegati, devo ricevere una lista vuota da getDocumentiCollegati"
        then:
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) != null
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 0
        documento.getNumeroDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) == 0

        when: "aggiungo un documento-collegato che non è presente sul documento, questo viene aggiunto"
        documento.addDocumentoCollegato (allegato, Allegato.CODICE_TIPO_COLLEGAMENTO)
        documento.save()
        then:
        documento.documentiCollegati.size() == 1
        documento.getNumeroDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) == 1
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 1
        documento.getDocumentiCollegati("NON_ESISTE").size() == 0
        Allegato a = documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)[0]
        a               != null
        a.descrizione   == allegato.descrizione
        a.id            == allegato.id

        when: "aggiungo un documento-collegato che è già presente sul documento, questo NON viene aggiunto"
        documento.addDocumentoCollegato (allegato, Allegato.CODICE_TIPO_COLLEGAMENTO)
        documento.save()
        then:
        documento.documentiCollegati.size() == 1
        documento.getNumeroDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) == 1
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 1
        Allegato a2 = documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)[0]
        a2               != null
        a2.descrizione   == allegato.descrizione
        a2.id            == allegato.id

        when: "aggiungo un nuovo documento-collegato, questo viene aggiunto"
        documento.addDocumentoCollegato (allegato2, Allegato.CODICE_TIPO_COLLEGAMENTO)
        documento.save()
        then:
        documento.documentiCollegati.size() == 2
        documento.getNumeroDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) == 2
        documento.getNumeroDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) == 2
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 2

        when: "elimino un documento collegato, questo viene rimosso dalla lista"
        DocumentoCollegato documentoCollegato = documento.removeDocumentoCollegato (allegato, Allegato.CODICE_TIPO_COLLEGAMENTO)
        documentoCollegato?.delete()
        documento.save()
        then:
        documento.documentiCollegati.size() == 1
        documento.getNumeroDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) == 1
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 1
        Allegato a3 = documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)[0]
        a3.descrizione  == allegato2.descrizione
        a3.id           == allegato2.id

        when: "tento di eliminare un documento collegato che non è presente, non succede nulla"
        DocumentoCollegato documentoCollegato1 = documento.removeDocumentoCollegato(allegato, Allegato.CODICE_TIPO_COLLEGAMENTO)
        documentoCollegato1?.delete()
        documento.save()
        then:
        documentoCollegato1 == null
        documento.documentiCollegati.size() == 1
        documento.getNumeroDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO) == 1
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 1
        Allegato a4 = documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)[0]
        a4.descrizione  == allegato2.descrizione
        a4.id           == allegato2.id
    }
}
