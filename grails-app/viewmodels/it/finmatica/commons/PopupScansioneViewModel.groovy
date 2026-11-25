package it.finmatica.commons
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
class PopupScansioneViewModel {

    Window self
    String urlScansione

    @NotifyChange("urlScansione")
    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("urlScansione") String urlScansione) {
        this.self = w
        this.urlScansione = urlScansione
    }

    @Command
    void onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, false)
    }
}
