package it.finmatica.gestionedocumenti.zk

import it.finmatica.gorm.criteria.PagedResultList
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4RuoloDTO
import org.zkoss.zk.ui.event.EventListener

@CompileStatic
class BandboxRuoliAd4 extends KeyboardSelectableBandbox<Ad4RuoloDTO> implements EventListener {

    BandboxRuoliAd4() {
        super("/commons/bandboxRuoliAd4.zul")
    }

    @Override
    protected String getItemToString(Ad4RuoloDTO ad4RuoloDTO) {
        return ad4RuoloDTO.ruolo
    }

    @CompileDynamic
    @Override
    protected PagedList<Ad4RuoloDTO> doSearch(String filtroRuoli, int offset, int max) {
        PagedResultList elencoRuoli = Ad4Ruolo.createCriteria().list(max: max, offset: offset) {
            or {
                ilike("ruolo", "%" + filtroRuoli + "%")
                ilike("descrizione", "%" + filtroRuoli + "%")
            }

            order("descrizione", "asc")
            order("ruolo", "asc")
        }

        return new PagedList<Ad4RuoloDTO>(elencoRuoli.toDTO(), elencoRuoli.totalCount)
    }
}
