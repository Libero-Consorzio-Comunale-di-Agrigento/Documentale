package it.finmatica.gestionedocumenti.zk

import it.finmatica.gorm.criteria.PagedResultList
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO
import org.zkoss.zk.ui.event.EventListener

@CompileStatic
class BandboxUnitaSo4 extends KeyboardSelectableBandbox<So4UnitaPubbDTO> implements EventListener {

    BandboxUnitaSo4() {
        super("/commons/bandboxUnitaSo4.zul")
    }

    @Override
    protected String getItemToString(So4UnitaPubbDTO so4UnitaPubbDTO) {
        return so4UnitaPubbDTO.descrizione
    }

    @CompileDynamic
    @Override
    protected PagedList<So4UnitaPubbDTO> doSearch(String filtro, int offset, int max) {
        Date data = new Date();
        PagedResultList elencoUnita = So4UnitaPubb.createCriteria().list(max: max, offset: offset) {
            or {
                ilike('descrizione', '%' + filtro + '%')
                ilike('codice', '%' + filtro + '%')
            }

            le('dal', data)
            or {
                isNull('al')
                ge('al', data)
            }

            order('descrizione', 'asc')
            order('codice', 'asc')
        }

        return new PagedList<So4UnitaPubbDTO>(elencoUnita.toDTO(), elencoUnita.totalCount)
    }
}
