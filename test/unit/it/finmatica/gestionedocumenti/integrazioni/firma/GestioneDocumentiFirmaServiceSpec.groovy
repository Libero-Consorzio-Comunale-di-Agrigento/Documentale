package it.finmatica.gestionedocumenti.integrazioni.firma

import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.DocumentoService
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.FileDocumentoRepository
import it.finmatica.gestionedocumenti.documenti.FileDocumentoService
import it.finmatica.gestionedocumenti.documenti.IGestoreFile
import it.finmatica.gestionedocumenti.documenti.StatoFirma
import it.finmatica.gestionedocumenti.documenti.beans.GestoreTransazioneFirma
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.test.GestioneDocumentiSpecification
import it.finmatica.gestionedocumenti.testutils.TestUtils
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import org.junit.Ignore

@Ignore
class GestioneDocumentiFirmaServiceSpec extends GestioneDocumentiSpecification {

    void "i file non PDF non devono essere aggiunti alla firma quando si firma PADES"() {
        given: "una firma PADES"
        GestioneDocumentiFirmaService service = new GestioneDocumentiFirmaService()
        TestUtils.setUpImpostazioni((Impostazioni.FIRMA_PADES.toString()): 'Y')
        service.gestoreTransazioneFirma = Mock(GestoreTransazioneFirma)
        service.gestoreFile = Mock(IGestoreFile)
        service.documentoService = Mock(DocumentoService)

        Documento documento = new Documento(statoFirma: StatoFirma.DA_FIRMARE)
        FileDocumento fileDocumento = new FileDocumento(nome: 'file.doc', contentType: TipoFile.PDF, valido: true, modificabile: true)
        documento.addToFileDocumenti(fileDocumento)
        documento.save(flush:true, failOnError:true)

        when: "aggiungo dei file non PDF alla firma"
        service.preparaFilePerFirma(fileDocumento, false, false, false)

        then: "non bisogna aggiungere il file alla firma"
        0 * service.gestoreTransazioneFirma.addFileDaFirmare(_, _, _)
    }

    void "il file principale con modello testo viene firmato con firma quando si firma PADES"() {
        given: "una firma PADES"

        GestioneDocumentiFirmaService service = new GestioneDocumentiFirmaService()

        TestUtils.setUpImpostazioni((Impostazioni.FIRMA_PADES.toString()): 'Y')
        service.gestoreTransazioneFirma = Mock(GestoreTransazioneFirma)
        service.gestoreFile = Mock(IGestoreFile)
        service.documentoService = Mock(DocumentoService)
        service.fileDocumentoService = Mock(FileDocumentoService)

        GestioneTestiTipoModello tipoModello = new GestioneTestiTipoModello(codice: 'TESTO', descrizione: 'test', query: '<root/>'.bytes).save(failOnError: true)
        GestioneTestiModello modelloTesto = new GestioneTestiModello(tipo: 'odt', nome: 'primo modello', fileTemplate: 'primo template'.bytes, tipoModello: tipoModello).save(failOnError: true)

        Documento documento = new Documento(statoFirma: StatoFirma.DA_FIRMARE)
        FileDocumento fileDocumento = new FileDocumento(codice: FileDocumento.CODICE_FILE_PRINCIPALE, nome: 'file.doc', modificabile: true, contentType: TipoFile.DOC, valido: true)
        fileDocumento.modelloTesto = modelloTesto
        documento.addToFileDocumenti(fileDocumento)
        documento.save(flush:true, failOnError:true)

        when: "aggiungo dei file principale non PDF alla firma"
        service.preparaFilePerFirma(fileDocumento, true, false, false)

        then: "bisogna verificare che il file princiaple sia stato aggiunto alla firma"
        1 * service.gestoreTransazioneFirma.addFileDaFirmare(_, _, _)
    }
}
