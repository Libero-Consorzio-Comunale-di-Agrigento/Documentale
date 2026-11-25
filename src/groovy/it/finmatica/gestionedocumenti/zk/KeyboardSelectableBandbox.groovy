package it.finmatica.gestionedocumenti.zk

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang.StringUtils
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.annotation.ComponentAnnotation
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zk.ui.event.OpenEvent
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Listen
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Bandbox
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Listbox
import org.zkoss.zul.Paging

@Slf4j
@CompileStatic
@VariableResolver(DelegatingVariableResolver)
@ComponentAnnotation('selectedItem:@ZKBIND(ACCESS=both, SAVE_EVENT=onSelectItem)')
abstract class KeyboardSelectableBandbox<T> extends Bandbox implements EventListener<Event> {

    public static final String ON_SELECTED_ITEM = 'onSelectItem'

    @Wire('listbox')
    private Listbox listbox

    @Wire('paging')
    private Paging paging

    private T item
    protected String filtro
    private final String componentZul
    private final int pageSize

    KeyboardSelectableBandbox(String componentZul) {
        this(componentZul, 10)
    }

    KeyboardSelectableBandbox(String componentZul, int pageSize) {
        this.pageSize = pageSize
        setAutodrop(true)
        setMold("rounded")
        this.componentZul = componentZul

        setWidgetListener('onOpen', '''
var listbox = zk.Widget.$(jq("@listbox", this));
if (event.open) {
    listbox.setSelectedIndex(-1);
} else {
    listbox.fireOnSelect(listbox);
}
''')

        setWidgetListener('onKeyUp', '''
        if (!this.isOpen()) {
            return;
        }
        var listbox = zk.Widget.$(jq("@listbox", this));
        if (listbox.lastItem === undefined) { // la listbox non ha figli, non faccio nulla
            return;
        }
        if (event.data.keyCode === 38) { // freccia su
            if (listbox.getSelectedIndex() > 0) {
                listbox.setSelectedIndex(listbox.getSelectedIndex()-1);
            } else {
                listbox.setSelectedIndex(listbox.lastItem.getChildIndex()-1);
            }
        } else if (event.data.keyCode === 40) { // freccia giu
            if ((listbox.getSelectedIndex() + 1) < listbox.lastItem.getChildIndex()) {
                listbox.setSelectedIndex(listbox.getSelectedIndex()+1);
            } else {
                listbox.setSelectedIndex(0);
            }
        }''')

        addEventListener(Events.ON_CHANGE, this)
        addEventListener(Events.ON_CHANGING, this)
        addEventListener(Events.ON_CREATE, this)
        addEventListener(Events.ON_OPEN, this)
    }

    protected abstract String getItemToString(T t);

    protected abstract PagedList<T> doSearch(String filtro, int offset, int max);

    protected Map<String, Object> getBandboxParams() {
        return [:]
    }

    final T getSelectedItem() {
        return item
    }

    final void setSelectedItem(T item) {
        this.item = item
        if (item != null) {
            setValue(getItemToString(item))
        }
    }

    @Listen('onPaging = paging')
    final void onPaging() {
        loadItems(filtro)
    }

    final void doSelectItem() {
        T value = listbox.getSelectedItem()?.getValue()
        if (value) {
            setSelectedItem(value)
            Events.postEvent(ON_SELECTED_ITEM, this, item)
        }
    }

    final void resetSelectItem() {
        setSelectedItem(listbox?.getItemAtIndex(0)?.getValue())
        Events.postEvent(ON_SELECTED_ITEM, this, item)
    }

    @Listen('onClick = listbox')
    final void onClickListbox() {
        // chiudo la bandpopup e rilancio l'evento che l'ho chiusa
        close()
        Events.postEvent(new OpenEvent(Events.ON_OPEN, this, false))
    }

    final void onEvent(Event event) throws Exception {
        switch (event.name) {
            case Events.ON_OPEN:
                OpenEvent openEvent = (OpenEvent) event
                if (openEvent.open) {
                    // se il campo è vuoto (es. dopo "svuota filtri") azzero il filtro anche se la listbox ha ancora una selezione precedente
                    if (StringUtils.isEmpty(value)) {
                        filtro = ""
                    }

                    listbox.setSelectedIndex(-1)
                    loadItems(this.filtro ?: "") // carico tutti i valori se non ho impostato il filtro

                } else {
                    doSelectItem()
                }
                break

        // questo evento scatta quando il componente perde il focus: devo aggiornare l'item selezionato.
            case Events.ON_CHANGE:

                if(StringUtils.isEmpty(((InputEvent) event).value)){
                    resetSelectItem()
                }
                else{
                    doSelectItem()
                }
                break

            case Events.ON_CHANGING:
                InputEvent inputEvent = (InputEvent) event
                loadItems(inputEvent.getValue())
                this.filtro = inputEvent.getValue()
                break
            case Events.ON_CREATE:
                Executions.createComponents(componentZul, this, getBandboxParams())
                Selectors.wireVariables(this, this, Selectors.newVariableResolvers(getClass(), Bandbox))
                Selectors.wireComponents(this, this, false)
                Selectors.wireEventListeners(this, this)

                // se non ho l'elemento "paging", ne creo uno arbitrario che non verrà visualizzato.
                if (paging == null) {
                    paging = new Paging()
                    paging.setPageSize(Integer.MAX_VALUE)
                    paging.setActivePage(0)
                }
                break

            default:
                break
        }
    }

    final protected void loadItems(String filtro) {

        if (filtro?.size() > 1 || StringUtils.isEmpty(filtro)) {
            PagedList items = doSearch(filtro, paging.getPageSize() * paging.getActivePage(), paging.getPageSize())

            paging.setTotalSize(items.totalCount)
            listbox.setModel(new ListModelList<?>(items))

            if (items.totalCount == 0) {
                // svuoto solo l'item corrente senza cambiare il "value" (cioè quello che ha scritto l'utente)
                this.item = null
            }
        }
    }
}
