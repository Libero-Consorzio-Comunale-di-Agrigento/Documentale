package it.finmatica.commons
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import groovy.util.logging.Slf4j
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.gestionedocumenti.soggetti.MetodoCalcoloSoggetti
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggettoService
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

@Slf4j
@VariableResolver(DelegatingVariableResolver)
class PopupSceltaSoggettoViewModel {

	// service
	@WireVariable private So4SpringSecurityService springSecurityService
	@WireVariable private TipologiaSoggettoService tipologiaSoggettoService

	// componenti
	Window self

	// dati
	def listaSoggetti
	def selectedRecord
	def documentoDto
	def soggetti
	long idTipologiaSoggetto
	String tipoSoggetto

	// stato
	String categoriaSoggetto
	String labelTitolo
	String labelColonna1
	String labelColonna2
	String labelNessunRisultato

	@NotifyChange("listaUnita")
	@Init init(@ContextParam(ContextType.COMPONENT) 			Window w
			 , @ExecutionArgParam("idTipologiaSoggetto") 	    long idTipologiaSoggetto
			 , @ExecutionArgParam("documento") 					def documentoDto		// la domain class di partenza
			 , @ExecutionArgParam("soggetti") 					def soggetti
			 , @ExecutionArgParam("tipoSoggetto") 				String tipoSoggetto
			 , @ExecutionArgParam("categoriaSoggetto") 			String categoriaSoggetto) {
		this.self = w
		this.categoriaSoggetto = categoriaSoggetto;
		this.documentoDto = documentoDto;
		this.soggetti = soggetti;
		this.tipoSoggetto = tipoSoggetto;
		this.idTipologiaSoggetto = idTipologiaSoggetto;

		labelTitolo 		 = Labels.getLabel("gestionedocumenti.documenti.popupSceltaSoggetto.${tipoSoggetto}.titolo");
		labelColonna1 		 = Labels.getLabel("gestionedocumenti.documenti.popupSceltaSoggetto.${tipoSoggetto}.colonna1");
		labelColonna2 		 = Labels.getLabel("gestionedocumenti.documenti.popupSceltaSoggetto.${tipoSoggetto}.colonna2");
		labelNessunRisultato = Labels.getLabel("gestionedocumenti.documenti.popupSceltaSoggetto.${tipoSoggetto}.nessunRisultato");

		def lista = tipologiaSoggettoService.calcolaListaSoggetti (idTipologiaSoggetto, documentoDto.domainObject, soggetti, tipoSoggetto, null)

		listaSoggetti = [];
		def warnings = [];
		for (def componente : lista) {
			def soggetto = null;
			if (MetodoCalcoloSoggetti.Categoria.COMPONENTE.toString() == categoriaSoggetto) {
				soggetto = [descrizione: componente.nominativoSoggetto
				          , utente: 	 componente.soggetto?.utenteAd4?.toDTO()
				          , unita:  	 So4UnitaPubb.getUnita(componente.progrUnita, componente.ottica.codice).get()?.toDTO()];

				// se il soggetto calcolato non ha un utente o l'unità, vuol dire che molto probabimente
				// c'è qualche errore su so4.
				if (MetodoCalcoloSoggetti.Categoria.COMPONENTE.toString() == categoriaSoggetto && (soggetto.utente == null || soggetto.unita == null)) {
					warnings << "Il Componente con id ${componente.id} ha un utente o una unità organizzativa nulli.";
					continue;
				}

			} else {
    			soggetto = [descrizione: componente.descrizione
						  , utente: 	 null
                		  , unita:  	 componente.toDTO()];
			}

			listaSoggetti << soggetto;
		}

		listaSoggetti = listaSoggetti.sort { it.descrizione }

		BindUtils.postNotifyChange(null, null, this, "listaSoggetti")

		if (warnings.size() > 0) {
			Clients.showNotification("Attenzione! Ci sono incongruenze di dati in Struttura Organizzativa:\n"+warnings.join("\n"), Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", 5000, true);
		}
	}

	@Command onSelezionaSoggetto() {
		// se ho annullato la modifica, non faccio niente:
		if (selectedRecord == null)
			return;

		// se ho deselezionato il soggetto, lo svuoto
		if (soggetti[tipoSoggetto] == null) {
			soggetti[tipoSoggetto] = [:]
		}

		// se ho cambiato utente o unità, allora aggiorno il soggetti:
		if (soggetti[tipoSoggetto].utente?.id  != selectedRecord.utente?.id || soggetti[tipoSoggetto].unita?.progr != selectedRecord.unita?.progr) {
			soggetti[tipoSoggetto].tipoSoggetto	= tipoSoggetto
			soggetti[tipoSoggetto].descrizione 	= selectedRecord.descrizione
			soggetti[tipoSoggetto].unita 		= selectedRecord.unita
			soggetti[tipoSoggetto].utente 		= selectedRecord.utente
			soggetti[tipoSoggetto].modificato	= true

			tipologiaSoggettoService.aggiornaSoggetti(idTipologiaSoggetto, documentoDto.domainObject, soggetti, tipoSoggetto);
		}

		Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
