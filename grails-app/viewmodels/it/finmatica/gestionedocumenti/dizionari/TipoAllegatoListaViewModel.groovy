package it.finmatica.gestionedocumenti.dizionari

import it.finmatica.afc.PaginatedViewModel
import it.finmatica.gestionedocumenti.commons.AbstractDomain
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.utils.ExportService
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gorm.criteria.PagedResultList
import it.finmatica.gestionedocumenti.documenti.TipoAllegato
import it.finmatica.gestionedocumenti.documenti.TipoAllegatoDTO
import it.finmatica.gestionedocumenti.documenti.TipoAllegatoDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class TipoAllegatoListaViewModel extends PaginatedViewModel {

    // services
    @WireVariable
    private TipoAllegatoDTOService tipoAllegatoDTOService
    @WireVariable
    private ExportService exportService
    @WireVariable
    private SessionFactory sessionFactory

    // dati
    List<TipoAllegatoDTO> listaTipoAllegato

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w

        exportExcel = true
        caricaListaTipoAllegato()
    }

    @NotifyChange(["listaTipoAllegato", "totalSize"])
    private void caricaListaTipoAllegato(String filterCondition = filtro) {
        Session session = sessionFactory.getCurrentSession()
        session.disableFilter(AbstractDomain.SOLO_VALIDI_FILTER)
        try {
            PagedResultList lista = TipoAllegato.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
                if (!visualizzaTutti) eq("valido", true)
                if (filterCondition ?: "" != "") ilike("codice", "%${filterCondition}%")
                order("acronimo", "asc")
                order("descrizione", "asc")
            }
            totalSize = lista.totalCount
            listaTipoAllegato = lista.toDTO()
        } finally {
            session.enableFilter(AbstractDomain.SOLO_VALIDI_FILTER)
        }
    }

    @Command
    void onPagina() {
        caricaListaTipoAllegato()
        BindUtils.postNotifyChange(null, null, this, "listaTipoAllegato")
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "activePage")
    }


    @Command
    void onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
        Long idTipoAllegato = isNuovoRecord ? null : selectedRecord.id
        Window w = Executions.createComponents("/dizionari/gestionedocumenti/tipoAllegatoDettaglio.zul", self, [id: idTipoAllegato])
        w.onClose {
            activePage = 0
            caricaListaTipoAllegato()
            BindUtils.postNotifyChange(null, null, this, "listaTipoAllegato")
            BindUtils.postNotifyChange(null, null, this, "totalSize")
            BindUtils.postNotifyChange(null, null, this, "activePage")
        }
        w.doModal()
    }

    @NotifyChange(["listaTipoAllegato", "totalSize", "selectedRecord", "activePage"])
    @Command
    void onRefresh() {
        activePage = 0
        caricaListaTipoAllegato()
        selectedRecord = null
    }

    //@NotifyChange(["listaTipoAllegato", "totalSize", "selectedRecord"])
    @Command
    void onElimina() {
        Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
                Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            //se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
                            if (listaTipoAllegato.size() == 1) {
                                TipoAllegatoListaViewModel.this.activePage = TipoAllegatoListaViewModel.this.activePage == 0 ? 0 : TipoAllegatoListaViewModel.this.activePage - 1
                            }
                            tipoAllegatoDTOService.elimina(TipoAllegatoListaViewModel.this.selectedRecord)
                            TipoAllegatoListaViewModel.this.selectedRecord = null
                            TipoAllegatoListaViewModel.this.caricaListaTipoAllegato()
                            BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "activePage")
                            BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "listaTipoAllegato")
                            BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "totalSize")
                            BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "selectedRecord")
                        } else if (Messagebox.ON_CANCEL.equals(e.getName())) {
                            //Cancel is clicked
                        }
                    }
                }
        )

    }

    @NotifyChange(["listaTipoAllegato", "totalSize", "activePage", "visualizzaTutti"])
    @Command
    void onVisualizzaTutti() {
        activePage = 0
        visualizzaTutti = !visualizzaTutti
        caricaListaTipoAllegato()
    }

    @NotifyChange(["listaTipoAllegato", "totalSize", "activePage"])
    @Command
    void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
        if (event instanceof InputEvent) {
            caricaListaTipoAllegato(event.value)
        } else {
            caricaListaTipoAllegato()
        }
    }

    @NotifyChange(["listaTipoAllegato", "totalSize", "filtro", "activePage"])
    @Command
    void onCancelFiltro() {
        activePage = 0
        filtro = ""
        caricaListaTipoAllegato()
    }

    private List<TipoAllegatoDTO> listaCompleta() {
        PagedResultList lista = TipoAllegato.createCriteria().list(max: Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt, offset: pageSize * activePage) {
            if (filtro ?: "" != "") ilike("codice", "%${filtro}%")
            order("acronimo", "asc")
            order("descrizione", "asc")
        }
        listaTipoAllegato = lista.toDTO()
    }

    @Command
    void onExportExcel() {
        if (totalSize > Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt) {
            Messagebox.show("Attenzione: il numero dei documenti da esportare supera il massimo consentito.", "Esportazione interrotta.", Messagebox.OK, Messagebox.EXCLAMATION, null)
            return
        }

        LinkedHashMap<String, LinkedHashMap<String, Serializable>> exportOptions = getExportOptions()
        List<LinkedHashMap<String, Object>> export = getExportObject(listaCompleta())

        exportService.downloadExcel(exportOptions, export)
    }

    /**
     * Metodo che mappa le property dell'oggetto da esportare
     * @param listaTipoAllegato
     * @return
     */
    private List<LinkedHashMap<String, Object>> getExportObject(List<TipoAllegatoDTO> listaTipoAllegato) {

        List<LinkedHashMap<String, Object>> export = listaTipoAllegato.collect {
            [
                    acronimo     : it.acronimo
                    , descrizione: it.descrizione
                    , commento   : it.commento
                    , valido     : it.valido
            ]
        }
        export
    }

    /**
     * Metodo che definisce, in una mappa, le proprietà di esportazione in excel
     * @return
     */
    private LinkedHashMap<String, LinkedHashMap<String, Serializable>> getExportOptions() {

        LinkedHashMap<String, LinkedHashMap<String, Serializable>> exportOptions = [
                acronimo     : [esportabile: true, label: 'Acronimo', index: 0, columnType: 'TEXT']
                , descrizione: [esportabile: true, label: 'Descrizione', index: 1, columnType: 'TEXT']
                , commento   : [esportabile: true, label: 'Commento', index: 2, columnType: 'TEXT']
                , valido     : [esportabile: true, label: 'Valido', index: 3, columnType: 'BOOLEAN']
        ]
        exportOptions
    }
}
