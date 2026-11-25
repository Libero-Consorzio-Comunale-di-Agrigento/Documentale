package it.finmatica.gestionedocumenti.documenti.beans

import groovy.util.logging.Slf4j
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.gestionedocumenti.commons.Ad4Service
import it.finmatica.gestionedocumenti.documenti.Allegato
import it.finmatica.gestionedocumenti.documenti.DocumentoService
import it.finmatica.gestionedocumenti.documenti.IFileDocumento
import it.finmatica.gestionedocumenti.documenti.IGestoreFile
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.integrazioni.gdm.IntegrazioneGdmService
import it.finmatica.gestioneiter.IGestoreCompetenze
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.jsign.api.SignedObjectReaderFactory
import it.finmatica.jsign.api.SignedObjectReaderI
import it.finmatica.segreteria.common.FirmaUtilities
import it.finmatica.smartdoc.api.struct.File
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.zkoss.util.media.AMedia
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Window

import javax.servlet.http.HttpServletResponse

/**
 * TODO: questa classe va rivista e (forse) eliminata
 */
@Slf4j
class FileDownloader {

    @Autowired
    IntegrazioneGdmService integrazioneGdmService
    @Autowired
    SpringSecurityService springSecurityService
    @Autowired
    GestioneTestiService gestioneTestiService
    @Autowired
    IGestoreCompetenze gestoreCompetenze
    @Autowired
    IGestoreFile gestoreFile
    @Autowired
    Ad4Service ad4Service
    @Autowired
    DocumentoService documentoService

    /**
     * Ottiene un file storico da GDM.
     * @deprecated
     * Questa funzione è relativa al download dei file storici dallo storico JSON che va eliminato.
     * Piuttosto utilizzare la funzione "downloadFileStorico" che prende come parametro anche l'idFile
     *
     * @param idDocumentoEsterno
     * @param revisione
     * @param nomeFile
     */
    @Deprecated
    void downloadFileStorico(Long idDocumentoEsterno, Long revisione, String nomeFile) {
        InputStream inputStream = integrazioneGdmService.getFileStorico(idDocumentoEsterno, revisione, nomeFile)
        AMedia media = new AMedia(nomeFile, null, contentType, inputStream);
        Filedownload.save(media)
        //Filedownload.save(inputStream, "application/octet-stream", nomeFile)
    }

    void downloadFileStorico(Long idDocumentoEsterno, Long revisione, String nomeFile, Long idFile) {
        File file = integrazioneGdmService.getFileStorico(idDocumentoEsterno, revisione, idFile)
        AMedia media = new AMedia(nomeFile, null, "application/octet-stream", file.getInputStream());
        Filedownload.save(media)
        //Filedownload.save(file.getInputStream(), "application/octet-stream", nomeFile)
    }

    Window downloadFileAllegato(def documento, IFileDocumento fileDocumento, boolean solalettura = true, boolean controllaRiservato = true) {

        String testoLog = "Download file allegato con id=${fileDocumento?.id?.toString()}"
        if (documento.hasProperty("TIPO_OGGETTO") && documento.hasProperty("id")) {
            ad4Service.logAd4("Download file allegato ${documento?.tipoOggetto?.codice}", testoLog)
        }

        String logAd4 = "Download file ${fileDocumento?.codice}. ${getDettagliLog(fileDocumento)}"
        String annotazione = "id file_documento: ${fileDocumento?.id?.toString()}"
        ad4Service.eventoLogAd4(logAd4, documento?.id?.toString(), annotazione)

        // se il file allegato è un p7m, apro la popup di download/verifica.
        if (fileDocumento.isFirmato()) {
            Window w = Executions.createComponents("/commons/popupFileFirmato.zul", null,
                    [documento: documento?.toDTO(), nomeFile: fileDocumento?.nome, idFileAllegato: fileDocumento?.id, storico: false, solalettura: solalettura])
            w.doModal()
            return w
        }

        downloadFile(documento, fileDocumento, solalettura)
        return null
    }

    void downloadFile(def documento, IFileDocumento fileDocumento, boolean trasformaInPdf = true, boolean sbusta = false, HttpServletResponse response = null) {
        // ottengo l'input stream:
        InputStream inputStream = gestoreFile.getFile(documento, fileDocumento, true)

        if (inputStream == null) {
            throw new GestioneDocumentiRuntimeException("Attenzione! File Storico non trovato!")
        }

        String nomeFile = fileDocumento.nome
        String contentType = fileDocumento.contentType

        try {
            // se devo sbustare, sbusto e scarico il file
            if (sbusta) {
                if (!fileDocumento.isPdf() && FirmaUtilities.isSigned(inputStream, nomeFile)) {
                    inputStream = gestoreFile.getFile(documento, fileDocumento)
                    SignedObjectReaderI reader = SignedObjectReaderFactory.getSignedObjectReader(inputStream);
                    inputStream = reader.getOriginalContent()

                } else {
                    inputStream = gestoreFile.getFile(documento, fileDocumento)
                }
                nomeFile = fileDocumento.getNomeFileSbustato()

                contentType = GestioneTestiService.getContentType(nomeFile.substring(nomeFile.lastIndexOf(".")))
            } else if (trasformaInPdf && !fileDocumento.isPdf() && !fileDocumento.isP7m()) {
                // se devo scaricare il pdf, eseguo la conversione solo se il file non è già PDF o P7M.
                // faccio il controllo sulle estensioni piuttosto che sul flag "modificabile" perché ad es. a Trezzano
                // usano la firma autografa che imposta il documento come "non modificabile" ma non viene trasformato in pdf (almeno in prima battuta)
                nomeFile = fileDocumento.getNomePdf()
                contentType = GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF)
                inputStream = documentoService.convertiStreamInPdf(inputStream, fileDocumento.nome, fileDocumento.documento)
            }
        } catch (Exception e) {
            log.error("Si è verificato un errore nello sbustare o convertire il file allegato con id ${fileDocumento?.id}. Scarico il file originale.", e)

            // se la trasformazione in pdf dà errore, scarico il file "normale"
            nomeFile = fileDocumento.nome
            contentType = fileDocumento.contentType

            // rileggo il file originale siccome lo stream potrebbe essere stato "consumato" anche solo parzialmente.
            inputStream = gestoreFile.getFile(documento, fileDocumento, true)
        }

        if (response == null) {

            AMedia media = new AMedia(nomeFile, null, contentType, inputStream);
            Filedownload.save(media)
            //Filedownload.save(inputStream, contentType, nomeFile)
        } else {

            response.contentType = contentType
            response.setHeader("Content-disposition", "attachment filename=${nomeFile}")
            IOUtils.copy(inputStream, response.getOutputStream())
            response.outputStream.flush()
        }

        String logAd4 = "Download file ${fileDocumento?.codice}. ${getDettagliLog(fileDocumento)}"
        String annotazione = "id file_documento: ${fileDocumento?.id?.toString()}"
        ad4Service.eventoLogAd4(logAd4, documento?.id?.toString(), annotazione)
    }

    String getDettagliLog(IFileDocumento fileDocumento){

        String estremiProt = ""
        if(fileDocumento?.documento instanceof Allegato){
            if(fileDocumento?.documento?.getDocumentoPrincipale()?.numero){
                estremiProt += "${fileDocumento.documento?.getDocumentoPrincipale()?.numero}/${fileDocumento.documento?.getDocumentoPrincipale()?.anno} (${fileDocumento.documento?.getDocumentoPrincipale()?.tipoRegistro?.commento})"
            }
             return "Protocollo ${estremiProt} oggetto: ${fileDocumento.documento?.getDocumentoPrincipale()?.oggetto}"
        } else{
            if(fileDocumento?.documento?.numero){
                estremiProt += "${fileDocumento.documento?.numero}/${fileDocumento.documento?.anno} (${fileDocumento.documento?.tipoRegistro?.commento})"
            }
            return "Protocollo ${estremiProt} oggetto: ${fileDocumento.documento?.oggetto}"
        }
    }
}
