package it.finmatica.gestionedocumenti.integrazioni.documentale

import groovy.util.logging.Slf4j
import it.finmatica.gestionedocumenti.commons.MarkableFileInputStream
import it.finmatica.gestionedocumenti.documenti.Allegato
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.DocumentoCollegato
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.IDocumentoEsterno
import it.finmatica.gestionedocumenti.documenti.IFileDocumento
import it.finmatica.gestionedocumenti.documenti.IGestoreFile
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.integrazioni.gdm.converters.BooleanConverter
import it.finmatica.gestionedocumenti.utils.FileInputStreamDeleteOnClose
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.smartdoc.api.DocumentaleService
import it.finmatica.smartdoc.api.struct.Campo
import it.finmatica.so4.login.So4SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.sql.SQLException

@Slf4j
@Transactional
@Service
class IntegrazioneDocumentaleService implements IGestoreFile {

    @Autowired
    private DocumentaleService documentaleService
    @Autowired
    private So4SpringSecurityService springSecurityService
    @Autowired
    private GestioneTestiService gestioneTestiService

    @Override
    void addFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, InputStream is, boolean escludiControlloCompetenze = false) {
        MarkableFileInputStream inputStream = null
        try {
            if (is != null) {
                BufferedInputStream inputStreamBuffered
                if (is instanceof FileInputStreamDeleteOnClose) {
                    inputStreamBuffered = new BufferedInputStream(is)
                    inputStream = new MarkableFileInputStream(inputStreamBuffered)
                } else {
                    inputStream = new MarkableFileInputStream(is)
                }

                if (inputStream.markSupported()) {
                    inputStream.mark(Integer.MAX_VALUE)
                }
            }

            // carico il file sul documentale:
            addFileToDocumentale(documento, fileDocumento, inputStream, escludiControlloCompetenze)

            // ora che ho letto tutto lo stream, ne ho la dimensione e la scrivo:

            if (is != null) {

                fileDocumento.dimensione = inputStream.getByteCount()
            }

            // questo è normalmente true per:
            // - MarkableFileInputStream	<- questo arriva quando si fa un upload di file più grosso di qualche centinaio di KB
            // - ByteArrayInputStream		<- questo arriva quando si fa un edita/testo o upload di file "piccoli"
            // - OracleBlobInputStream 		<- questo non so se arriverà mai
            if (is != null) {
                if (inputStream.markSupported()) {
                    inputStream.reset()

                    // tento di leggere il testo ocr del file:
                    String nomeFileEffettivo = fileDocumento.nome
                    try {
                        if (fileDocumento.p7m) {
                            nomeFileEffettivo = fileDocumento.getNomeFileSbustato()
                        }

                        fileDocumento.testo = gestioneTestiService.getTestoOCR(inputStream, nomeFileEffettivo, fileDocumento.p7m)
                    } catch (Throwable e) {
                        // in caso di errore in lettura del testo ocr non faccio niente.
                        log.warn("Non sono riuscito a leggere il testo del file: ${nomeFileEffettivo} con id: ${fileDocumento.id}")
                    }
                }
            }

            fileDocumento.documento = documento
            fileDocumento.save()
            documento.addToFileDocumenti(fileDocumento)
        } finally {
            // chiudo qui l'input stream siccome nell'upload alla profilo (se documentale è GDM), anch'essa tenta di chiuderlo
            // ma avendo sovrascritto il metodo close() per poter rileggere l'inputstream,
            // c'è bisogno di chiuderlo qui.
            inputStream?.chiudiMeglio()
        }
    }

    @Override
    InputStream getFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, boolean escludiControlloCompetenze = false) {
        if (!(fileDocumento.idFileEsterno > 0)) {
            throw new GestioneDocumentiRuntimeException("Non è possibile leggere il file '${fileDocumento.nome}': Non è ancora stato salvato il file sul documentale.")
        }

        it.finmatica.smartdoc.api.struct.Documento documentoDocumentale
        if (!(documento.idDocumentoEsterno > 0)) {
            documentoDocumentale = new it.finmatica.smartdoc.api.struct.Documento()
        } else {
            documentoDocumentale = getDocumento(documento.idDocumentoEsterno.toString(), escludiControlloCompetenze)
        }

        documentoDocumentale.addChiaveExtra("ESCLUDI_CONTROLLO_COMPETENZE", "Y")

        it.finmatica.smartdoc.api.struct.File fileRet = new it.finmatica.smartdoc.api.struct.File()
        fileRet.setId(fileDocumento.idFileEsterno.toString())

        fileRet = documentaleService.getFile(documentoDocumentale, fileRet)
        return fileRet.getInputStream()
    }

    @Override
    void removeFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, boolean spostaFile = false) {

        if (!(documento.idDocumentoEsterno > 0)) {
            throw new GestioneDocumentiRuntimeException("Non è possibile eliminare il file '${fileDocumento.nome}': il documento non è ancora stato salvato sul documentale.")
        }

        it.finmatica.smartdoc.api.struct.File fileToDelete = new it.finmatica.smartdoc.api.struct.File(fileDocumento.nome)
        it.finmatica.smartdoc.api.struct.Documento documentoSmart = new it.finmatica.smartdoc.api.struct.Documento()
        documentoSmart.setId(documento.idDocumentoEsterno?.toString())

        documentoSmart.addChiaveExtra("ESCLUDI_CONTROLLO_COMPETENZE", "Y")
        documentoSmart = documentaleService.getDocumento(documentoSmart, new ArrayList<it.finmatica.smartdoc.api.struct.Documento.COMPONENTI>())

        if (spostaFile) {
            if ("REPOSITORY" != documentoSmart.getMappaChiaviExtra().get("MODELLO")) {

                fileToDelete.setId(fileDocumento.idFileEsterno?.toString())
                it.finmatica.smartdoc.api.struct.Documento documentoSmartRepository = new it.finmatica.smartdoc.api.struct.Documento()
                documentoSmartRepository.addChiaveExtra("AREA", "GDMSYS")
                documentoSmartRepository.addChiaveExtra("MODELLO", "REPOSITORY")
                documentaleService.salvaDocumento(documentoSmartRepository)

                documentaleService.spostaFileInDocumento(fileToDelete, documentoSmartRepository)

            }
            return
        }

        documentoSmart.removeFile(fileToDelete)
        documentaleService.salvaDocumento(documentoSmart)
    }

    /**
     * 	Cambia il nome del file su GDM
     */
    void renameFileName(Long idDocumentoEsterno, Long idFileEsterno, String fileName) {
        it.finmatica.smartdoc.api.struct.Documento documentoSmart = new it.finmatica.smartdoc.api.struct.Documento()

        documentoSmart.addChiaveExtra("ESCLUDI_CONTROLLO_COMPETENZE", "Y")

        it.finmatica.smartdoc.api.struct.File fileOriginale = new it.finmatica.smartdoc.api.struct.File()
        fileOriginale.setId("" + idFileEsterno)
        fileOriginale = documentaleService.getFile(documentoSmart, fileOriginale)
        fileOriginale.setNome(fileName)

        documentoSmart.addFile(fileOriginale)
        documentaleService.salvaDocumento(documentoSmart)
    }

    it.finmatica.smartdoc.api.struct.Documento salva(IDocumentoEsterno documento) {
        it.finmatica.smartdoc.api.struct.Documento documentoSmart = new it.finmatica.smartdoc.api.struct.Documento()
        if (documento.class.name == Allegato.name) {
            documentoSmart = costruisciAllegato((Allegato) documento)
        }
        return documentaleService.salvaDocumento(documentoSmart)
    }

    void addFileToDocumentale(Documento documento, FileDocumento fileDocumento, InputStream is, boolean escludiControlloCompetenze = false) {
        // per prima cosa salvo il documento sia sulla mia base dati che sul documentale se non è già salvato:
        if (!(documento.idDocumentoEsterno > 0)) {

            // eseguo una flush della sessione hibernate per avere "dati freschi"
            fileDocumento.documento = documento
            fileDocumento.save()

            // salvo il documento sul documentale
            it.finmatica.smartdoc.api.struct.Documento documentoDocumentale = salva(documento)

            // salvo il documento con l'id documento esterno del documentale
            documento.idDocumentoEsterno = Long.parseLong(documentoDocumentale.getId())
            documento.save()
        }

        // salvo il file sul documentale (senza inputStrim, salvo solo il collegamento
        salvaFile(documento, fileDocumento, is, escludiControlloCompetenze)
    }

    void addFileToRepositoryDocumentale(Documento documento, FileDocumento fileDocumento, InputStream is, Long idDocumentoRepository = null) {
        boolean creatoNuovoFile = true
        it.finmatica.smartdoc.api.struct.Documento documentoSmart = new it.finmatica.smartdoc.api.struct.Documento()

        documentoSmart.addChiaveExtra("ESCLUDI_CONTROLLO_COMPETENZE", "Y")

        if (idDocumentoRepository != null) {
            documentoSmart.setId(idDocumentoRepository.toString())
        } else {
            documentoSmart.addChiaveExtra("AREA", "GDMSYS")
            documentoSmart.addChiaveExtra("MODELLO", "REPOSITORY")
        }
        it.finmatica.smartdoc.api.struct.File fileToSave = new it.finmatica.smartdoc.api.struct.File()
        if (fileDocumento.idFileEsterno > 0) {
            fileToSave.setId(fileDocumento.idFileEsterno.toString())
            creatoNuovoFile = false
        }
        fileToSave.setNome(fileDocumento.nome)
        fileToSave.setInputStream(is)

        documentoSmart.addFile(fileToSave)
        documentaleService.salvaDocumento(documentoSmart)

        if (creatoNuovoFile) {
            fileDocumento.idFileEsterno = Long.parseLong(fileToSave.getId())
        }

        fileDocumento.documento = documento
        fileDocumento.save()

        documento.addToFileDocumenti(fileDocumento)
        documento.save()
    }

    private void salvaFile(Documento documento, FileDocumento fileDocumento, InputStream inputStream, boolean escludiControlloCompetenze = false) {
        boolean creatoNuovoFile = true

        it.finmatica.smartdoc.api.struct.File fileToSave = new it.finmatica.smartdoc.api.struct.File()
        if (fileDocumento.idFileEsterno > 0) {
            fileToSave.setId(fileDocumento.idFileEsterno.toString())
            creatoNuovoFile = false
        }
        fileToSave.setNome(fileDocumento.nome)
        if (inputStream) {
            fileToSave.setInputStream(inputStream)
        }

        it.finmatica.smartdoc.api.struct.Documento documentoSmart = getDocumento(Long.toString(documento.idDocumentoEsterno), escludiControlloCompetenze)

        documentoSmart.addFile(fileToSave)
        documentaleService.salvaDocumento(documentoSmart)

        if (creatoNuovoFile) {
            fileDocumento.idFileEsterno = Long.parseLong(fileToSave.getId())
        }

        fileDocumento.documento = documento
        fileDocumento.save()
    }

    it.finmatica.smartdoc.api.struct.Documento getDocumento(String idDocumentoEsterno, boolean escludiControlloCompetenze = false) {
        try {
            it.finmatica.smartdoc.api.struct.Documento documentoSmart = new it.finmatica.smartdoc.api.struct.Documento()

            if (escludiControlloCompetenze) {
                documentoSmart.addChiaveExtra("ESCLUDI_CONTROLLO_COMPETENZE", "Y")
            }
            documentoSmart.setId(String.valueOf(idDocumentoEsterno))
            return documentaleService.getDocumento(documentoSmart, [it.finmatica.smartdoc.api.struct.Documento.COMPONENTI.VERSIONI])
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }

    private it.finmatica.smartdoc.api.struct.Documento costruisciAllegato(Allegato allegato) {
        it.finmatica.smartdoc.api.struct.Documento documento = new it.finmatica.smartdoc.api.struct.Documento()

        String area = "SEGRETERIA"
        String codiceModello = "M_ALLEGATO_PROTOCOLLO"

        documento.addChiaveExtra("AREA", area)
        documento.addChiaveExtra("MODELLO", codiceModello)
        Long documentoPadre = allegato.documentoPrincipale?.idDocumentoEsterno
        if (documentoPadre) {
            documento.addChiaveExtra("ID_DOCUMENTO_PADRE", Long.toString(documentoPadre))
        }

        documento.addCampo(new Campo("TITOLO_DOCUMENTO", allegato.descrizione))
        documento.addCampo(new Campo("DESCRIZIONE", allegato.descrizione))
        documento.addCampo(new Campo("UBICAZIONE_DOC_ORIGINALE", allegato.ubicazione))
        documento.addCampo(new Campo("ORIGINE_DOC", allegato.origine))
        documento.addCampo(new Campo("QUANTITA", allegato.quantita))
        documento.addCampo(new Campo("NUMERO_PAG", allegato.numPagine))
        documento.addCampo(new Campo("CODICE_AMMINISTRAZIONE", allegato.ente?.amministrazione?.codice))
        documento.addCampo(new Campo("CODICE_AOO", allegato.ente?.aoo))
        documento.addCampo(new Campo("RISERVATO", BooleanConverter.Y_N.INSTANCE.convert(allegato.riservato)))

        documento.addCampo(new Campo("TIPO_ALLEGATO", allegato.tipoAllegato.acronimo))
        if (allegato.idDocumentoEsterno != null) {
            documento.setId("" + allegato.idDocumentoEsterno)
        }

        DocumentoCollegato documentoCollegato = allegato.getCollegamentoDocumentoPrincipale()

        if (documentoCollegato != null) {
            it.finmatica.smartdoc.api.struct.Documento documentoPrincipale = new it.finmatica.smartdoc.api.struct.Documento()
            documentoPrincipale.setId(("" + documentoCollegato.getDocumento().idDocumentoEsterno))
            documentoPrincipale = documentaleService.getDocumento(documentoPrincipale, [])
            Campo campoIdRif = documentoPrincipale.trovaCampo("IDRIF")
            if (campoIdRif != null) {
                documento.addCampo(new Campo("IDRIF", ("" + campoIdRif.valore)))
            }
        }

        return documento
    }
}