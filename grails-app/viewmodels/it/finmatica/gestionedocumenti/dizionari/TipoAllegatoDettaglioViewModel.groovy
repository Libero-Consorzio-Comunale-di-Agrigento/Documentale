package it.finmatica.gestionedocumenti.dizionari
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.documenti.TipoAllegato
import it.finmatica.gestionedocumenti.documenti.TipoAllegatoDTO
import it.finmatica.gestionedocumenti.documenti.TipoAllegatoDTOService
import it.finmatica.gestionedocumenti.dizionari.commons.DizionariDettaglioViewModel
import org.apache.commons.lang.StringUtils
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class TipoAllegatoDettaglioViewModel extends DizionariDettaglioViewModel {

	TipoAllegatoDTO 	selectedRecord

	// services
	@WireVariable private TipoAllegatoDTOService	tipoAllegatoDTOService


	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaTipoAllegatoDto(id)
		} else {
			selectedRecord = new TipoAllegatoDTO(valido:true)
		}
    }

	private TipoAllegatoDTO caricaTipoAllegatoDto (Long idTipoAllegato){
		return TipoAllegato.get(idTipoAllegato).toDTO()
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

//		if(selectedRecord.id == null){
//			Clients.showNotification("L'inserimento di nuovi tipi di allegato va effettuato dalla maschera del relativo dizionario del documentale", Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 10000, true)
//			return
//		}
		selectedRecord = tipoAllegatoDTOService.salva(selectedRecord)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	void onSalvaChiudi() {
		onSalva()
		onChiudi ()
	}


	@Command
	void onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoAllegatoDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoAllegatoDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoAllegatoDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}


}
