package it.finmatica.gestionedocumenti.dizionari

import it.finmatica.afc.PaginatedViewModel
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoService
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gorm.criteria.PagedResultList
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoDTO
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoDTOService

import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class TipologiaSoggettoListaViewModel extends PaginatedViewModel {

	// services
	@WireVariable private TipologiaSoggettoDTOService	tipologiaSoggettoDTOService
	@WireVariable private TipologiaSoggettoService tipologiaSoggettoService

	// dati
	List<TipologiaSoggettoDTO> 	listaTipologie

    @Init void init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaTipologie()
    }

	@NotifyChange(["listaTipologie", "totalSize"])
	private void caricaListaTipologie(String filterCondition = filtro) {
		PagedResultList listaP = tipologiaSoggettoService.list(pageSize,activePage,filterCondition,visualizzaTutti)
		totalSize  = listaP.totalCount
		listaTipologie = listaP.toDTO()
	}

	@Command void onPagina() {
		caricaListaTipologie()
		BindUtils.postNotifyChange(null, null, this, "listaTipologie")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	@Command void onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/gestionedocumenti/tipologiaSoggettoDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaTipologie()
			BindUtils.postNotifyChange(null, null, this, "listaTipologie")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipologie", "totalSize", "selectedRecord", "activePage"])
	@Command void onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaTipologie()
	}

	@NotifyChange(["listaTipologie", "totalSize", "selectedRecord"])
	@Command void onElimina () {
		tipologiaSoggettoDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaTipologie()
	}

	@NotifyChange(["visualizzaTutti", "listaTipologie", "totalSize"])
	@Command void onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaTipologie()
	}

	@NotifyChange(["listaTipologie", "totalSize", "selectedRecord", "activePage"])
	@Command void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaTipologie()
	}

	@NotifyChange(["listaTipologie", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command void onCancelFiltro() {
		onRefresh()
	}
}
