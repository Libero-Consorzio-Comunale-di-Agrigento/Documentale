package it.finmatica.gestionedocumenti.competenze

import it.finmatica.ad4.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.commons.EnteDTO
import it.finmatica.gestionedocumenti.documenti.DocumentoDTO
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgCompetenzaDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO

class DocumentoCompetenzeDTO implements it.finmatica.dto.DTO<DocumentoCompetenze> {
    private static final long serialVersionUID = 1L

    Long id
    Long version
    boolean cancellazione
    WkfCfgCompetenzaDTO cfgCompetenza
    boolean lettura
    boolean modifica
    DocumentoDTO documento
    Ad4RuoloDTO ruoloAd4
    So4UnitaPubbDTO unitaSo4
    Ad4UtenteDTO utenteAd4
    EnteDTO ente


    DocumentoCompetenze getDomainObject () {
        return DocumentoCompetenze.get(this.id)
    }

    DocumentoCompetenze copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
