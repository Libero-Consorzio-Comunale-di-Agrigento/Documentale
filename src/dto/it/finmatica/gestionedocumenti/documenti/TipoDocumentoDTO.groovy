package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.commons.EnteDTO
import it.finmatica.gestionedocumenti.documenti.TipoDocumento
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoDTO
import it.finmatica.gestionetesti.reporter.GestioneTestiModelloDTO

import java.util.Date
import java.util.Set

class TipoDocumentoDTO implements it.finmatica.dto.DTO<TipoDocumento> {
    private static final long serialVersionUID = 1L

    Long id
    Long version
    String codice
    String commento
    String acronimo
    boolean conservazioneSostitutiva
    Date dateCreated
    String descrizione
    EnteDTO ente
    Date lastUpdated
    Set<TipoDocumentoModelloDTO> modelliAssociati
    Long progressivoCfgIter
    boolean testoObbligatorio
    TipologiaSoggettoDTO tipologiaSoggetto
    Ad4UtenteDTO utenteIns
    Ad4UtenteDTO utenteUpd
    boolean valido

    void addToModelliAssociati (TipoDocumentoModelloDTO tipoDocumentoModello) {
        if (this.modelliAssociati == null)
            this.modelliAssociati = new HashSet<TipoDocumentoModelloDTO>()
        this.modelliAssociati.add (tipoDocumentoModello)
        tipoDocumentoModello.tipoDocumento = this
    }

    void removeFromModelliAssociati (TipoDocumentoModelloDTO tipoDocumentoModello) {
        if (this.modelliAssociati == null)
            this.modelliAssociati = new HashSet<TipoDocumentoModelloDTO>()
        this.modelliAssociati.remove (tipoDocumentoModello)
        tipoDocumentoModello.tipoDocumento = null
    }

    TipoDocumento getDomainObject () {
        return TipoDocumento.get(this.id)
    }

    TipoDocumento copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

	/* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
	// qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
