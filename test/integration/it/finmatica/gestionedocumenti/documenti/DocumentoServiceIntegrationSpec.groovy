package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gestionedocumenti.testutils.TestUtils
import spock.lang.Specification
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestionetesti.GestioneTestiService
import org.zkoss.util.media.Media
import spock.lang.Ignore

@Ignore
class DocumentoServiceIntegrationSpec extends Specification {

    DocumentoService documentoService

    def setup () {
        TestUtils.setUpEnte()
        TestUtils.login("romagnoli.p", "P_MO")
    }

    def cleanup () {
        TestUtils.logout()
    }

    void "upload ed eliminazione di file allegati" () {
        setup:
        TipoAllegato tipoAllegato = new TipoAllegato(codice: "ALLEGATO", descrizione: "Allegato", commento: "Allegato")
        tipoAllegato.save()

        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato")
        allegato.save()

        Media media = Mock(Media)
        media.isBinary()        >> { true }
        media.getStreamData()   >> { new File("test/integration/it/finmatica/gestionedocumenti/documenti/DocumentoServiceIntegrationSpec.groovy").newInputStream() }
        media.getContentType()  >> { "text/groovy" }
        media.getName()         >> { "DocumentoServiceIntegrationSpec.groovy" }

        when:
        FileDocumento fileDocumento = documentoService.uploadFile(allegato, media)
        then:
        fileDocumento != null
        fileDocumento.nome          == "DocumentoServiceIntegrationSpec.groovy"
        fileDocumento.contentType   == "text/groovy"
        fileDocumento.dimensione > 0
        fileDocumento.id > 0
        allegato.fileDocumenti.size() == 1

        when:
        documentoService.eliminaFileDocumento(allegato, fileDocumento.id)
        then:
        allegato.fileDocumenti.size() == 0
        FileDocumento.get(fileDocumento.id) == null
    }

    void getNomeFileUnivoco () {
        setup:
        TipoAllegato tipoAllegato = new TipoAllegato(codice: "ALLEGATO", descrizione: "Allegato", commento: "Allegato")
        tipoAllegato.save()

        Allegato allegato = new Allegato(tipoAllegato: tipoAllegato, descrizione: "Nuovo Allegato")
        allegato.save()

        AllegatoDTO allegatoDTO = allegato.toDTO()

        Media media = Mock(Media)
        media.isBinary()        >> { true }
        media.getStreamData()   >> { new File("test/integration/it/finmatica/gestionedocumenti/documenti/DocumentoServiceIntegrationSpec.groovy").newInputStream() }
        media.getContentType()  >> { "text/groovy" }
        media.getName()         >> { "DocumentoServiceIntegrationSpec.groovy" }

        when:
        String nome1 = documentoService.getNomeFileUnivoco(allegato.id, "DocumentoServiceIntegrationSpec.groovy")
        FileDocumento fileDocumento1 = documentoService.uploadFile(allegato, media)
        then:
        nome1 == "DocumentoServiceIntegrationSpec.groovy"
        fileDocumento1.nome == nome1

        when:
        String nome2 = documentoService.getNomeFileUnivoco(allegato.id, "DocumentoServiceIntegrationSpec.groovy")
        FileDocumento fileDocumento2 = documentoService.uploadFile(allegato, media)
        then:
        nome2 == "DocumentoServiceIntegrationSpec(1).groovy"
        fileDocumento2.nome == nome2
    }

    void "salvare un Allegato al momento dell'upload di un file" () {
        setup:
        TipoAllegato tipoAllegato = new TipoAllegato(codice: "ALLEGATO", descrizione: "Allegato", commento: "Allegato")
        tipoAllegato.save()

        AllegatoDTO allegatoDto = new AllegatoDTO(tipoAllegato: tipoAllegato.toDTO(), descrizione: "Nuovo Allegato")

        TestUtils.setUpTipoCollegamento(Allegato.CODICE_TIPO_COLLEGAMENTO, "Allegato")

        TipoDocumento tipoDocumento = new TipoDocumento(codice: "LETTERA_ENTRATA", descrizione: "Lettera in Entrata", commento: "Lettera in Entrata", funzionarioObbligatorio: true)
        tipoDocumento.save()

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "LETTERA", nome:"Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        Documento lettera = new Documento(tipoOggetto: tipoOggetto)
        lettera.save()

        DocumentoDTO documentoPrincipaleDto = lettera.toDTO()

        Media media = Mock(Media)
        media.isBinary()        >> { true }
        media.getStreamData()   >> { new File("test/integration/it/finmatica/gestionedocumenti/documenti/DocumentoServiceIntegrationSpec.groovy").newInputStream() }
        media.getContentType()  >> { "text/groovy" }
        media.getName()         >> { "DocumentoServiceIntegrationSpec.groovy" }

        when:
        Allegato allegato = documentoService.salvaAllegato (allegatoDto, documentoPrincipaleDto, media)
        then:
        allegato != null
        Documento documento = documentoPrincipaleDto.domainObject
        documento != null
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 1

        when:
        allegatoDto             = allegato.toDTO()
        documentoPrincipaleDto  = documento.toDTO()
        documentoService.eliminaAllegato(documento, allegato)
        documento = documentoPrincipaleDto.domainObject
        allegato  = allegatoDto.domainObject
        then:
        allegato == null
        documento.getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO).size() == 0
    }

    void "edita testo / elimina testo" () {
        setup:
        TipoDocumento tipoDocumento = new TipoDocumento(codice: "LETTERA_ENTRATA", descrizione: "Lettera in Entrata", commento: "Lettera in Entrata", funzionarioObbligatorio: true)
        tipoDocumento.save()

        WkfTipoOggetto tipoOggetto = new WkfTipoOggetto(codice: "LETTERA", nome:"Lettera", descrizione: "Lettera")
        tipoOggetto.save()

        Documento lettera = new Documento(tipoOggetto: tipoOggetto)
        lettera.save()

        FileDocumento testoPrincipale = new FileDocumento(codice: "TESTO_PRINCIPALE", nome: "LETTERA.odt", contentType: GestioneTestiService.getContentType("odt"), valido:true, modificabile:true, firmato:false)
        lettera.addToFileDocumenti(testoPrincipale)
        lettera.save()

        when:
        documentoService.eliminaTesto(lettera.toDTO(), testoPrincipale.toDTO())
        lettera.refresh()
        then:
        lettera.fileDocumenti.size() == 0
    }

    void "salva file originale" () {
        setup:
        Documento lettera = TestUtils.setUpDocumento()
        FileDocumento fileOriginale = TestUtils.setUpFile(lettera)

        when:
        documentoService.salvaFileStorico(lettera, lettera.filePrincipale, FileDocumento.CODICE_FILE_ORIGINALE)
        FileDocumento fileStorico = lettera.filePrincipale.getFileStorico(FileDocumento.CODICE_FILE_ORIGINALE)
        then:
        fileStorico.nome 		    == fileOriginale.nome
        fileStorico.modificabile    == fileOriginale.modificabile
        fileStorico.modelloTesto    == fileOriginale.modelloTesto
        fileStorico.contentType     == fileOriginale.contentType
        fileStorico.dimensione      == fileOriginale.dimensione
        fileStorico.idFileEsterno   == fileOriginale.idFileEsterno
        fileStorico.revisione       == 1
        lettera.fileDocumenti.findAll { it.codice == FileDocumento.CODICE_FILE_ORIGINALE }.size()  == 1
        lettera.fileDocumenti.findAll { it.codice == FileDocumento.CODICE_FILE_PRINCIPALE }.size() == 1
        lettera.filePrincipale.fileStorici.findAll { it.codice == FileDocumento.CODICE_FILE_ORIGINALE }.size()  == 1

        when: "se tento di salvare un nuovo file originale già esistente, devo solo cambiare il n. di revisione (ed il nome del file nel caso)"
        lettera.filePrincipale.nome == "test 2.odt"
        lettera.filePrincipale.save()
        documentoService.salvaFileStorico(lettera, lettera.filePrincipale, FileDocumento.CODICE_FILE_ORIGINALE)
        then:
        lettera.fileDocumenti.findAll { it.codice == FileDocumento.CODICE_FILE_ORIGINALE }.size() == 1
        lettera.fileDocumenti.findAll { it.codice == FileDocumento.CODICE_FILE_PRINCIPALE }.size() == 1
        lettera.filePrincipale.fileStorici.findAll { it.codice == FileDocumento.CODICE_FILE_ORIGINALE }.size()  == 1

        when: "se tento di salvare un nuovo file originale già esistente, devo solo cambiare il n. di revisione (ed il nome del file nel caso)"
        lettera.filePrincipale.nome == "test 3.odt"
        lettera.filePrincipale.save()
        documentoService.salvaFileStorico(lettera, lettera.filePrincipale, "DOPO_FIRMA")
        then:
        lettera.fileDocumenti.findAll { it.codice == FileDocumento.CODICE_FILE_ORIGINALE }.size() == 1
        lettera.fileDocumenti.findAll { it.codice == FileDocumento.CODICE_FILE_PRINCIPALE }.size() == 1
        lettera.fileDocumenti.findAll { it.codice == "DOPO_FIRMA" }.size() == 1
        lettera.filePrincipale.fileStorici.findAll { it.codice == "DOPO_FIRMA" }.size()  == 1
    }
}
