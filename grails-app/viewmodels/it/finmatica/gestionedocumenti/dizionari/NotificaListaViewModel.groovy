package it.finmatica.gestionedocumenti.dizionari

import it.finmatica.afc.PaginatedViewModel
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.notifiche.NotificaDTO
import it.finmatica.gestionedocumenti.notifiche.NotificaDTOService
import it.finmatica.gestionedocumenti.utils.ExportService
import it.finmatica.gorm.criteria.PagedResultList
import org.hibernate.SessionFactory
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class NotificaListaViewModel extends PaginatedViewModel {

	// service
	@WireVariable
	private NotificaDTOService notificaDTOService

	@WireVariable
	SessionFactory sessionFactory
	@WireVariable
	private ExportService exportService

	// dati
	List<NotificaDTO> listaNotifica

	boolean exportExcel

	@Init
	void init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w

		exportExcel = true
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize"])
	private void caricaListaNotifica() {
		PagedResultList lista = notificaDTOService.list(pageSize, activePage, visualizzaTutti, filtro)
		totalSize = lista.totalCount
		listaNotifica = lista
	}

	@Command
	void onPagina() {
		caricaListaNotifica()
		BindUtils.postNotifyChange(null, null, this, "listaNotifica")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	@Command
	void onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents("/dizionari/gestionedocumenti/notificaDettaglio.zul", self, [id: (isNuovoRecord ? null : selectedRecord.id)])
		w.onClose {
			caricaListaNotifica()
			BindUtils.postNotifyChange(null, null, this, "listaNotifica")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command
	void onRefresh() {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord"])
	@Command
	void onElimina() {
		notificaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaNotifica()
	}

	@NotifyChange(["visualizzaTutti", "listaNotifica", "totalSize", "selectedRecord", "activePage"])
	@Command
	void onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord", "activePage"])
	@Command
	void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command
	void onCancelFiltro() {
		onRefresh()
	}

	@Command
	void onExportExcel() {
		if (totalSize > Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt) {
			Messagebox.show("Attenzione: il numero dei documenti da esportare supera il massimo consentito.", "Esportazione interrotta.", Messagebox.OK, Messagebox.EXCLAMATION, null)
			return
		}

		PagedResultList listaCompleta = notificaDTOService.list(Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt, activePage, true, filtro)
		List<NotificaDTO> listaNotifica = listaCompleta

		LinkedHashMap<String, LinkedHashMap<String, Serializable>> exportOptions = getExportOptions()
		List<LinkedHashMap<String, Object>> export = getExportObject(listaNotifica)

		exportService.downloadExcel(exportOptions, export)
	}

	/**
	 * Metodo che mappa le property dell'oggetto da esportare
	 * @param listaNotifica
	 * @return
	 */
	private List<LinkedHashMap<String, Object>> getExportObject(List<NotificaDTO> listaNotifica) {

		List<LinkedHashMap<String, Object>> export = listaNotifica.collect {
			[
					tipoNotifica           : it.tipoNotifica
					, titolo               : it.titolo
					, modalitaInvio        : it.modalitaInvio
					, tipoNotificaScrivania: it.tipoNotificaScrivania
					, oggetti              : it.oggetti
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
				tipoNotifica           : [esportabile: true, label: 'Tipologia', index: 0, columnType: 'TEXT']
				, titolo               : [esportabile: true, label: 'Titolo', index: 1, columnType: 'TEXT']
				, modalitaInvio        : [esportabile: true, label: 'Modalità', index: 2, columnType: 'TEXT']
				, tipoNotificaScrivania: [esportabile: true, label: 'Tipologia Scrivania', index: 3, columnType: 'TEXT']
				, oggetti              : [esportabile: true, label: 'Valida per i documenti', index: 4, columnType: 'TEXT']
		]
		exportOptions
	}
}
