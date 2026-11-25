package it.finmatica.gestionedocumenti.documenti

import groovy.util.logging.Slf4j
import it.finmatica.firmadigitale.utils.VerificatoreFirma
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Slf4j
@Transactional
@Service
class FileDocumentoService {

    @Autowired
    FileDocumentoRepository fileDocumentoRepository
    @Autowired
    DocumentoService documentoService

    void aggiornaVerificaFirma(FileDocumento fileDocumento) {
        List<VerificatoreFirma.RisultatoVerifica> risultatiVerifica = documentoService.estraiInformazioneFileFirmato(fileDocumento)

        if(fileDocumento.firmato){
            if(risultatiVerifica == null || risultatiVerifica.size() == 0){
                fileDocumento.esitoVerifica = FileDocumento.ESITO_FALLITO
                fileDocumento.dataVerifica = new Date()
                return
            }
        }

        boolean firmaValida = true
        if (risultatiVerifica?.size() > 0) {
            for (VerificatoreFirma.RisultatoVerifica risultatoVerifica : risultatiVerifica) {
                if (!risultatoVerifica.valida) {
                    firmaValida = false
                    break
                }
            }

            fileDocumento.firmato = true
            fileDocumento.dataVerifica = new Date()
            if (firmaValida) {
                fileDocumento.esitoVerifica = FileDocumento.ESITO_VERIFICATO
            } else {
                fileDocumento.esitoVerifica = FileDocumento.ESITO_FALLITO
            }

            fileDocumento.save()
        }
    }

    void forzaVerificaFirma(FileDocumento fileDocumento) {
        fileDocumento.esitoVerifica = FileDocumento.ESITO_FORZATO

        fileDocumento.save()
    }

    void forzaFirmaNonVerificata(FileDocumento fileDocumento) {
        fileDocumento.esitoVerifica = FileDocumento.ESITO_NON_VERIFICATO
        fileDocumento.dataVerifica = null

        fileDocumento.save()
    }

    FileDocumento getFileOriginale(long idDocumento, long idFilePrincipale){
        fileDocumentoRepository.getFileOriginale(idDocumento, idFilePrincipale)
    }

    int numeroDocumentiDocumentaleAssociati(long idFileEsterno){
        return fileDocumentoRepository.numeroDocumentiDocumentaleAssociati(idFileEsterno)
    }
}
