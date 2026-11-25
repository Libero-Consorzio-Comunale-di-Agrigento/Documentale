package it.finmatica.gestionedocumenti.zk

import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gorm.criteria.PagedResultList
import it.finmatica.so4.login.So4SpringSecurityService
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import it.finmatica.dto.DTO
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoService
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.select.annotation.WireVariable

@CompileStatic
class BandboxSoggettiUnitaSo4 extends KeyboardSelectableBandbox<Map<String, Object>> implements EventListener {

    @WireVariable
    So4SpringSecurityService springSecurityService
    @WireVariable
    TipologiaSoggettoService tipologiaSoggettoService

    private DTO documento
    private Map<String, Object> soggetti
    private long idTipologiaSoggetto
    private String tipoSoggetto

    BandboxSoggettiUnitaSo4() {
        super("/commons/bandboxUnitaSo4.zul")
    }

    @CompileDynamic
    @Override
    protected String getItemToString(Map<String, Object> soggetto) {
        //Se ho abilitato l'impostazione UNITA_CONCAT_CODICE allora la descrizione è data dalla concatenazione codice + descrizione
        //Altrimenti ritorna la sola descrizione
        if(Impostazioni.UNITA_CONCAT_CODICE.abilitato) {
            String descrizioneUO = ""
            if( null != soggetto.unita?.codice) {
                descrizioneUO = descrizioneUO.concat(soggetto.unita.codice).concat(" - ")
            }
            if( null != soggetto.unita?.descrizione) {
                descrizioneUO = descrizioneUO.concat(soggetto.unita.descrizione)
            }
            return descrizioneUO
        } else {
            return soggetto.unita?.descrizione ?: ''
        }
    }

    @CompileDynamic
    @Override
    protected PagedList<Map<String, Object>> doSearch(String filtro, int offset, int max) {
        PagedResultList<So4UnitaPubb> listaUnita = tipologiaSoggettoService.calcolaListaSoggetti(idTipologiaSoggetto, documento, soggetti, tipoSoggetto, filtro ?: '',offset,max)
        List<So4UnitaPubbDTO> listDTO = listaUnita.toDTO()
        List<Map<String, Object>> lista = listDTO.collect { So4UnitaPubbDTO unita ->
            if (unita.codice) {
                [descrizione : unita.descrizione
                 , codice    : unita.codice
                 , unita     : unita
                 , modificato: true]

            } else {
                [descrizione: unita.descrizione,modificato: true]
            }
        }.sort {it.descrizione}
        lista.add(0,[descrizione: "Nessuna Unità selezionata",modificato: true])
        return new PagedList<Map<String, Object>>(lista, listaUnita.totalCount)
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
