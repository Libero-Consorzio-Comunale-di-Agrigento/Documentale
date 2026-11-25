package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.commons.EnteDTO
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.soggetti.DocumentoSoggettoDTO
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.gestioneiter.motore.WkfIterDTO

class DocumentoDTO implements it.finmatica.dto.DTO<Documento>, IDocumentoEsterno {
    private static final long serialVersionUID = 1L

    Long id
    Long version
    Date dateCreated
    Set<DocumentoCollegatoDTO> documentiCollegati
    EnteDTO ente
    List<FileDocumentoDTO> fileDocumenti
    Long idDocumentoEsterno
    WkfIterDTO iter
    Date lastUpdated
    boolean riservato
    Set<DocumentoSoggettoDTO> soggetti
    StatoDocumento stato
    StatoConservazione statoConservazione
    Integer anniConservazione
    StatoFirma statoFirma
    WkfTipoOggettoDTO tipoOggetto
    Ad4UtenteDTO utenteIns
    Ad4UtenteDTO utenteUpd
    boolean valido
    String campiProtetti


    void addToDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null)
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        this.documentiCollegati.add (documentoCollegato)
        documentoCollegato.documento = this
    }

    void removeFromDocumentiCollegati (DocumentoCollegatoDTO documentoCollegato) {
        if (this.documentiCollegati == null)
            this.documentiCollegati = new HashSet<DocumentoCollegatoDTO>()
        this.documentiCollegati.remove (documentoCollegato)
        documentoCollegato.documento = null
    }

    void addToFileDocumenti(FileDocumentoDTO value) {
        if (this.fileDocumenti == null) {
            this.fileDocumenti = new ArrayList<FileDocumento>()
        }

        FileDocumentoDTO file = null
        int index = 0
        for (FileDocumentoDTO f : this.fileDocumenti) {
            if (value.id != null && f.id == value.id) {
                file = value
                break
            }
            index = index + 1
        }
        if (file == null) {
            this.fileDocumenti.add(value)
        } else {
            this.fileDocumenti.set(index, value)
        }
        value.documento = this
    }

    void removeFromFileDocumenti (FileDocumentoDTO fileDocumento) {
        if (this.fileDocumenti == null)
            this.fileDocumenti = new ArrayList<FileDocumentoDTO>()
        this.fileDocumenti.remove (fileDocumento)
        fileDocumento.documento = null
    }

    void addToSoggetti (DocumentoSoggettoDTO documentoSoggetto) {
        if (this.soggetti == null)
            this.soggetti = new HashSet<DocumentoSoggettoDTO>()
        this.soggetti.add (documentoSoggetto)
        documentoSoggetto.documento = this
    }

    List<FileDocumentoDTO> getFileDocumentiValidi(){

        ArrayList<FileDocumentoDTO> soloValidi = new ArrayList<FileDocumento>()
        for(FileDocumentoDTO f : fileDocumenti){
            if(f.valido){
                soloValidi.add(f)
            }
        }

        return soloValidi
    }

    void removeFromSoggetti (DocumentoSoggettoDTO documentoSoggetto) {
        if (this.soggetti == null)
            this.soggetti = new HashSet<DocumentoSoggettoDTO>()
        this.soggetti.remove (documentoSoggetto)
        documentoSoggetto.documento = null
    }

    Documento getDomainObject () {
        return Documento.get(this.id)
    }

    Documento copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    String getApplicativo() {
        return null
    }

    String getEstremi() {
        return null
    }
}
