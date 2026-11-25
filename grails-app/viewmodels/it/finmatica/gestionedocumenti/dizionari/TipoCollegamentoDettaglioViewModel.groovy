package it.finmatica.gestionedocumenti.dizionari

import org.zkoss.bind.BindUtils
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.documenti.TipoCollegamento
import it.finmatica.gestionedocumenti.documenti.TipoCollegamentoDTO
import it.finmatica.gestionedocumenti.documenti.TipoCollegamentoService
import it.finmatica.gestionedocumenti.dizionari.commons.DizionariDettaglioViewModel
import org.apache.commons.lang.StringUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Button
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class TipoCollegamentoDettaglioViewModel extends DizionariDettaglioViewModel {

	// services
	@WireVariable
	private TipoCollegamentoService	tipoCollegamentoService

	@NotifyChange(["selectedRecord"])
    @Init
	void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaTipoCollegamentoDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		}
		else {
			selectedRecord = new TipoCollegamentoDTO(id: -1, valido: true)
		}
    }

	private TipoCollegamentoDTO caricaTipoCollegamentoDto (Long idTipoCollegamento){
		TipoCollegamento tipoCollegamento = tipoCollegamentoService.findById(idTipoCollegamento)
		return tipoCollegamento.toDTO()
	}

	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	void onSalva () {

		Collection<String> messaggiValidazione = validaMaschera()
		if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
			Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
			return
		}

		selectedRecord = tipoCollegamentoService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}


	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	void onSalvaChiudi() {
		onSalva()
		onChiudi ()
	}

	@Command
	void onSettaValido(@BindingParam("valido") boolean valido) {
		if(selectedRecord.sistema) {
			Clients.showNotification("Non è possibile attivare/disattivare i tipi di collegamento preinstallati", Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", 5000, true)
			return
		} else {
			super.onSettaValido(valido)
		}
	}
}
