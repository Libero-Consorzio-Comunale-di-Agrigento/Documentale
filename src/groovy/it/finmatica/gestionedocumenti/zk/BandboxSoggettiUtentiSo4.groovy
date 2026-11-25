package it.finmatica.gestionedocumenti.zk

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.dto.DTO
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoService
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.select.annotation.WireVariable

@CompileStatic
class BandboxSoggettiUtentiSo4 extends KeyboardSelectableBandbox<Map<String, Object>> implements EventListener {

    @WireVariable
    So4SpringSecurityService springSecurityService
    @WireVariable
    TipologiaSoggettoService tipologiaSoggettoService

    private DTO documento
    private Map<String, Object> soggetti
    private long idTipologiaSoggetto
    private String tipoSoggetto

    BandboxSoggettiUtentiSo4() {
        super("/commons/bandboxUtenti.zul")
    }

    @CompileDynamic
    @Override
    protected String getItemToString(Map<String, Object> soggetto) {
        if (soggetto.utente) {
            return soggetto.descrizione
        } else {
            return ""
        }
    }

    @CompileDynamic
    @Override
    protected PagedList<Map<String, Object>> doSearch(String filtro, int offset, int max) {
        List<So4ComponentePubb> listaComponenti = tipologiaSoggettoService.calcolaListaSoggetti(idTipologiaSoggetto, documento, soggetti, tipoSoggetto, filtro)
        List<So4ComponentePubbDTO> listDTO = listaComponenti.toDTO(["soggetto.utenteAd4"])
        List<Map<String, Object>> lista = listDTO.collect { So4ComponentePubbDTO componente ->
            if (componente.id) {
                [descrizione         : componente.nominativoSoggetto
                 , nominativoSoggetto: componente.nominativoSoggetto
                 , utente            : componente.soggetto?.utenteAd4
                 , modificato        : true
                 , unita             : So4UnitaPubb.getUnita(componente.progrUnita, componente.ottica?.codice).get()?.toDTO()]
            } else {
                [descrizione         : componente.nominativoSoggetto
                 , nominativoSoggetto: componente.nominativoSoggetto]
            }
        }.sort{it -> it.nominativoSoggetto}
        //Prima ordino sul nominativoSoggetto poi aggiugno in testa sia in lista  che in listaDTO
        listDTO.add(0, new So4ComponentePubbDTO(nominativoSoggetto: "Nessun Componente selezionato"))
        lista.add(0,  [nominativoSoggetto: "Nessun Componente selezionato"])
        return new PagedList<Map<String, Object>>(lista, listDTO.size())
    }

    void setDocumento(DTO documento) {
        this.documento = documento
    }

    void setSoggetti(Map<String, Object> soggetti) {
        this.soggetti = soggetti
    }

    void setIdTipologiaSoggetto(long idTipologiaSoggetto) {
        this.idTipologiaSoggetto = idTipologiaSoggetto
    }

    void setTipoSoggetto(String tipoSoggetto) {
        this.tipoSoggetto = tipoSoggetto
    }
}
