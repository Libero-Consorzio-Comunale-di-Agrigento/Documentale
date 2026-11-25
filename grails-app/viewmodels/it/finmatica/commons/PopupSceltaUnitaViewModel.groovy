package it.finmatica.commons

import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.zk.AlberoStrutturaOrganizzativa
import it.finmatica.gestionedocumenti.zk.AlberoStrutturaOrganizzativaNodo
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class PopupSceltaUnitaViewModel {

    // services
    @WireVariable
    private So4SpringSecurityService springSecurityService

    // componenti
    Window self
    String urlIcone
    String labelOperazione

    // dati per la costruzione dell'albero
    AlberoStrutturaOrganizzativa alberoSo4
    String filtroRicerca
    int livelloApertura = 1
    private int livelloAperturaIniziale = 1

    AlberoStrutturaOrganizzativaNodo unitaSelezionataNodo

    Integer sequenza
    List<Long> radici

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window window
              , @ExecutionArgParam("operazione") String operazione
              , @ExecutionArgParam("radici") List<Long> radici) {

        this.self = window
        this.urlIcone = "images/ags/16x16/"
        this.labelOperazione = "Seleziona"

        livelloApertura = 1
        livelloAperturaIniziale = livelloApertura
        this.radici = radici

        onCerca()
    }

    boolean isAbilitato(AlberoStrutturaOrganizzativaNodo nodo) {
        return true
    }

    @NotifyChange(["alberoSo4", "livelloApertura"])
    @Command
    void onCerca() {
        if (filtroRicerca?.length() > 0) {
            // in caso di ricerca, mostro tutti i nodi aperti:
            livelloApertura = Integer.MAX_VALUE
        } else {
            // ripristino il livello di apertura di defalut
            livelloApertura = livelloAperturaIniziale
        }

        // carico l'albero dalla radice
        String otticaCodice = springSecurityService.principal?.ottica()?.codice
        if (!otticaCodice) {
            otticaCodice = Impostazioni.OTTICA_SO4.valore
        }
        alberoSo4 = new AlberoStrutturaOrganizzativa(otticaCodice, false, "", filtroRicerca, radici)
    }

    @Command
    void onAnnulla() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    void onSelezionaUnita() {

        So4UnitaPubbDTO unitaPubbDTO = unitaSelezionataNodo.getUnita()
        Events.postEvent(Events.ON_CLOSE, self, unitaPubbDTO)
    }
}
