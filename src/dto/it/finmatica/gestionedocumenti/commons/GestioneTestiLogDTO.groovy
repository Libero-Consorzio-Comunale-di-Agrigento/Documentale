package it.finmatica.gestionedocumenti.commons

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.documenti.DocumentoDTO

import javax.persistence.Column

public class GestioneTestiLogDTO implements it.finmatica.dto.DTO<GestioneTestiLog> {
    private static final long serialVersionUID = 1L;

    Long id;
    DocumentoDTO documento;
    String stato;
    String operazione;
    String nomeFile;
    String errore;
    String estremiDocumento;

    Date dateCreated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;

    public GestioneTestiLog getDomainObject () {
        return GestioneTestiLog.get(this.id)
    }

    public GestioneTestiLog copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
