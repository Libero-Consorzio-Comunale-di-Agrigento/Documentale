package it.finmatica.commons
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.documenti.TipoDocumentoModelloDTO
import it.finmatica.gestionetesti.GestioneTestiModelloDTOService
import it.finmatica.gestionetesti.reporter.GestioneTestiModelloDTO
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class PopupSceltaModelloTestoViewModel {
	
	Window self
	
	List<GestioneTestiModelloDTO> listaModelliTesto
	@WireVariable private GestioneTestiModelloDTOService gestioneTestiModelloDTOService
	def selectedRecord
	TipoDocumentoModelloDTO modelloTestoAssoc
	boolean modelloTestoObbligatorio
	
	boolean predefinito

	@NotifyChange(["listaModelliTesto"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("tipoModello") String tipoModello, @ExecutionArgParam("modelloTestoObbligatorio") boolean modelloTestoObbligatorio ) {
		this.self = w
		this.modelloTestoObbligatorio = modelloTestoObbligatorio
		listaModelliTesto = gestioneTestiModelloDTOService.getListaModelli(tipoModello)
		modelloTestoAssoc = new TipoDocumentoModelloDTO()
	}

	@Command onSeleziona() {

		if(selectedRecord != null){

			modelloTestoAssoc.modelloTesto = selectedRecord
			if(modelloTestoAssoc.codice== null){
				Messagebox.show("Selezionare un codice per il modello testo", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
			}
			else Events.postEvent(Events.ON_CLOSE, self, modelloTestoAssoc)
		} else {
			Messagebox.show("Selezionare uno dei modelli testo presenti nella lista", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
		}
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
