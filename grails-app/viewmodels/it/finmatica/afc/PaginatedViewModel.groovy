package it.finmatica.afc

import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.GlobalCommand
import org.zkoss.zk.ui.event.Event

// fornisce le basi per avere un view model con paginazione, estendendo AfcAbstractGrid e dando i punti di attacco per
// il widget della selezione del numero di elementi per pagina.
abstract class PaginatedViewModel extends AfcAbstractGrid {
    public static final String PAGE_SIZE_SELECTED = "font-weight:bold"
    public static final String ON_CHANGE_PAGE_SIZE = "onChangePageSize"
    public static final String ON_CHANGE_PAGE_SIZE_POI_PAGINA = "onChangePageSizePoiPagina"

    Integer pageSizeDefault = PAGE_SIZE_DEFAULT
    String defaultCommandQueue = this.toString()
    Map<Integer, String> pageSizeOpzioni = [10: "", 20: "", 30: "", 50: "", 100: "", (this.pageSizeDefault): (PAGE_SIZE_SELECTED)]

    void setPageSizeDefault(Integer pageSizeDefault = PAGE_SIZE_DEFAULT) {
        if (!pageSizeDefault)
            return

        this.pageSizeDefault = pageSizeDefault
        onChangePageSize(pageSizeDefault)
    }

    @Override
    void setPageSize(int pageSize) {
        if (pageSize == this.pageSize)
            return

        pageSizeOpzioni[this.pageSize] = ""
        super.pageSize = pageSize // devo mettere super se no groovy impazzisce e va in stack overflow
        pageSizeOpzioni[this.pageSize] = PAGE_SIZE_SELECTED
        this.activePage = 0

        BindUtils.postNotifyChange(null, null, this, "pageSizeOpzioni")
        BindUtils.postNotifyChange(null, null, this, "activePage")
        BindUtils.postNotifyChange(null, null, this, "pageSize")
    }

    @Command
    @GlobalCommand
    void onChangePageSize(@BindingParam("newPageSize") Integer newPageSize, @BindingParam("commandQueue") String commandQueue) {
        // di default ci interessa solo pagesize, il parametro commandQueue è li per permettere a discendenti che devono
        // avere multipli widget di paginazione per ViewModel di sovrascrivere questi metodi e gestire il refresh/paginazione del widget giusto
        if(commandQueue!=defaultCommandQueue)
            return
        onChangePageSize(newPageSize)
    }

    void onChangePageSize(Integer newPageSize) {
        // di default ci interessa solo pagesize, il parametro commandQueue è li per permettere a discendenti che devono
        // avere multipli widget di paginazione per ViewModel di sovrascrivere questi metodi e gestire il refresh/paginazione del widget giusto
        this.pageSize = newPageSize
    }


    @Command
    @GlobalCommand
    void onChangePageSizePoiPagina(@BindingParam("newPageSize") Integer newPageSize, @BindingParam("commandQueue") String commandQueue) {
        // di default ci interessa solo pagesize, il parametro commandQueue è li per permettere a discendenti che devono
        // avere multipli widget di paginazione per ViewModel di sovrascrivere questi metodi e gestire il refresh/paginazione del widget giusto
        if(commandQueue!=defaultCommandQueue)
            return
        onChangePageSizePoiPagina(newPageSize)
    }

    void onChangePageSizePoiPagina(Integer newPageSize) {
        // di default ci interessa solo pagesize, il parametro commandQueue è li per permettere a discendenti che devono
        // avere multipli widget di paginazione per ViewModel di sovrascrivere questi metodi e gestire il refresh/paginazione del widget giusto
        onChangePageSize(newPageSize)
        onPagina()
    }

    // metodi obbligatori: onPagina e onRefresh devono per forza essere specificati da view model figli.
    @Override
    @Command
    abstract void onPagina()

    @Override
    @Command
    abstract void onRefresh()

    // metodi opzionali: tutti gli altri metodi di AfcAbstractGrid sono rilevanti solo per alcuni tipi di view model figli,
    // quindi qua li overridiamo senza fare nulla (per evitare comportamenti non voluti).
    // Poi se ai figli servono se li possono implementare loro.
    @Override
    @Command
    void onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord) {}

    @Override
    @Command
    void onElimina() {}

    @Override
    @Command
    void onVisualizzaTutti() {}

    @Override
    @Command
    void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {}

    @Override
    @Command
    void onCancelFiltro() {}

    @Override
    @Command
    void onExportExcel() {}
}

//Do nothing class, se qualcuno deve referenziare il PaginatedViewModel
class DefaultPaginatedViewModel extends PaginatedViewModel {

    @Override
    void onPagina() {}

    @Override
    void onRefresh() {}
}

