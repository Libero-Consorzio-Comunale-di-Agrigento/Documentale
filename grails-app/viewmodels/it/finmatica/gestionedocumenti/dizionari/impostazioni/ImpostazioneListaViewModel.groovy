package it.finmatica.gestionedocumenti.dizionari.impostazioni

import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.dizionari.commons.DizionariDettaglioViewModel
import it.finmatica.gestionedocumenti.dizionari.commons.PaginatedDizionariDettaglioViewModel
import it.finmatica.gestionedocumenti.impostazioni.Impostazione
import it.finmatica.gestionedocumenti.impostazioni.ImpostazioneDTO
import it.finmatica.gestionedocumenti.impostazioni.ImpostazioneService
import it.finmatica.gestionedocumenti.impostazioni.ImpostazioniMap
import it.finmatica.gorm.criteria.PagedResultList
import org.apache.commons.lang.StringUtils
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Combobox
import org.zkoss.zul.Comboitem
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class ImpostazioneListaViewModel extends PaginatedDizionariDettaglioViewModel {

    @WireVariable
    private ImpostazioniMap impostazioniMap

    // componenti
    Window self

    // dati
    HashMap selectedRecord
    ArrayList listaImpostazioniModificabili

    // services
    @WireVariable
    ImpostazioneService impostazioneService

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w) {
        super.init()

        activePage = 0
        totalSize = 0
        this.self = w
        caricaListaImpostazioni()
    }

    @NotifyChange(["listaImpostazioniModificabili", "totalSize"])
    private void caricaListaImpostazioni() {
        listaImpostazioniModificabili = new ArrayList()
        PagedResultList lista = Impostazione.createCriteria().list(max: pageSize, offset: pageSize * activePage) {

            // se non sono l'utente amministratore, mostro solo quelli modificabili.
//			if (!GestioneDocumentiUtils.isUtenteAmministratore()) {
//				eq ("modificabile", true)
//			}

            order("codice", "asc")

            if (filtro != "" && filtro != null) {
                or {
                    ilike("codice", "%" + filtro + "%")
                    ilike("descrizione", "%" + filtro + "%")
                    ilike("valore", "%" + filtro + "%")
                }
            }
        }
        totalSize = lista.totalCount
        List<ImpostazioneDTO> listaImpostazioni = lista.toDTO()
        listaImpostazioni.each {

            HashMap hashMap = new HashMap()
            hashMap.put("impostazione", it)
            hashMap.put("modificato", new Boolean(false))
            listaImpostazioniModificabili.add(hashMap)
        }
    }

    @Command
    void onChangeTxt(@BindingParam("content") HashMap content) {
        content.modificato = true
    }

    @Command
    void onCreateCbx(@BindingParam("target") Component target, @BindingParam("content") HashMap content) {
        if (content?.impostazione?.caratteristiche != null) {
            def xml = new XmlSlurper().parseText(content.impostazione.caratteristiche)
            def selectedItem
            xml.children()?.each { item ->
                Comboitem ci = new Comboitem(item.@label.text())
                ci.value = item.@value.text()
                if (content.impostazione.valore == ci.value) selectedItem = ci
                target.appendChild(ci)
            }
            ((Combobox) target).setSelectedItem(selectedItem)
        }
    }

    @Command
    void onSelectCbx(@BindingParam("target") Component target, @BindingParam("content") HashMap content) {
        content.impostazione.valore = ((Combobox) target).getSelectedItem().value
        content.modificato = true
    }

    @NotifyChange(["listaImpostazioniModificabili", "totalSize"])
    @Command
    void onPagina() {
        caricaListaImpostazioni()
        BindUtils.postNotifyChange(null, null, this, "listaImpostazioniModificabili")
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "activePage")
    }

    @NotifyChange(["listaImpostazioniModificabili", "selectedRecord", "activePage", "totalSize"])
    @Command
    void onRefresh() {
        selectedRecord = null
        activePage = 0
        caricaListaImpostazioni()
    }

    @Command
    void onSalva() {

        Collection<String> messaggiValidazione = validaMaschera()
        if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
            Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
            return
        }
        def that = this
        listaImpostazioniModificabili.findAll { it.modificato == true }.each { imp ->
            that.impostazioneService.salva(imp.impostazione)
            Ente e = imp.impostazione.ente?.domainObject
            Impostazione i = null
            if (e) {
                i = Impostazione.findByCodiceAndEnte(imp.impostazione.codice, e)
            } else {
                i = Impostazione.findByCodice(imp.impostazione.codice)
            }
            imp.impostazione = (i.get(imp.impostazione.id)).toDTO()
            imp.modificato = true
        }

        impostazioniMap.refresh()

        Clients.showNotification("Impostazioni salvate.", Clients.NOTIFICATION_TYPE_INFO, self, "before_center", 3000, true)
    }

    @Command
    void onModificaImpostazione(@BindingParam("target") Component target, @BindingParam("content") HashMap content) {
        Ente e = content.impostazione.ente?.domainObject
        Impostazione impostazioneD = null
        if (e) {
            impostazioneD = Impostazione.findByCodiceAndEnte(content.impostazione.codice, e)
        } else {
            impostazioneD = Impostazione.findByCodice(content.impostazione.codice)
        }
        impostazioneD.valore = target.value
        impostazioneD.save()
        impostazioniMap.refresh()
        content.modificato = true
    }

    @NotifyChange(["listaImpostazioniModificabili", "totalSize"])
    @Command
    void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        selectedRecord = null
        activePage = 0
        caricaListaImpostazioni()
    }
}
