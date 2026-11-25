package it.finmatica.gestionedocumenti.dizionari
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.registri.*
import it.finmatica.gestionedocumenti.dizionari.commons.DizionariDettaglioViewModel
import org.apache.commons.lang.StringUtils
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class TipoRegistroDettaglioViewModel extends DizionariDettaglioViewModel {

	// services
	@WireVariable private RegistroDTOService 	registroDTOService

	// dati
	List<RegistroDTO> 	listaRegistroDTO

	// stato
	boolean visualizzaTutti

	@NotifyChange(["selectedRecord", "listaRegistroDTO","creaRegistroVisible"])
	@Init
	void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("codice") String codice) {
		this.self = w


		if (codice == null) {
			selectedRecord = new TipoRegistroDTO(valido:true)
		} else {
			selectedRecord = TipoRegistro.get(codice).toDTO()
			caricaListaRegistro()
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		}
	}

	private void caricaListaRegistro() {
		listaRegistroDTO = Registro.createCriteria().list {
			eq ("tipoRegistro.codice", selectedRecord?.codice)
			if (!visualizzaTutti) {
				eq ("valido", true)
			}
			order("anno",   "desc")
			order("valido", "desc")
		}.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaRegistroDTO")
	}

	@Command
	void onCrea() {
		// funzionalità di modifica disattivate
		/*
		Window w = Executions.createComponents ("/dizionari/gestionedocumenti/registroDettaglio.zul", self, [tipoRegistro: selectedRecord])
		w.onClose {
			caricaListaRegistro()
			BindUtils.postNotifyChange(null, null, this, "listaRegistroDTO")
		}
		w.doModal()

		 */
	}

	@Command
	void onVisualizzaTutti () {
		visualizzaTutti = !visualizzaTutti
		caricaListaRegistro()
		BindUtils.postNotifyChange(null, null, this, "visualizzaTutti")
	}

	/*
	 *  implementazione record di AfcAbstractRecord
	 */

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica","creaRegistroVisible"])
	@Command
	void onSalva () {
		// modifica disattivata
		/*
		Collection<String> messaggiValidazione = validaMaschera()
		if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
			Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
			return
		}

		selectedRecord = registroDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)

		 */
	}

	@Command
	void onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command
	void onSettaValido(@BindingParam("valido") boolean valido) {
		// modifica disattivata
		/*
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						TipoRegistroDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoRegistroDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoRegistroDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoRegistroDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)

		 */
	}

	@Command
	void onEliminaRegistro (@BindingParam("reg") RegistroDTO registroDto) {
		// modifica disabilitata
		/*
		Collection<String> messaggiValidazione = validaMaschera()
		if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
			Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
			return
		}

		if (registroDto.valido) {
			registroDTOService.chiudiRegistro (registroDto)
			caricaListaRegistro ()
		} else {
			registroDTOService.riapriRegistro (registroDto)
			caricaListaRegistro ()
		}

		 */
	}
}
