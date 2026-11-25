package it.finmatica.gestionedocumenti.zk

import it.finmatica.afc.PaginatedViewModel
import org.zkoss.bind.BindUtils
import org.zkoss.zk.ui.annotation.ComponentAnnotation
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.A
import org.zkoss.zul.Cell
import org.zkoss.zul.Div
import org.zkoss.zul.Grid
import org.zkoss.zul.Label
import org.zkoss.zul.Row
import org.zkoss.zul.Rows

/**
 * Aggiunge un footer alle lista paginated in cui ci sono le opzioni di paginazione (grandezza della pagina) e numero dei record totali trovati.<br>
 * <br>Quando si aggiunge il component a uno zul si DEVE indicare:
 * <br>- totalSize: nome del campo del view model che contiene il numero totale dei records
 * <br>- pageSize: nome del campo del view model che contiene la dimensione della pagina corrente
 * <br>- pageSizeOpzioni: nome del campo del view model che contiene tutte le opzioni per la dimensione delle pagine
 * <br><br>Quando si aggiunge il component a uno zul si possono indicare:
 * <br>- pageSizeLabel:     label della dimensione pagina (default: "Per pagina:")
 * <br>- recordsLabel:      label del numero di records (default: "Trovati:")
 */
@VariableResolver(DelegatingVariableResolver)
@ComponentAnnotation(['pageSizeOpzioni:@ZKBIND(ACCESS=both)',
        'pageSize:@ZKBIND(ACCESS=both)',
        'totalSize:@ZKBIND(ACCESS=load)'])
class PaginatedListFooter extends Div {

    Integer pageSize
    Map<Integer, String> pageSizeOpzioni
    String commandQueue

    private Label recordsLabelComponent
    private Label pageSizeLabelComponent
    private Label totalSizeComponent
    private Cell cellLeft

    PaginatedListFooter() {
        // struttura della ui
        def grid = new Grid(hflex: "1")
        def rows = new Rows()
        def row = new Row()
        cellLeft = new Cell(align: "left")
        pageSizeLabelComponent = new Label()
        def cellRight = new Cell(align: "right")
        recordsLabelComponent = new Label()
        totalSizeComponent = new Label(style: "font-weight:bold")

        cellLeft.appendChild(pageSizeLabelComponent)
        cellRight.appendChild(recordsLabelComponent)
        cellRight.appendChild(totalSizeComponent)

        row.appendChild(cellLeft)
        row.appendChild(cellRight)
        rows.appendChild(row)
        grid.appendChild(rows)
        this.appendChild(grid)

        // wire variables
        Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Div))
        Selectors.wireComponents(this, this, false)
        Selectors.wireEventListeners(this, this)

        // default values per le labels e properties, che l'utente può modificare
        recordsLabel = "Trovati:"
        pageSizeLabel = "Per pagina:"
        totalSize = 0
    }

    String getPageSizeLabel() {
        return pageSizeLabelComponent.value
    }

    void setPageSizeLabel(String pageSizeLabel) {
        this.pageSizeLabelComponent.value = pageSizeLabel?.trim()+" "
    }

    String getRecordsLabel() {
        return recordsLabelComponent.value
    }

    void setRecordsLabel(String recordsLabel) {
        this.recordsLabelComponent.value = recordsLabel?.trim()+" "
    }

    String getTotalSize() {
        return totalSizeComponent.value
    }

    void setTotalSize(String totalSize) {
        this.totalSizeComponent.value = totalSize
    }

    Map<Integer, String> getPageSizeOpzioni() {
        return pageSizeOpzioni
    }

    void setPageSizeOpzioni(Map<Integer, String> pageSizeOpzioni) {
        this.pageSizeOpzioni = pageSizeOpzioni

        // repaint della lista delle opzioni
        onChangePageSize()
    }

    Integer getPageSize() {
        return pageSize
    }

    void setPageSize(Integer pageSize, String event = PaginatedViewModel.ON_CHANGE_PAGE_SIZE) {
        // serve avere una escape condition così non ci rimpalliamo all'infinito i setPageSize con il ViewModel dato che pageSize è in @bind
        if (this.pageSize == pageSize)
            return

        this.pageSize = pageSize

        // postiamo al view model che la pageSize è cambiata (di default senza richiedere una paginazione)
        BindUtils.postGlobalCommand(null, null, event, [newPageSize: pageSize, commandQueue: commandQueue])
    }

    String getCommandQueue() {
        return commandQueue
    }

    void setCommandQueue(String commandQueue) {
        this.commandQueue = commandQueue
    }

    void onChangePageSize() {
        cellLeft.children.clear()
        cellLeft.appendChild(pageSizeLabelComponent)

        pageSizeOpzioni.each { size, style ->
            def selectableOption = new A(label: size, style: style)
            selectableOption.onClick {
                // on click scatta setPageSize, che aggiorna il view model con un GlobalCommand che informa che la pageSize è cambiata e richiede una nuova paginazione,
                // poi (prima dell'eventuale paginazione) il viewmodel aggiorna pageSizeOpzioni, questo a sua volta triggera il setPageSizeOpzioni del component tramite il @bind di zk
                setPageSize(size, PaginatedViewModel.ON_CHANGE_PAGE_SIZE_POI_PAGINA)
            }
            cellLeft.appendChild(selectableOption)
            cellLeft.appendChild(new Label(value: " "))
        }
    }

}