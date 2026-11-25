package it.finmatica.gestionedocumenti.zk

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import org.zkoss.zk.ui.event.EventListener

@CompileStatic
class BandboxUtenti extends KeyboardSelectableBandbox<Ad4UtenteDTO> implements EventListener {

    BandboxUtenti() {
        super("/commons/bandboxUtenti.zul")
    }

    @Override
    protected String getItemToString(Ad4UtenteDTO ad4UtenteDTO) {
        return ad4UtenteDTO.nominativoSoggetto
    }

    @CompileDynamic
    @Override
    protected PagedList<Ad4UtenteDTO> doSearch(String filtro, int offset, int max) {
        filtro = filtro.replace(" ", "%")
        def elenco = Ad4Utente.findAllByNominativoSoggettoIlike("%${filtro}%", [sort: "nominativoSoggetto", order: "asc", offset: offset, max: max])
        int count = Ad4Utente.countByNominativoSoggettoIlike("%${filtro}%")
        return new PagedList<Ad4UtenteDTO>(elenco.toDTO(), count)
    }
}
