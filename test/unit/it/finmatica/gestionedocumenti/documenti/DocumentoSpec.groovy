package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gestionedocumenti.test.GestioneDocumentiSpecification
import org.hibernate.proxy.HibernateProxy
import spock.lang.Ignore
import spock.lang.Issue

class DocumentoSpec extends GestioneDocumentiSpecification {

    void "getAllegati() non deve dare errore di cast exception a causa del proxy hibernate"() {
        setup:
        Documento documento = new Documento()
        Allegato allegato = new Allegato()
        allegato.save()
        Allegato allegato2 = new Allegato()
        allegato2.save()
        TipoCollegamento tipoCollegamento = new TipoCollegamento(codice: Allegato.CODICE_TIPO_COLLEGAMENTO, descrizione: "Allegato").save()
        documento.addToDocumentiCollegati(new DocumentoCollegato(tipoCollegamento: tipoCollegamento, collegato: allegato))
        documento.addToDocumentiCollegati(new DocumentoCollegato(tipoCollegamento: tipoCollegamento, collegato: allegato2))
        documento.save(flush: true)

        getSessionFactory().getCurrentSession().clear()

        when:
        Documento doc = Documento.get(documento.id)
        List<Allegato> allegati = doc.getAllegati()

        then:
        allegati.size() == 2
        allegati.collect { Allegato a -> a.id }.size() == 2
    }

    void "la proprietà documento di FileDocumento non deve essere un proxy"() {
        setup:
        Documento documento = new Documento()

        Allegato allegato = new Allegato()
        allegato.save()

        Allegato allegato2 = new Allegato()
        allegato2.save()

        TipoCollegamento tipoCollegamento = new TipoCollegamento(codice: Allegato.CODICE_TIPO_COLLEGAMENTO, descrizione: "Allegato").save()
        documento.addToDocumentiCollegati(new DocumentoCollegato(tipoCollegamento: tipoCollegamento, collegato: allegato))
        documento.addToDocumentiCollegati(new DocumentoCollegato(tipoCollegamento: tipoCollegamento, collegato: allegato2))
        documento.save(flush: true)

        FileDocumento fileDocumento = new FileDocumento(nome: "file.txt", contentType: "text/plain")
        documento.addToFileDocumenti(fileDocumento)
        documento.save()

        getSessionFactory().getCurrentSession().flush()
        getSessionFactory().getCurrentSession().clear()

        expect:
        !(FileDocumento.get(fileDocumento.id).documento instanceof HibernateProxy)
    }

    // TODO: a runtime ottengo errore. questo test ancora non replica correttamente.
    @Ignore
    @Issue("non riesco a riprodurre questo errore.")
    void "eliminare un file da un documento non deve dare errore NULL su db"() {
        setup:
        Documento documento = new Documento()
        documento.addToFileDocumenti(new FileDocumento(nome: "test.pdf", contentType: "application/pdf", idFileEsterno: 123, codice: FileDocumento.CODICE_FILE_PRINCIPALE))
        documento.addToFileDocumenti(new FileDocumento(nome: "test2.pdf", contentType: "application/pdf", idFileEsterno: 1234, codice: FileDocumento.CODICE_FILE_ALLEGATO))
        documento.addToFileDocumenti(new FileDocumento(nome: "test3.pdf", contentType: "application/pdf", idFileEsterno: 1235, codice: FileDocumento.CODICE_FILE_ALLEGATO))
//        documento.addToSoggetti(new DocumentoSoggetto(tipoSoggetto: TipoSoggetto.UO_PROTOCOLLANTE, utenteAd4: new Ad4Utente(id:"ute", nominativo: "utente").save()))
        sessionFactory.currentSession.save(documento)
        sessionFactory.currentSession.flush()
        sessionFactory.currentSession.clear()

        when:
        documento = sessionFactory.currentSession.get(Documento, documento.id)
        documento.removeFromFileDocumenti(documento.fileDocumenti[1])
        documento.save(flush: true)

        then:
        noExceptionThrown()
        documento.soggetti.size() == 0
    }
}
