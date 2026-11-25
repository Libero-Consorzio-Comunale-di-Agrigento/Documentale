package it.finmatica.gestionedocumenti.dizionari

import it.finmatica.afc.PaginatedViewModel
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.utils.ExportService
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gorm.criteria.PagedResultList
import it.finmatica.gestionedocumenti.documenti.TipoCollegamentoDTO
import it.finmatica.gestionedocumenti.documenti.TipoCollegamentoService

import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class TipoCollegamentoListaViewModel extends PaginatedViewModel {

    // services
    @WireVariable
    private TipoCollegamentoService tipoCollegamentoService
    @WireVariable
    private ExportService exportService

    // dati
    List<TipoCollegamentoDTO> listaTipoCollegamento

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w

        exportExcel = true

        caricaListaTipoCollegamento()
    }

    @NotifyChange(["listaTipoCollegamento", "totalSize"])
    private void caricaListaTipoCollegamento(String filterCondition = filtro) {
        PagedResultList lista = tipoCollegamentoService.list(pageSize, activePage, filterCondition, visualizzaTutti)
        totalSize = lista.totalCount
        listaTipoCollegamento = lista.toDTO()
    }

    @Command
    void onPagina() {
        caricaListaTipoCollegamento()
        BindUtils.postNotifyChange(null, null, this, "listaTipoCollegamento")
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "activePage")
    }

    @Command
    void onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
        Long idTipoCollegamento = isNuovoRecord ? null : selectedRecord.id
        Window w = Executions.createComponents("/dizionari/gestionedocumenti/tipoCollegamentoDettaglio.zul", self, [id: idTipoCollegamento])
        w.onClose {
            activePage = 0
            caricaListaTipoCollegamento()
            BindUtils.postNotifyChange(null, null, this, "listaTipoCollegamento")
            BindUtils.postNotifyChange(null, null, this, "totalSize")
            BindUtils.postNotifyChange(null, null, this, "activePage")
        }
        w.doModal()
    }

    @NotifyChange(["listaTipoCollegamento", "totalSize", "selectedRecord", "activePage"])
    @Command
    void onRefresh() {
        activePage = 0
        caricaListaTipoCollegamento()
        selectedRecord = null
    }

    //@NotifyChange(["listaTipoCollegamento", "totalSize", "selectedRecord"])
    @Command
    void onElimina() {
        if (this.selectedRecord.sistema) {
            Clients.showNotification("Non è possibile eliminare i tipi di collegamento preinstallati", Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", 5000, true)
            return
        } else {
            Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
                    new org.zkoss.zk.ui.event.EventListener() {
                        public void onEvent(Event e) {
                            if (Messagebox.ON_OK.equals(e.getName())) {
                                //se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
                                if (listaTipoCollegamento.size() == 1) {
                                    TipoCollegamentoListaViewModel.this.activePage = TipoCollegamentoListaViewModel.this.activePage == 0 ? 0 : TipoCollegamentoListaViewModel.this.activePage - 1
                                }
                                tipoCollegamentoService.elimina(TipoCollegamentoListaViewModel.this.selectedRecord)
                                TipoCollegamentoListaViewModel.this.selectedRecord = null
                                TipoCollegamentoListaViewModel.this.caricaListaTipoCollegamento()
                                BindUtils.postNotifyChange(null, null, TipoCollegamentoListaViewModel.this, "activePage")
                                BindUtils.postNotifyChange(null, null, TipoCollegamentoListaViewModel.this, "listaTipoCollegamento")
                                BindUtils.postNotifyChange(null, null, TipoCollegamentoListaViewModel.this, "totalSize")
                                BindUtils.postNotifyChange(null, null, TipoCollegamentoListaViewModel.this, "selectedRecord")
                            } else if (Messagebox.ON_CANCEL.equals(e.getName())) {
                                //Cancel is clicked
                            }
                        }
                    }
            )
        }
    }

    @NotifyChange(["listaTipoCollegamento", "totalSize", "activePage", "visualizzaTutti"])
    @Command
    void onVisualizzaTutti() {
        activePage = 0
        visualizzaTutti = !visualizzaTutti
        caricaListaTipoCollegamento()
    }

    @NotifyChange(["listaTipoCollegamento", "totalSize", "activePage"])
    @Command
    void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
        if (event instanceof InputEvent) {
            caricaListaTipoCollegamento(event.value)
        } else {
            caricaListaTipoCollegamento()
        }
    }

    @NotifyChange(["listaTipoCollegamento", "totalSize", "filtro", "activePage"])
    @Command
    void onCancelFiltro() {
        activePage = 0
        filtro = ""
        caricaListaTipoCollegamento()
    }

    @Command
    void onExportExcel() {
        if (totalSize > Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt) {
            Messagebox.show("Attenzione: il numero dei documenti da esportare supera il massimo consentito.", "Esportazione interrotta.", Messagebox.OK, Messagebox.EXCLAMATION, null)
            return
        }

        PagedResultList listaCompleta = tipoCollegamentoService.list(Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt, activePage, filtro, true)

        LinkedHashMap<String, LinkedHashMap<String, Serializable>> exportOptions = getExportOptions()
        List<LinkedHashMap<String, Object>> export = getExportObject(listaCompleta.toDTO())

        exportService.downloadExcel(exportOptions, export)
    }

    /**
     * Metodo che mappa le property dell'oggetto da esportare
     * @param listaTipoCollegamento
     * @return
     */
    private List<LinkedHashMap<String, Object>> getExportObject(List<TipoCollegamentoDTO> listaTipoCollegamento) {

        List<LinkedHashMap<String, Object>> export = listaTipoCollegamento.collect {
            [
                    codice       : it.codice
                    , descrizione: it.descrizione
                    , commento   : it.commento
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
                codice       : [esportabile: true, label: 'Tipo di Collegamento', index: 0, columnType: 'TEXT']
                , descrizione: [esportabile: true, label: 'Descrizione', index: 1, columnType: 'TEXT']
                , commento   : [esportabile: true, label: 'Commento', index: 2, columnType: 'TEXT']
        ]
        exportOptions
    }
}
