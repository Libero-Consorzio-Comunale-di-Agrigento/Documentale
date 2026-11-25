package it.finmatica.afc

import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

abstract class AfcAbstractGrid implements IAfcGrid {

    public static final int PAGE_SIZE_DEFAULT = 30

    Window self

    // Paginazione
    int pageSize = PAGE_SIZE_DEFAULT
    int activePage = 0
    int totalSize = 0

    // Filtro in ricerca
    String filtro

    // Item selezionato
    def selectedRecord

    // Filtro su record validi
    boolean visualizzaTutti = false

    // da settare a false se il dizionario non supporta la cancellazione logica dei record
    boolean eliminazioneLogicaAttiva = true

    // da settare a false se il dizionario è read only
    boolean modificaAttiva = true
    boolean eliminazioneAttiva = true
    boolean  creazioneAttiva = true

    boolean exportExcel = false

    @Command
    abstract void onPagina();

    @Command
    abstract void onRefresh();

    @Command
    abstract void onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord);

    @Command
    abstract void onElimina();

    @Command
    abstract void onVisualizzaTutti();

    @Command
    abstract void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event);

    @Command
    abstract void onCancelFiltro();

    @Command
    abstract void onExportExcel();

    @Command
    void onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }
}
