package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gestionedocumenti.IntegrationSpec
import it.finmatica.gestionedocumenti.testutils.TestUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import org.apache.commons.io.IOUtils
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore

import java.security.MessageDigest

class AllegatoIntegrationSpec extends IntegrationSpec {

    @Autowired
    private IGestoreFile gestoreFile

    @Autowired
    private SessionFactory sessionFactory

    @Autowired
    private DocumentoService documentoService

    def setup() {
        TestUtils.setUpEnte()
        TestUtils.login()
    }

    def cleanup() {
        TestUtils.logout()
    }

    void "aggiunge e recupera gli allegati ad un documento"() {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "TEST_LETTERA", nome: "Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        Documento documento = new Documento(tipoOggetto: tipoOggetto)
        documento.save()

        TipoAllegato tipoAllegato = TipoAllegato.findByCodice("ALLEGATO")
        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato")
        allegato.save()

        when:
        documento.addDocumentoCollegato(allegato, Allegato.CODICE_TIPO_COLLEGAMENTO)
        documento.save()

        then:
        List<Allegato> allegati = documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)
        allegati.size() == 1
        allegati[0].descrizione == "Nuovo Allegato"
    }

    @Ignore
    void "caricamento di file sull'allegato"() {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")
        TipoAllegato tipoAllegato = TipoAllegato.findByCodice("ALLEGATO")

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "TEST_LETTERA", nome: "Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        Documento documento = new Documento(tipoOggetto: tipoOggetto, idDocumentoEsterno: 1)
        documento.save()

        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato")
        allegato.save()

        documento.addDocumentoCollegato(allegato, Allegato.CODICE_TIPO_COLLEGAMENTO)
        documento.save()

        when:
        FileDocumento fileDocumento = new FileDocumento(nome: "AllegatoIntegrationSpec.groovy", contentType: "text/groovy")
        InputStream inputStream = new File("test/integration/it/finmatica/gestionedocumenti/documenti/AllegatoIntegrationSpec.groovy").newInputStream()
        allegato.addToFileDocumenti(fileDocumento)
        gestoreFile.addFile(allegato, fileDocumento, inputStream)

        InputStream inputStreamOriginale = new File("test/integration/it/finmatica/gestionedocumenti/documenti/AllegatoIntegrationSpec.groovy").newInputStream()
        InputStream inputStreamLettoDaGdm = gestoreFile.getFile(allegato, allegato.fileDocumenti.first())
        String md5inputStreamOriginale = MessageDigest.getInstance("MD5").digest(IOUtils.toByteArray(inputStreamOriginale)).encodeHex().toString()
        String md5inputStreamLettoDaGdm = MessageDigest.getInstance("MD5").digest(IOUtils.toByteArray(inputStreamLettoDaGdm)).encodeHex().toString()

        then:
        inputStreamOriginale != null
        inputStreamLettoDaGdm != null
        md5inputStreamOriginale == md5inputStreamLettoDaGdm
    }

    @Ignore
    void "elimina allegato e suoi file"() {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")
        TipoAllegato tipoAllegato = TipoAllegato.findByCodice("ALLEGATO")

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "TEST_LETTERA", nome: "Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato")
        allegato.save()

        FileDocumento fileDocumento = new FileDocumento(nome: "AllegatoIntegrationSpec.groovy", contentType: "text/groovy")
        InputStream inputStream = new File("test/integration/it/finmatica/gestionedocumenti/documenti/AllegatoIntegrationSpec.groovy").newInputStream()
        gestoreFile.addFile(allegato, fileDocumento, inputStream)

        FileDocumento fileDocumento1 = new FileDocumento(nome: "DocumentoServiceIntegrationSpec.groovy", contentType: "text/groovy")
        InputStream inputStream1 = new File("test/integration/it/finmatica/gestionedocumenti/documenti/DocumentoServiceIntegrationSpec.groovy").newInputStream()
        gestoreFile.addFile(allegato, fileDocumento1, inputStream1)

        sessionFactory.getCurrentSession().flush()
        sessionFactory.getCurrentSession().clear()

        when:
        Allegato a = Documento.get(allegato.id)
        documentoService.eliminaAllegato(a)

        then:
        FileDocumento.get(fileDocumento1.id) == null
        FileDocumento.get(fileDocumento.id) == null
    }

    @Ignore
    void "eliminazione di un file"() {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")
        TipoAllegato tipoAllegato = TipoAllegato.findByCodice("ALLEGATO")

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "TEST_LETTERA", nome: "Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato", documentoPrincipale: new Documento(idDocumentoEsterno: -1))
        allegato.save()

        FileDocumento fileDocumento = new FileDocumento(nome: "AllegatoIntegrationSpec.groovy", contentType: "text/groovy")
        InputStream inputStream = new File("test/integration/it/finmatica/gestionedocumenti/documenti/AllegatoIntegrationSpec.groovy").newInputStream()
        gestoreFile.addFile(allegato, fileDocumento, inputStream)
        sessionFactory.getCurrentSession().flush()
        sessionFactory.getCurrentSession().clear()

        when:
        Documento doc = Documento.get(allegato.id)
        documentoService.eliminaFileDocumento(Documento.get(allegato.id), fileDocumento.id)

        then:
        doc.fileDocumenti.size() == 0
        FileDocumento.get(fileDocumento.id) == null
    }

    /*void "aggiornamento sequenza di due file allegati"() {
        setup:
        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")
        TipoAllegato tipoAllegato = TipoAllegato.findByCodice("ALLEGATO")

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "TEST_LETTERA", nome: "Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato")
        allegato.save()

        FileDocumento fileDocumento = new FileDocumento(nome: "AllegatoIntegrationSpec.groovy", contentType: "text/groovy")
        InputStream inputStream = new File("test/integration/it/finmatica/gestionedocumenti/documenti/AllegatoIntegrationSpec.groovy").newInputStream()
        gestoreFile.addFile(allegato, fileDocumento, inputStream, true)

        FileDocumento fileDocumento1 = new FileDocumento(nome: "DocumentoServiceIntegrationSpec.groovy", contentType: "text/groovy")
        InputStream inputStream1 = new File("test/integration/it/finmatica/gestionedocumenti/documenti/DocumentoServiceIntegrationSpec.groovy").newInputStream()
        gestoreFile.addFile(allegato, fileDocumento1, inputStream1, true)

        InputStream inputStream2 = new File("test/integration/it/finmatica/gestionedocumenti/documenti/DocumentoServiceIntegrationSpec.groovy").newInputStream()
        gestoreFile.addFile(allegato, fileDocumento1, inputStream2, true)

        sessionFactory.getCurrentSession().flush()
        sessionFactory.getCurrentSession().clear()

        when:
        Documento.get(allegato.id)

        then:
        FileDocumento.get(fileDocumento.id).sequenza == 0
        FileDocumento.get(fileDocumento1.id).sequenza == 1
    }*/
}
