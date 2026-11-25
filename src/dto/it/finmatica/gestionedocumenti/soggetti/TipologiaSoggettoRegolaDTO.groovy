package it.finmatica.gestionedocumenti.soggetti

import it.finmatica.ad4.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.RegolaCalcolo
import it.finmatica.gestionedocumenti.commons.EnteDTO

public class TipologiaSoggettoRegolaDTO implements it.finmatica.dto.DTO<TipologiaSoggettoRegola> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    EnteDTO ente;
    Date lastUpdated;
    RegolaCalcolo regolaDefault;
    RegolaCalcolo regolaLista;
    Ad4RuoloDTO ruolo;
    int sequenza;
    String tipoSoggetto;
    String tipoSoggettoPartenza;
    TipologiaSoggettoDTO tipologiaSoggetto;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;


    public TipologiaSoggettoRegola getDomainObject () {
        return TipologiaSoggettoRegola.get(this.id)
    }

    public TipologiaSoggettoRegola copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
