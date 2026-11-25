package it.finmatica.gestionedocumenti.integrazioni.firma

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.documenti.FileDocumento

@CompileStatic
class FirmaEvent {

    private final FileDocumento fileDocumento
    private final Boolean saltaVerifica

    FirmaEvent(FileDocumento fileDocumento,Boolean saltaVerifica) {
        this.fileDocumento = fileDocumento
        this.saltaVerifica = saltaVerifica
    }
    FirmaEvent(FileDocumento fileDocumento) {
        this.fileDocumento = fileDocumento
        this.saltaVerifica = false
    }

    FileDocumento getFileDocumento() {
        return fileDocumento
    }
    Boolean getSaltaVerifica() {
        return saltaVerifica
    }
}
