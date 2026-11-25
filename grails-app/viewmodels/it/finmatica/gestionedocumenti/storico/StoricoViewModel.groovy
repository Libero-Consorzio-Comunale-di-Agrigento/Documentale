package it.finmatica.gestionedocumenti.storico
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.documenti.beans.FileDownloader
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zul.DefaultTreeModel
import org.zkoss.zul.DefaultTreeNode

@VariableResolver(DelegatingVariableResolver)
class StoricoViewModel {

    @WireVariable private DocumentoStoricoService documentoStoricoService
    @WireVariable private FileDownloader fileDownloader

    Long idDocumento
    String prefissoLabel
    def filtri
    def filtroSelezionato

    DefaultTreeModel<DefaultTreeNode<DatoStorico>> datiStorici = null
    List<DatoStorico> risultatoStorico = []
    Date ricercaDal
    Date ricercaAl

    @Init init (@ExecutionArgParam("idDocumento") Long idDocumento, @ExecutionArgParam("prefissoLabel") String prefissoLabel, @ExecutionArgParam("filtri")def filtri) {
        this.idDocumento    = idDocumento
        this.prefissoLabel  = prefissoLabel
        filtroSelezionato   = [codice:"_TUTTI", titolo:"-- Tutti i Campi --", descrizione:"Mostra tutti i campi", filtri:[".*"]]
        this.filtri         = [filtroSelezionato]
        this.filtri.addAll(filtri)
    }

    private DefaultTreeModel<DefaultTreeNode<DatoStorico>> creaTreeNode (List<DatoStorico> datiStorici) {
        List<DefaultTreeNode<DatoStorico>> treeNodes = new ArrayList<>()
        for (DatoStorico datoStorico : datiStorici) {
            treeNodes << new DefaultTreeNode<DatoStorico>(datoStorico, datoStorico.datiStorici.collect { new DefaultTreeNode<DatoStorico>(it) })
        }
        return new DefaultTreeModel<DefaultTreeNode<DatoStorico>>(new DefaultTreeNode<DatoStorico>(null, treeNodes), true)
    }

    @NotifyChange(["datiStorici", "risultatoStorico"])
    @Command onRicerca () {
        risultatoStorico = documentoStoricoService.ricercaStorico (idDocumento, ricercaDal, ricercaAl)
        risultatoStorico = documentoStoricoService.filtraDatiStorici (risultatoStorico, filtroSelezionato.filtri)

        datiStorici = creaTreeNode(risultatoStorico)

        BindUtils.postNotifyChange(null, null, this, "datiStorici")
        BindUtils.postNotifyChange(null, null, this, "risultatoStorico")
    }

    @Command onDownloadFileStorico (@BindingParam("nodo")DefaultTreeNode<DatoStorico> nodo) {
        fileDownloader.downloadFileStorico (nodo.data.dati._idDocumentoEsterno, nodo.parent.data.revisione, nodo.data.dati._value)
    }
}
