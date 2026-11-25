package it.finmatica.gestionedocumenti.soggetti;

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.documenti.DocumentoDTO;
import it.finmatica.gestionedocumenti.soggetti.DocumentoSoggetto;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO;

public class DocumentoSoggettoDTO implements it.finmatica.dto.DTO<DocumentoSoggetto> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean attivo;
    DocumentoDTO documento;
    int sequenza;
    String tipoSoggetto;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;


    public DocumentoSoggetto getDomainObject () {
        return DocumentoSoggetto.get(this.id)
    }

    public DocumentoSoggetto copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
