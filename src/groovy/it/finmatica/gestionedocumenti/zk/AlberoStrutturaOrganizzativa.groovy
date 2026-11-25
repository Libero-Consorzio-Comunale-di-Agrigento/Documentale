package it.finmatica.gestionedocumenti.zk

import org.zkoss.zul.AbstractTreeModel

/**
 * Classe di data-model per la gestione dell'albero di so4.
 * Features:
 * - Ricerca per Unità / Componente
 * - Differenziazione delle unità (senza componenti, con componenti con un certo ruolo, senza componenti con un certo ruolo)
 * - Differenziazione dei componenti (con un certo ruolo, senza un certo ruolo)
 * - Componenti visibili o meno a seconda di un parametro
 *
 * Utilizzo:
 * In uno zul, definire un tree come segue:
 *   <tree model="@load(vm.alberoSo4)" height="100%" width="100%" vflex="1">
 <template name="model">
 <treeitem open="@load((each.livello lt vm.livelloApertura))" image="@load(c:cat('/images/ags/16x16/', each.icona))">
 <treerow>
 <treecell label="@load(each.denominazione)" />
 <treecell label="@load(each.caricato)" />
 <treecell label="@load(each.unita)" />
 </treerow>
 </treeitem>
 </template>
 </tree>
 * La parte riguardante il livelloApertura è necessaria per determinare a quale profondità mostrare l'albero in fase di primo caricamento.
 * È necessario fare in questo modo siccome ZK visiterà ogni nodo ritornato dal data-model forzando il caricamento dei nodi anche di livello più profondo. Spostando la gestione della visibilità nel viewmodel
 * si aggira questo problema.
 *
 * Il componente <tree> di zk, richiamerà i metodi di questa classe in questo ordine per ogni nodo:
 * - isLeaf
 * - getChildCount
 * - getChild
 *
 * Quindi bisogna predisporre il caricamento di ulteriori figli in alcuni di questi metodi.
 *
 * Created by esasdelli on 29/03/2017.
 */
class AlberoStrutturaOrganizzativa extends AbstractTreeModel<AlberoStrutturaOrganizzativaNodo> {

    private List<Integer> mostraComponentiPerUnita
    private String ruoloDiscriminante   = null  // ruolo ad4 con cui cercare i soggetti nelle unità organizzative
    private String filtroRicerca        = null
    private String codiceOttica         = null

    /**
     * Costruisce un albero di struttura organizzativa in cui si vedano e siano ricercabili solo i componenti delle unità con i progressivi specificati.
     *
     * @param codiceOttica
     * @param mostraComponentiPerUnita
     * @param ruoloDiscriminante
     * @param filtroRicerca
     */
    AlberoStrutturaOrganizzativa (String codiceOttica, List<Long> mostraComponentiPerUnita, String ruoloDiscriminante, String filtroRicerca, List<Long> radici = null) {
        super (AlberoStrutturaOrganizzativaNodo.getRoot (codiceOttica, mostraComponentiPerUnita, ruoloDiscriminante, filtroRicerca, radici))
        this.codiceOttica       = codiceOttica
        this.mostraComponentiPerUnita   = mostraComponentiPerUnita
        this.ruoloDiscriminante = ruoloDiscriminante
        this.filtroRicerca      = filtroRicerca
    }

    /**
     * Costruisce un albero di struttura organizzativa in cui si vedano e siano ricercabili tutti i componenti di tutte le unità oppure nessun componente di nessuna unità.
     *
     * @param codiceOttica
     * @param mostraTuttiComponenti
     * @param ruoloDiscriminante
     * @param filtroRicerca
     */
    AlberoStrutturaOrganizzativa (String codiceOttica, boolean mostraTuttiComponenti, String ruoloDiscriminante, String filtroRicerca, List<Long> radici = null) {
        this(codiceOttica, mostraTuttiComponenti ? [AlberoStrutturaOrganizzativaNodo.TUTTE_UNITA] : [], ruoloDiscriminante, filtroRicerca, radici)
    }

    /**
     * Ritorna false se il nodo non è già stato caricato oppure se il nodo è di tipo "COMPONENTI" (quindi il nodo radice dei componenti) oppure se ha altri nodi figli.
     * Ritorna true in tutti gli altri casi
     */
    @Override
    boolean isLeaf (AlberoStrutturaOrganizzativaNodo nodo) {

        // in fase di reload dell'albero, succede che zk passi null a questa funzione. In tal caso, ritorno true per evitare un NullPointerException.
        if (nodo == null) {
            return true
        }

        // ritorno false perché siccome non ho ancora ultimato di caricare il nodo, non posso sapere se questo avrà figli o no,
        // quindi per "sicurezza" dico che ha figli così che possa comparire la freccia di espansione del nodo
        if (!nodo.caricato) {
            return false
        }

        // il nodo "COMPONENTI" è il nodo che contiene i vari componenti appartenenti all'unità.
        if (nodo.tipoNodo == "COMPONENTI") {
            return false
        }

        if (nodo.nodi.size() > 0) {
            return false
        }

        return true
    }

    /**
     * Questa funzione ritorna il numero di nodi figli, inoltre carica gli eventuali figli del nodoPadre se questi non sono già stati caricati.
     */
    @Override
    int getChildCount (AlberoStrutturaOrganizzativaNodo nodo) {
        if (!nodo.caricato && (filtroRicerca == null || filtroRicerca.trim().length() == 0)) {
            nodo.caricaNodi(mostraComponentiPerUnita, true, ruoloDiscriminante)
        }
        return nodo.nodi?.size()?:0
    }

    /**
     * Questa funzione ritorna il nodo figlio richiesto, inoltre carica gli eventuali figli del nodoPadre se questi non sono già stati caricati.
     *
     * @param nodoPadre
     * @param indiceFiglio
     * @return
     */
    @Override
    AlberoStrutturaOrganizzativaNodo getChild (AlberoStrutturaOrganizzativaNodo nodoPadre, int indiceFiglio) {
        if (!nodoPadre.caricato && (filtroRicerca == null || filtroRicerca.trim().length() == 0)) {
            nodoPadre.caricaNodi(mostraComponentiPerUnita, true, ruoloDiscriminante)
        }
        AlberoStrutturaOrganizzativaNodo child =  nodoPadre.getNodi()[indiceFiglio]
        return child
    }
}
