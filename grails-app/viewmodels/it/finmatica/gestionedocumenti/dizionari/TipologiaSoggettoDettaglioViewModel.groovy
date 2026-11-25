package it.finmatica.gestionedocumenti.dizionari
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4RuoloDTO
import it.finmatica.gestionedocumenti.soggetti.*
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.gestionedocumenti.dizionari.commons.DizionariDettaglioViewModel
import org.apache.commons.lang.StringUtils
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class TipologiaSoggettoDettaglioViewModel extends DizionariDettaglioViewModel {

	// services
	@WireVariable private TipologiaSoggettoDTOService	tipologiaSoggettoDTOService
	@WireVariable private TipologiaSoggettoService	tipologiaSoggettoService
	// componenti
	Window 		self
	
	// dati
	List<WkfTipoOggettoDTO> 			listaOggetti
	List<TipologiaSoggettoRegolaDTO>	listaTipologiaSoggettoRegola
	List<Ad4RuoloDTO> 					listaRuoli
	List<TipoSoggetto>					listTipoSoggetto
	def							        mapTipoSoggetto
		
	// bean
	@WireVariable
	def  regoleDiCalcoloSoggettiConfig
	
	// stato
	def selectedLayout
	List<LayoutSoggetti> listaZul
	List<LayoutSoggetti> listaZulPossibili
	
	List<RegolaCalcoloSoggetti>	 listaRegoleComponenteDefault 
	List<RegolaCalcoloSoggetti>	 listaRegoleUnitaDefault      
	List<RegolaCalcoloSoggetti>	 listaRegoleComponenteLista  
	List<RegolaCalcoloSoggetti>	 listaRegoleUnitaLista       

	@Init
	void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		listaZulPossibili = regoleDiCalcoloSoggettiConfig.getLayoutSoggettiList();
		
		listaRegoleComponenteDefault = regoleDiCalcoloSoggettiConfig.getListaRegoleComponenteDefault()
		listaRegoleUnitaDefault      = regoleDiCalcoloSoggettiConfig.getListaRegoleUnitaDefault()
		listaRegoleComponenteLista   = regoleDiCalcoloSoggettiConfig.getListaRegoleComponenteLista()
		listaRegoleUnitaLista        = regoleDiCalcoloSoggettiConfig.getListaRegoleUnitaLista()
		
		listaOggetti 			= WkfTipoOggetto.list(sort: "nome", order: "asc").toDTO()
		listTipoSoggetto = [new TipoSoggetto(codice:"-- nessuno --", descrizione: "-- nessuno --")] + regoleDiCalcoloSoggettiConfig.getTipoSoggettoList()
		mapTipoSoggetto = listTipoSoggetto.collectEntries {
			t -> [t.codice, t]
		}
				
		/*
		listaRegoleComponenteDefault	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_COMPONENTE, RegolaCalcolo.TIPO_DEFAULT).toDTO()
		listaRegoleUnitaDefault     	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_UNITA, 	 RegolaCalcolo.TIPO_DEFAULT).toDTO()
		listaRegoleComponenteLista  	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_COMPONENTE, RegolaCalcolo.TIPO_LISTA).toDTO()
		listaRegoleUnitaLista       	= [nessuno] + RegolaCalcolo.findAllByCategoriaAndTipo(TipoSoggetto.CATEGORIA_UNITA, 	 RegolaCalcolo.TIPO_LISTA).toDTO()
        */
		
		def nessuno = new RegolaCalcoloSoggetti(nomeBean:"-- nessuno --", nomeMetodo:"-- nessuno --", titolo:"-- nessuno --", descrizione: "-- nessuno --");
		listaRegoleComponenteDefault	= [nessuno] + listaRegoleComponenteDefault
		listaRegoleUnitaDefault     	= [nessuno] + listaRegoleUnitaDefault
		listaRegoleComponenteLista  	= [nessuno] + listaRegoleComponenteLista
		listaRegoleUnitaLista       	= [nessuno] + listaRegoleUnitaLista
		
		listaRuoli = [new Ad4RuoloDTO(ruolo:"-- nessuno --", descrizione: "-- nessuno --")] + Ad4Ruolo.findAllByRuoloLike('%', [sort: "ruolo", order: "asc"])?.toDTO()

		if (id != null) {
			selectedRecord 			= (TipologiaSoggettoDTO) tipologiaSoggettoService.reloadFromDb(id).toDTO(["utenteUpd", "utenteIns"])
			listaTipologiaSoggettoRegola 	= caricaTipologiaSoggettoRegole(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)

			caricaListaZul ()

			selectedLayout = listaZul.find { it.url == selectedRecord.layoutSoggetti }
		} else {
			selectedRecord = new TipologiaSoggettoDTO(valido:true)
		}
	}

	@NotifyChange(["listaZul"])
	@Command
	void caricaListaZul () {
		listaZul = listaZulPossibili.findAll { it.tipoOggetto == selectedRecord.tipoOggetto.codice }
	}

	@NotifyChange("listaTipologiaSoggettoRegola")
	@Command
	void onSelectLayout () {
		// svuoto l'elenco dei soggetti.
		listaTipologiaSoggettoRegola = [];
		// lo ricreo
		for (TipoSoggetto tipoSoggetto : selectedLayout.soggetti) {
			
			listaTipologiaSoggettoRegola << new TipologiaSoggettoRegolaDTO(sequenza:listaTipologiaSoggettoRegola.size(), tipoSoggetto: tipoSoggetto.codice);
		}
	}

	private TipologiaSoggettoDTO caricaTipologia (Long idTipologiaSoggetto) {
		TipologiaSoggetto tipologia = TipologiaSoggetto.createCriteria().get {
			eq("id", idTipologiaSoggetto)

			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}
		return tipologia.toDTO()
	}

	private List<TipologiaSoggettoDTO> caricaTipologiaSoggettoRegole (Long idTipologiaSoggetto) {
		return TipologiaSoggettoRegola.createCriteria().list {
			eq ("tipologiaSoggetto.id", idTipologiaSoggetto)
			order ("sequenza", "asc")

			fetchMode("tipoSoggetto", 			FetchMode.JOIN)
			fetchMode("tipoSoggettoPartenza", 	FetchMode.JOIN)
			fetchMode("regolaLista", 			FetchMode.JOIN)
			fetchMode("regolaDefault", 			FetchMode.JOIN)
			fetchMode("ruolo", 					FetchMode.JOIN)
		}.toDTO()
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	void onSalva () {

		Collection<String> messaggiValidazione = validaMaschera()
		if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
			Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
			return
		}

		selectedRecord.layoutSoggetti = selectedLayout.url
		def itTipologia = tipologiaSoggettoDTOService.salva(selectedRecord, listaTipologiaSoggettoRegola).id
		selectedRecord 		 = (TipologiaSoggettoDTO) tipologiaSoggettoService.reloadFromDb(itTipologia).toDTO(["utenteUpd", "utenteIns"])
		listaTipologiaSoggettoRegola = caricaTipologiaSoggettoRegole(itTipologia)
		if (selectedRecord.id == null) {
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		}
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	void onSalvaChiudi() {
		onSalva()
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command
	void onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command
	void onSettaValido(@BindingParam("valido") boolean valido) {
		// se voglio disattivare la tipologia, prima verifico che non sia usata da nessuna tipologia di determina/delibera ancora valida.
		if (selectedRecord.valido && valido == false) {
			def tipologie = [];
			if (tipologie.size() > 0) {
				Clients.showNotification ("Non è possibile disattivare il modello testo perché è usato da altre tipologie ancora attive:\n" +
										  (tipologie.titolo.join("\n")), Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", tipologie.size()*3000, true);
				return;
			}
		}

		Messagebox.show("Modificare la validità della tipologia?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e){
					if (Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipologiaSoggettoDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipologiaSoggettoDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipologiaSoggettoDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}
}
