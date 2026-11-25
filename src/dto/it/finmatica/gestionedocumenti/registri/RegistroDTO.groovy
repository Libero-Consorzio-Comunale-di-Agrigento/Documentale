package it.finmatica.gestionedocumenti.registri;

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.commons.EnteDTO;
import it.finmatica.gestionedocumenti.registri.Registro;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO;
import java.util.Date;

public class RegistroDTO implements it.finmatica.dto.DTO<Registro> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    int anno;
    boolean aperto;
    Date dataUltimoNumero;
    Date dateCreated;
    EnteDTO ente;
    Date lastUpdated;
    TipoRegistroDTO tipoRegistro;
    int ultimoNumero;
    So4UnitaPubbDTO unitaEmergenza;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;


    public Registro getDomainObject () {
        return Registro.get(this.id)
    }

    public Registro copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
