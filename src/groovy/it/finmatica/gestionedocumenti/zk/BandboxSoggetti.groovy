package it.finmatica.gestionedocumenti.zk

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.As4SoggettoCorrenteDTO
import org.zkoss.zk.ui.event.EventListener

@CompileStatic
class BandboxSoggetti extends KeyboardSelectableBandbox<As4SoggettoCorrenteDTO> implements EventListener {

    BandboxSoggetti() {
        super("/commons/bandboxSoggetti.zul")
    }

    @Override
    protected String getItemToString(As4SoggettoCorrenteDTO as4SoggettoCorrenteDTO) {
        return as4SoggettoCorrenteDTO.denominazione
    }

    @CompileDynamic
    @Override
    protected PagedList<As4SoggettoCorrenteDTO> doSearch(String filtro, int offset, int max) {
        filtro = filtro.replace(" ", "%")
        def elenco = As4SoggettoCorrente.findAllByDenominazioneIlike("%${filtro}%", [sort: "denominazione", order: "asc", offset: offset, max: max])
        int count = As4SoggettoCorrente.countByDenominazioneIlike("%${filtro}%")
        return new PagedList<As4SoggettoCorrenteDTO>(elenco.toDTO(), count)
    }
}
