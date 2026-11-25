package it.finmatica.gestionedocumenti.dizionari
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.registri.Registro
import it.finmatica.gestionedocumenti.registri.RegistroDTO
import it.finmatica.gestionedocumenti.registri.RegistroDTOService
import it.finmatica.gestionedocumenti.registri.TipoRegistroDTO
import it.finmatica.gestionedocumenti.dizionari.commons.DizionariDettaglioViewModel
import org.apache.commons.lang.StringUtils
import org.hibernate.annotations.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class RegistroDettaglioViewModel extends DizionariDettaglioViewModel {

	RegistroDTO 	selectedRecord

	// services
	@WireVariable private RegistroDTOService	registroDTOService


	@NotifyChange(["selectedRecord"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("tipoRegistro") TipoRegistroDTO tipoRegistro , @ExecutionArgParam("id") Long id) {
		this.self = w
		if (id != null) {
			selectedRecord = caricaRegistroDto(id)
		} else {

			int annoAttuale = ((Registro.createCriteria().get {
				projections {
					max ("anno")
				}
				eq("tipoRegistro.codice", tipoRegistro.codice)
				eq("valido", true)
			}?:(Calendar.getInstance().get(Calendar.YEAR)-1)) + 1)
			selectedRecord = new RegistroDTO(valido:true, anno: annoAttuale, ultimoNumero: 0)
			selectedRecord.tipoRegistro = tipoRegistro
		}
	}

	private RegistroDTO caricaRegistroDto (Long idRegistro) {
		Registro registro = Registro.createCriteria().get {
			eq("id", idRegistro)
			fetchMode("tipoRegistro", FetchMode.JOIN)
		}
		return registro.toDTO()
	}


	/*
	 * Implementazione della classe AfcAbstractRecord
	 */

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	void onSalva () {

		Collection<String> messaggiValidazione = validaMaschera()
		if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
			Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
			return
		}


		if (Registro.createCriteria().get {
			eq("anno", selectedRecord.anno)
			eq("tipoRegistro.codice", selectedRecord.tipoRegistro.codice)
			eq("valido", true)
		} != null) {
			Messagebox.show ("Non è possibile inserire il nuovo registro: esiste già un registro attivo per l'anno ${selectedRecord.anno}, selezionare un anno diverso.");
			return;
		}

		selectedRecord = registroDTOService.salva(selectedRecord)
	}


	@Command
	void onSalvaChiudi() {
		onSalva ()
		onChiudi ()
	}

	@Command
	void onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, RegistroDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, RegistroDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, RegistroDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}
}
