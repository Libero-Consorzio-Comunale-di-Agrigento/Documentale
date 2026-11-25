package it.finmatica.gestionedocumenti.documenti;

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.documenti.FileDocumentoFirmatario;
import java.util.Date;

public class FileDocumentoFirmatarioDTO implements it.finmatica.dto.DTO<FileDocumentoFirmatario> {
    private static final long serialVersionUID = 1L;

    Long                id;
    Long                version;
    Date                dataFirma;
    String              stato;
    Date                dateCreated;
    FileDocumentoDTO    fileDocumento;
    Date                lastUpdated;
    int                 sequenza;
    String 		        nominativo;
    Date 		        dataVerifica;
    Ad4UtenteDTO        utenteAd4;
    Ad4UtenteDTO        utenteIns;
    Ad4UtenteDTO        utenteUpd;
    boolean             valido;


    public FileDocumentoFirmatario getDomainObject () {
        return FileDocumentoFirmatario.get(this.id)
    }

    public FileDocumentoFirmatario copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
