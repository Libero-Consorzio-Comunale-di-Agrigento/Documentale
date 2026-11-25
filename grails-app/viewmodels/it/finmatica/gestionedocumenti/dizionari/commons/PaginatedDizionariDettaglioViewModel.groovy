package it.finmatica.gestionedocumenti.dizionari.commons

import it.finmatica.afc.IAfcRecord
import it.finmatica.afc.PaginatedViewModel
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.select.annotation.WireVariable


abstract class PaginatedDizionariDettaglioViewModel extends PaginatedViewModel implements IAfcRecord {

    DizionariDettaglioViewModel dizionariDettaglioViewModel

    @WireVariable
    GestioneDocumentiSpringSecurityService springSecurityService

    @Init
    init() {
        dizionariDettaglioViewModel = new DizionariDettaglioViewModel()
    }

    Collection<String> validaMaschera() {
        //passo il security service direttamente perchè se no groovy non fa il
        return dizionariDettaglioViewModel.validaMaschera(springSecurityService)
    }

    @Command
    void onSalva() { dizionariDettaglioViewModel.onSalva() }

    @Command
    void onSalvaChiudi() { dizionariDettaglioViewModel.onSalvaChiudi() }

    @Command
    void onSettaValido(@BindingParam("valido") boolean valido) {
        dizionariDettaglioViewModel.onSettaValido(valido)
    }

    protected void aggiornaDatiCreazione(String idUtenteIns, Date dataCreazione) {
        dizionariDettaglioViewModel.aggiornaDatiCreazione(idUtenteIns, dataCreazione)
    }

    protected void aggiornaDatiModifica(String idUtenteMod, Date dataModifica) {
        dizionariDettaglioViewModel.aggiornaDatiModifica(idUtenteMod, dataModifica)
    }

    String getDatiCreazione() {
        return dizionariDettaglioViewModel.datiCreazione
    }

    void setDatiCreazione(String datiCreazione) {
        dizionariDettaglioViewModel.datiCreazione = datiCreazione
    }

    String getDatiModifica() {
        return dizionariDettaglioViewModel.datiModifica
    }

    void setDatiModifica(String datiModifica) {
        dizionariDettaglioViewModel.datiModifica = datiModifica
    }

    @Command
    void onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }
}

