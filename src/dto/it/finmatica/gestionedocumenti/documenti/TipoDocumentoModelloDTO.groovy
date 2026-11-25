package it.finmatica.gestionedocumenti.documenti;

import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.documenti.TipoDocumentoModello;
import it.finmatica.gestionetesti.reporter.GestioneTestiModelloDTO;

public class TipoDocumentoModelloDTO implements it.finmatica.dto.DTO<TipoDocumentoModello> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String codice;
    GestioneTestiModelloDTO modelloTesto;
    boolean predefinito;
    TipoDocumentoDTO tipoDocumento;


    public TipoDocumentoModello getDomainObject () {
        return TipoDocumentoModello.get(this.id)
    }

    public TipoDocumentoModello copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

	/* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
	// qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
