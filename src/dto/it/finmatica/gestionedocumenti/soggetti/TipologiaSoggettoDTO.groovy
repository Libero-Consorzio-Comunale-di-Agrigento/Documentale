package it.finmatica.gestionedocumenti.soggetti;

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO;
import it.finmatica.dto.DtoUtils;
import it.finmatica.gestionedocumenti.commons.EnteDTO;
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggetto;
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggettoDTO;
import java.util.Date;
import java.util.List;

public class TipologiaSoggettoDTO implements it.finmatica.dto.DTO<TipologiaSoggetto> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String commento;
    Date dateCreated;
    String descrizione;
    EnteDTO ente;
    Date lastUpdated;
    String layoutSoggetti;
    List<TipologiaSoggettoRegolaDTO> regole;
    WkfTipoOggettoDTO tipoOggetto;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;

    public void addToRegole (TipologiaSoggettoRegolaDTO tipologiaSoggettoRegola) {
        if (this.regole == null)
            this.regole = new ArrayList<TipologiaSoggettoRegolaDTO>()
        this.regole.add (tipologiaSoggettoRegola);
        tipologiaSoggettoRegola.tipologiaSoggetto = this
    }

    public void removeFromRegole (TipologiaSoggettoRegolaDTO tipologiaSoggettoRegola) {
        if (this.regole == null)
            this.regole = new ArrayList<TipologiaSoggettoRegolaDTO>()
        this.regole.remove (tipologiaSoggettoRegola);
        tipologiaSoggettoRegola.tipologiaSoggetto = null
    }

    public TipologiaSoggetto getDomainObject () {
        return TipologiaSoggetto.get(this.id)
    }

    public TipologiaSoggetto copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
