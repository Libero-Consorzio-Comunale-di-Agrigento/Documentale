package it.finmatica.gestionedocumenti.test
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import it.finmatica.gestionedocumenti.zk.AlberoStrutturaOrganizzativa
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange

/**
 * Created by esasdelli on 30/03/2017.
 */
@VariableResolver(DelegatingVariableResolver)
class AlberoSo4ViewModel {

    AlberoStrutturaOrganizzativa alberoSo4

    int livelloApertura         = 1
    int livelloAperturaIniziale = 1

    String codiceOttica         = "1"
    String codiceRuolo          = "AGP"
    boolean mostraComponenti    = false
    String filtroRicerca        = null

    @Init
    void init () {
    }

    @NotifyChange(["alberoSo4", "livelloApertura"])
    @Command
    void onCerca () {
        if (filtroRicerca?.length() > 0) {
            // in caso di ricerca, mostro tutti i nodi aperti:
            livelloApertura = Integer.MAX_VALUE

        } else {
            // ripristino il livello di apertura di defalut
            livelloApertura = livelloAperturaIniziale
        }

        // carico l'albero dalla radice
//        alberoSo4 = new AlberoStrutturaOrganizzativa (codiceOttica, mostraComponenti, codiceRuolo, filtroRicerca)
        alberoSo4 = new AlberoStrutturaOrganizzativa (codiceOttica, [(long)203, (long)703], codiceRuolo, filtroRicerca)
    }
}
