package it.finmatica.gestionedocumenti.documenti;

import it.finmatica.ad4.autenticazione.Ad4RuoloDTO;
import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.documenti.TipoDocumentoCompetenza;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO;

public class TipoDocumentoCompetenzaDTO implements it.finmatica.dto.DTO<TipoDocumentoCompetenza> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean cancellazione;
    String descrizione;
    boolean lettura;
    boolean modifica;
    Ad4RuoloDTO ruoloAd4;
    TipoDocumentoDTO tipoDocumento;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;


    public TipoDocumentoCompetenza getDomainObject () {
        return TipoDocumentoCompetenza.get(this.id)
    }

    public TipoDocumentoCompetenza copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
