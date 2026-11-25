package it.finmatica.gestionedocumenti.dizionari

import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.dizionari.commons.DizionariDettaglioViewModel
import it.finmatica.gestionedocumenti.notifiche.Email
import it.finmatica.gestionedocumenti.notifiche.EmailDTO
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.NotificaDTO
import it.finmatica.gestionedocumenti.notifiche.NotificaDTOService
import it.finmatica.gestionedocumenti.notifiche.NotificaDestinatario
import it.finmatica.gestionedocumenti.notifiche.NotificaDestinatarioDTO
import it.finmatica.gestionedocumenti.notifiche.NotificheService
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RegolaCalcoloNotifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.TipoNotifica
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgStep
import org.apache.commons.lang.StringUtils
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.AfterCompose
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Combobox
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class NotificaDettaglioViewModel extends DizionariDettaglioViewModel {

    // service
    @WireVariable
    private NotificaDTOService notificaDTOService

    // componenti
    @Wire("#popupNuovoDestinatario")
    Window popupNuovoDestinatario

    @Wire("#popupIndirizzoEmail")
    Window popupIndirizzoEmail

    // bean
    @WireVariable
    NotificheService notificheService

    // dati
    List<TipoNotifica> listaTipoNotifica
    List<EmailDTO> listaEmail
    List<NotificaDestinatarioDTO> listaNotificaDestinatario
    List<NotificaDestinatarioDTO> listaDestinatari
    EmailDTO selectedEmail
    NotificaDestinatarioDTO selectedNotificaDestinatario
    NotificaDestinatarioDTO destinatario

    def listaTipiDestinatari
    def listaStep
    def oggettiDisponibili
    def listaTipiAllegati

    // stato
    String stato

    @NotifyChange(["selectedRecord", "soggettiList", "totalSize"])
    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
        this.self = w
        if (id != null) {
            caricaNotificaDto(id)
        } else {
            selectedRecord = new NotificaDTO(valido: true)
        }
        listaTipoNotifica = notificheService.getTipiNotifiche()
        listaTipiDestinatari = [new RegolaCalcoloNotifica(titolo: "", descrizione: "-- nessuno --")] + notificheService.getRegoleCalcoloDestinatari()
        listaTipiAllegati = [new RegolaCalcoloNotifica(titolo: "", descrizione: "-- nessuno --")] + notificheService.getRegoleCalcoloAllegati()
    }

    @AfterCompose
    void afterCompose() {
        Selectors.wireComponents(self, this, false)
    }

    def getCampiDisponibili() {
        // ottengo i campi che sono disponibili per gli oggetti di questa notifica:
        return notificheService.getRegoleCalcoloTag().findAll { campo ->
            (campo.tipiDocumento.intersect(oggettiDisponibili.findAll {
                it.selezionato
            }.codice).size() > 0)
        }
    }

    @NotifyChange('destinatario')
    @Command
    void onAggiungiDestinatario() {
        this.destinatario = new NotificaDestinatarioDTO(id: -1, notifica: selectedRecord)
        this.popupNuovoDestinatario.doModal()
    }

    @NotifyChange(['destinatario', 'selectedEmail'])
    @Command
    void onChiudiPopup() {
        this.popupNuovoDestinatario.setVisible(false)
        this.popupIndirizzoEmail.setVisible(false)
        this.destinatario = null
        this.selectedEmail = null
    }

    @Command
    void onCercaSoggetto() {
        Window w = Executions.createComponents("/commons/popupRicercaSoggetti.zul", self, [id: -1])
        w.onClose { Event event ->
            if (event.data != null) {
                this.destinatario.soggetto = event.data
                BindUtils.postNotifyChange(null, null, this, "destinatario")
            }
        }
        w.doModal()
    }

    @NotifyChange(["listaEmail", "listaNotificaDestinatario", "listaDestinatari"])
    @Command
    void onSalvaDestinatario() {
        NotificaDTO notificaDTO = notificaDTOService.salva(destinatario)
        this.selectedRecord.version = notificaDTO.version
        refreshListe()
        this.popupNuovoDestinatario.setVisible(false)
    }

    @NotifyChange(["listaEmail", "listaNotificaDestinatario", "listaDestinatari"])
    @Command
    void onEliminaDestinatario(@BindingParam("destinatario") NotificaDestinatarioDTO destinatario) {
        NotificaDTO notificaDTO = notificaDTOService.elimina(destinatario)
        this.selectedRecord.version = notificaDTO.version
        refreshListe()
    }

    private void caricaNotificaDto(Long idNotifica) {
        selectedRecord = notificaDTOService.forceLoadFromDb(idNotifica)

        aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
        aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)

        refreshOggettiDisponibili()
        refreshListe()
        refreshListaStep()


        BindUtils.postNotifyChange(null, null, this, "selectedRecord")
        BindUtils.postNotifyChange(null, null, this, "datiCreazione")
        BindUtils.postNotifyChange(null, null, this, "datiModifica")
        BindUtils.postNotifyChange(null, null, this, "listaEmail")
        BindUtils.postNotifyChange(null, null, this, "listaStep")
        BindUtils.postNotifyChange(null, null, this, "listaNotificaDestinatario")
    }

    void refreshListe() {
        listaNotificaDestinatario = NotificaDestinatario.createCriteria().list {
            eq("notifica.id", selectedRecord.id)
            isNotNull("email")

            fetchMode("notifica", FetchMode.JOIN)
            fetchMode("email", FetchMode.JOIN)
        }.toDTO()

        listaDestinatari = NotificaDestinatario.createCriteria().list {
            eq("notifica.id", selectedRecord.id)
            isNull("email")

            fetchMode("ruolo", FetchMode.JOIN)
            fetchMode("unita", FetchMode.JOIN)
        }.toDTO(["soggetto"])

        listaEmail = Email.createCriteria().list {
            eq("valido", true)
            if (listaNotificaDestinatario.size() > 0) {
                not {
                    'in'("indirizzoEmail", listaNotificaDestinatario*.email)
                }
            }
            fetchMode("notifica", FetchMode.JOIN)
        }.toDTO()
    }

    private void refreshListaStep() {
        listaStep = selectedRecord.tipo?.azioni == null ? null : WkfCfgStep.createCriteria().list {
            createAlias("azioniIngresso", "azioniIn", CriteriaSpecification.LEFT_JOIN)
            createAlias("azioniUscita", "azioniOut", CriteriaSpecification.LEFT_JOIN)

            projections {
                cfgIter { groupProperty("nome") }

                groupProperty("titolo")
            }

            cfgIter {
                eq("stato", WkfCfgIter.STATO_IN_USO)
            }

            or {
                for (String metodoAzione : selectedRecord.tipo.azioni.split(",")) {
                    and {
                        eq("azioniIn.nomeBean", metodoAzione.split("\\.")[0])
                        eq("azioniIn.nomeMetodo", metodoAzione.split("\\.")[1])
                    }
                }

                for (String metodoAzione : selectedRecord.tipo.azioni.split(",")) {
                    and {
                        eq("azioniOut.nomeBean", metodoAzione.split("\\.")[0])
                        eq("azioniOut.nomeMetodo", metodoAzione.split("\\.")[1])
                    }
                }
            }
        }.collect { [titoloIter: it[0], titoloStep: it[1]] }
    }

    private void refreshOggettiDisponibili() {
        def listaOggetti = selectedRecord.getListaOggetti()
        oggettiDisponibili = notificheService.getTipiDocumento().collect {
            [codice: it, selezionato: listaOggetti.contains(it)]
        }
    }

    @NotifyChange(["campiDisponibili"])
    @Command
    void onSelezionaOggettiDisponibili() {
    }

    @NotifyChange(["selectedRecord", "listaStep", "oggettiDisponibili", "campiDisponibili"])
    @Command
    void onSelectTipologia(@ContextParam(ContextType.TRIGGER_EVENT) SelectEvent<Combobox, TipoNotifica> selectEvent) {
        selectedRecord.tipoNotifica = selectEvent.selectedObjects.first().codice

        // di default, seleziono tutti gli oggetti:
        selectedRecord.oggetti = notificheService.getTipiDocumento().join(",")

        refreshListaStep()
        refreshOggettiDisponibili()
    }

    //////////////////////////////////////////
    //				SALVATAGGIO				//
    //////////////////////////////////////////

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "listaEmail", "listaNotificaDestinatario"])
    @Command
    void onDuplica() {
        // prima salvo, poi duplico:
        if (!salva()) {
            return
        }

        // se ho salvato correttamente, duplico la notifica:
        selectedRecord = notificaDTOService.duplica(selectedRecord)
        caricaNotificaDto(selectedRecord.id)

        Clients.showNotification("Notifica duplicata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
    }

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "listaEmail", "listaNotificaDestinatario"])
    @Command
    void onSalva() {
        salva()
    }

    private boolean salva() {
        Collection<String> messaggiValidazione = validaMaschera()
        if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
            Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
            return
        }

        // Prima di salvare, controllo che nel testo e nell'oggetto della notifica ci siano solo campi validi:
        // ottengo i campi scritti nel testo:
        def campiUsati = controllaCampiUsati(selectedRecord.oggetto)
        if (campiUsati.size() > 0) {
            Clients.showNotification("Non è possibile salvare: nell'Oggetto sono stati inseriti dei campi non validi: " + campiUsati.join(","), Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 5000, true)
            return false
        }

        campiUsati = controllaCampiUsati(selectedRecord.testo)
        if (campiUsati.size() > 0) {
            Clients.showNotification("Non è possibile salvare: nel Testo sono stati inseriti dei campi non validi: " + campiUsati.join(","), Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 5000, true)
            return false
        }

        selectedRecord.oggetti = oggettiDisponibili.findAll { it.selezionato }.codice.join(",")
        def idNotifica = notificaDTOService.salva(selectedRecord).id
        caricaNotificaDto(idNotifica)

        Clients.showNotification("Notifica salvata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
        return true
    }

    private controllaCampiUsati(String testo) {
        def campiOggetto = (testo =~ /\[([\w]+)\]/).collect { match -> match[1] }
        def campiDisponibili = getCampiDisponibili().collect { it.codice }

        return (campiOggetto - campiDisponibili)
    }

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
    @Command
    void onSalvaChiudi() {
        onSalva()
        onChiudi()
    }

    @Command
    void onSettaValido(@BindingParam("valido") boolean valido) {
        Messagebox.show("Modificare la validità della notifica?", "Modifica validità", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) { Event e ->
            if (Messagebox.ON_OK.equals(e.getName())) {
                super.getSelectedRecord().valido = valido
                onSalva()
                BindUtils.postNotifyChange(null, null, this, "selectedRecord")
                BindUtils.postNotifyChange(null, null, this, "datiCreazione")
                BindUtils.postNotifyChange(null, null, this, "datiModifica")
            }
        }
    }

    @Command
    @NotifyChange(["listaEmail", "listaNotificaDestinatario"])
    void dropToList1(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        if (ctx.triggerEvent.dragged.value != null && !listaEmail.contains(ctx.triggerEvent.dragged.value)) {
            NotificaDestinatarioDTO selezionato = ctx.triggerEvent.dragged.value
            selectedRecord.version = notificaDTOService.elimina(selezionato).version
            refreshListe()
        }
    }

    @Command
    @NotifyChange(["listaEmail", "listaNotificaDestinatario"])
    void dropToList2(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        if (ctx.triggerEvent.dragged.value != null && !listaNotificaDestinatario.contains(ctx.triggerEvent.dragged.value)) {
            NotificaDestinatarioDTO nuovo = new NotificaDestinatarioDTO(id: -1)
            EmailDTO selezionata = ctx.triggerEvent.dragged.value
            nuovo.email = selezionata.indirizzoEmail
            nuovo.notifica = selectedRecord
            nuovo.assegnazione = NotificaSoggetto.Assegnazione.CONOSCENZA
            selectedRecord.version = notificaDTOService.salva(nuovo).version
            refreshListe()
        }
    }

    @Command
    @NotifyChange(["listaEmail", "listaNotificaDestinatario"])
    void insertToList1(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        if (!listaEmail.contains(ctx.triggerEvent.dragged.value)) {
            NotificaDestinatarioDTO selezionato = ctx.triggerEvent.dragged.value
            selectedRecord.version = notificaDTOService.elimina(selezionato).version
            refreshListe()
        }
    }

    @Command
    @NotifyChange(["listaEmail", "listaEmailAssociate"])
    void insertToList2(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        if (!listaNotificaDestinatario.contains(ctx.triggerEvent.dragged.value)) {
            NotificaDestinatarioDTO nuovo = new NotificaDestinatarioDTO(id: -1)
            EmailDTO selezionata = ctx.triggerEvent.dragged.value
            nuovo.email = selezionata.indirizzoEmail
            nuovo.notifica = selectedRecord
            nuovo.assegnazione = NotificaSoggetto.Assegnazione.CONOSCENZA
            selectedRecord.version = notificaDTOService.salva(nuovo).version
            selectedEmail = null
            refreshListe()
        }
    }

    @NotifyChange(["listaEmail", "listaNotificaDestinatario"])
    @Command
    void onSelTuttoADx(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        listaEmail.each { EmailDTO selezionata ->
            NotificaDestinatarioDTO nuovo = new NotificaDestinatarioDTO(id: -1)
            nuovo.email = selezionata.indirizzoEmail
            nuovo.notifica = selectedRecord
            nuovo.assegnazione = NotificaSoggetto.Assegnazione.CONOSCENZA
            selectedRecord.version = notificaDTOService.salva(nuovo).version
            selectedEmail = null
        }
        refreshListe()
    }

    @NotifyChange(["listaEmail", "listaNotificaDestinatario"])
    @Command
    void onSelTuttoASx(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        listaNotificaDestinatario.each { NotificaDestinatarioDTO selezionato ->
            selectedRecord.version = notificaDTOService.elimina(selezionato).version
        }
        refreshListe()
    }

    @NotifyChange(["listaEmail", "listaNotificaDestinatario", "selectedEmail"])
    @Command
    void onSelADx(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        NotificaDestinatarioDTO nuovo = new NotificaDestinatarioDTO(id: -1)
        nuovo.email = selectedEmail.indirizzoEmail
        nuovo.notifica = selectedRecord
        nuovo.assegnazione = NotificaSoggetto.Assegnazione.CONOSCENZA
        selectedRecord.version = notificaDTOService.salva(nuovo).version
        selectedEmail = null
        refreshListe()
    }

    @NotifyChange(["listaEmail", "listaNotificaDestinatario", "selectedNotificaDestinatario"])
    @Command
    void onSelASx(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        selectedRecord.version = notificaDTOService.elimina(selectedNotificaDestinatario).version
        refreshListe()
    }

    @Command
    @NotifyChange(["selectedEmail"])
    void onAggiungiEmail() {
        selectedEmail = new EmailDTO()
        popupIndirizzoEmail.doModal()
    }

    @NotifyChange(["listaEmail", "listaNotificaDestinatario", "selectedEmail"])
    @Command
    void onSalvaEmail() {
        notificaDTOService.salvaIndirizzoEmail(selectedEmail)
        refreshListe()
    }
}
