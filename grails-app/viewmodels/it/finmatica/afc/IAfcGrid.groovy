package it.finmatica.afc

import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.zk.ui.event.Event

interface IAfcGrid {

    @Command
    void onPagina();

    @Command
    void onRefresh();

    @Command
    void onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord);

    @Command
    void onElimina();

    @Command
    void onVisualizzaTutti();

    @Command
    void onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event);

    @Command
    void onCancelFiltro();

    @Command
    void onExportExcel();

    @Command
    void onChiudi();
}
