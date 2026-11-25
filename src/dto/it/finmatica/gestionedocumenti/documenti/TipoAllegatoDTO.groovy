package it.finmatica.gestionedocumenti.documenti;

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.commons.EnteDTO;
import it.finmatica.gestionedocumenti.documenti.TipoAllegato;
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoDTO;
import java.util.Date;
import java.util.Set;

public class TipoAllegatoDTO extends TipoDocumentoDTO implements it.finmatica.dto.DTO<TipoAllegato> {
    private static final long serialVersionUID = 1L;

    boolean stampaUnica;

    public TipoAllegato getDomainObject () {
        return TipoAllegato.get(this.id)
    }

    public TipoAllegato copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

	/* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
	// qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
