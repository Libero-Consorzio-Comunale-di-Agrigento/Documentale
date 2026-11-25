package it.finmatica.gestionedocumenti.dizionari.commons

import it.finmatica.afc.AfcAbstractRecord
import it.finmatica.afc.PaginatedViewModel
import it.finmatica.gestionedocumenti.commons.Utils
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Messagebox

@VariableResolver(DelegatingVariableResolver)
class DizionariDettaglioViewModel extends AfcAbstractRecord {

    // bean
    @WireVariable
    GestioneDocumentiSpringSecurityService springSecurityService

    // FIXME[SPRINGBOOT]: questo metodo non dovrebbe stare qui, siccome riferisce a logiche specifiche del protocollo.
    Collection<String> validaMaschera(GestioneDocumentiSpringSecurityService springSecurityService = this.springSecurityService) {
        Collection<String> messaggi = []

        boolean dizProtocolloVisible = Utils.isUtenteAmministratore() || springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_DIZIONARI_PROTOCOLLO.valore)
        if (!dizProtocolloVisible) {
            messaggi << "L'utente ${springSecurityService.principal.username} non puo' accedere a quest'area"
        }

        return messaggi
    }

    @Command
    void onSalva() {}

    @Command
    void onSalvaChiudi() {}

    @Command
    void onSettaValido(@BindingParam("valido") boolean valido) {
        def that = this
        Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto", [valido ? "valido" : "non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
                Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK == e.getName()) {
                            selectedRecord.valido = valido
                            onSalva()
                            BindUtils.postNotifyChange(null, null, that, "selectedRecord")
                            BindUtils.postNotifyChange(null, null, that, "datiCreazione")
                            BindUtils.postNotifyChange(null, null, that, "datiModifica")
                        } else if (Messagebox.ON_CANCEL == e.getName()) {
                            //Cancel is clicked
                        }
                    }
                }
        )
    }

}
