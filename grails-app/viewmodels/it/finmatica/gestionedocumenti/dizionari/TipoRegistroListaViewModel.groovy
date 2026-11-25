package it.finmatica.gestionedocumenti.dizionari

import it.finmatica.afc.PaginatedViewModel
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.utils.ExportService
import it.finmatica.gorm.criteria.PagedResultList
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zkplus.spring.DelegatingVariableResolver


import it.finmatica.gestionedocumenti.registri.RegistroDTOService
import it.finmatica.gestionedocumenti.registri.TipoRegistro
import it.finmatica.gestionedocumenti.registri.TipoRegistroDTO

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
class TipoRegistroListaViewModel extends PaginatedViewModel {
    // services
    @WireVariable
    private RegistroDTOService registroDTOService
    @WireVariable
    private ExportService exportService

    // dati
    List<TipoRegistroDTO> listaTipoRegistroDto


    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
        this.modificaAttiva = false

        exportExcel = true
        caricaListaTipoRegistro()
    }

    @NotifyChange(["listaTipoRegistroDto", "totalSize"])
    private void caricaListaTipoRegistro(String filterCondition = filtro) {
        PagedResultList lista = TipoRegistro.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            if (!visualizzaTutti) eq("valido", true)
            if (filterCondition ?: "" != "") {
                or {
                    ilike("codice", "%${filterCondition}%")
                }
            }
            order("codice", "asc")
        }
        totalSize = lista.totalCount
        listaTipoRegistroDto = lista.toDTO()
    }

    @NotifyChange(["listaTipoRegistroDto", "totalSize"])
    @Command
    void onPagina() {
        caricaListaTipoRegistro()
        BindUtils.postNotifyChange(null, null, this, "listaTipoRegistroDto")
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "activePage")
    }

    @NotifyChange(["listaTipoRegistroDto", "totalSize", "selectedRecord", "activePage"])
    @Command
    void onRefresh() {
        activePage = 0
        caricaListaTipoRegistro()
        selectedRecord = null
    }

    @Override
    void onElimina() {
        // FIXME per ora nulla, vedi sotto quando verranno implementate le funzioni di modifica
    }

    @Command
    void onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
        if (isNuovoRecord) {
            Clients.showNotification('Inserimento non supportato', Clients.NOTIFICATION_TYPE_ERROR, self, "top_center", 3000, true)
        } else {
            String codice = isNuovoRecord ? null : selectedRecord.codice
            Window w = Executions.createComponents("/dizionari/gestionedocumenti/tipoRegistroDettaglio.zul", self, [codice: codice])
            w.onClose {
                activePage = 0
                caricaListaTipoRegistro()
                BindUtils.postNotifyChange(null, null, this, "listaTipoRegistroDto")
                BindUtils.postNotifyChange(null, null, this, "totalSize")
                BindUtils.postNotifyChange(null, null, this, "activePage")
            }
            w.doModal()
        }
    }

/*
	@Command void onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaTipoRegistroDto.size() == 1){
							TipoRegistroListaViewModel.this.activePage= TipoRegistroListaViewModel.this.activePage==0?0:TipoRegistroListaViewModel.this.activePage-1
						}
						registroDTOService.elimina(TipoRegistroListaViewModel.this.selectedRecord)
						TipoRegistroListaViewModel.this.selectedRecord = null
						TipoRegistroListaViewModel.this.caricaListaTipoRegistro()
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "listaTipoRegistroDto")
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}
	*/

    @NotifyChange(["listaTipoRegistroDto", "totalSize", "activePage", "visualizzaTutti"])
    @Command
    void onVisualizzaTutti() {
        activePage = 0
        visualizzaTutti = !visualizzaTutti
        caricaListaTipoRegistro()
    }

    @NotifyChange(["listaTipoRegistroDto", "totalSize", "activePage"])
    @Command
    void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
        if (event instanceof InputEvent) {
            caricaListaTipoRegistro(event.value)
        } else {
            caricaListaTipoRegistro()
        }

    }

    @NotifyChange(["listaTipoRegistroDto", "totalSize", "filtro", "activePage"])
    @Command
    void onCancelFiltro() {
        activePage = 0
        filtro = null
        caricaListaTipoRegistro()
    }

    private List<TipoRegistroDTO> listaCompleta() {
        PagedResultList lista = TipoRegistro.createCriteria().list(max: Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt, offset: pageSize * activePage) {
            if (filtro ?: "" != "") {
                or {
                    ilike("codice", "%${filtro}%")
                }
            }
            order("codice", "asc")
        }
        listaTipoRegistroDto = lista.toDTO()
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
     * @param listaTipoRegistroDto
     * @return
     */
    private List<LinkedHashMap<String, Object>> getExportObject(List<TipoRegistroDTO> listaTipoRegistroDto) {

        List<LinkedHashMap<String, Object>> export = listaTipoRegistroDto.collect {
            [
                    codice              : it.codice
                    , commento          : it.commento
                    , rinnovoAutomatico : it.rinnovoAutomatico
                    , chiusuraAutomatica: it.chiusuraAutomatica
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
                codice              : [esportabile: true, label: 'Codice', index: 0, columnType: 'TEXT']
                , commento          : [esportabile: true, label: 'Descrizione', index: 1, columnType: 'TEXT']
                , rinnovoAutomatico : [esportabile: true, label: 'Rinnovo', index: 2, columnType: 'BOOLEAN']
                , chiusuraAutomatica: [esportabile: true, label: 'Chiusura', index: 3, columnType: 'BOOLEAN']
        ]
        exportOptions
    }
}
