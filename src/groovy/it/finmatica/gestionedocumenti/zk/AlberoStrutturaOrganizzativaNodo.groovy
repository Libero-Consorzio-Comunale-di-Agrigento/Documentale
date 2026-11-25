package it.finmatica.gestionedocumenti.zk

import it.finmatica.gestionedocumenti.commons.StrutturaOrganizzativaService
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.as4.As4SoggettoCorrenteDTO

/**
 * Questa classe rappresenta un nodo nell'albero di so4.
 * Fornisce inoltre diversi metodi per la costruzione e la ricerca di tale albero.
 *
 * Ogni nodo può rappresentare:
 * - nodo radice -> non ha componenti e non ha una unità ma ha solo nodi figli
 * - nodo unità -> ha l'unità valorizzata e può avere un elenco di nodi figli
 * - nodo componenti -> è il nodo che raccoglie i componenti che appartengono ad una unità. Non ha nè una unità nè un componente associato, ma ha solo nodi figli (che sono solo componenti)
 * - nodo componente -> è il nodo che rappresente un componente che appartiene ad una unità. Ha i campi soggetto e componente valorizzati, non ha nodi figli.
 *
 * Created by esasdelli on 29/03/2017.
 */
class AlberoStrutturaOrganizzativaNodo {

    public static final int TUTTE_UNITA = -1

    // dati del componente, se questo nodo rappresente
    As4SoggettoCorrenteDTO soggetto
    So4ComponentePubbDTO componente
    boolean conRuolo

    List<AlberoStrutturaOrganizzativaNodo> nodi = []
    So4UnitaPubbDTO unita
    int livello

    private boolean componentiCaricati = false
    private boolean unitaCaricate = false

    boolean unitaConComponentiAbilitati = false
    boolean unitaSenzaComponenti = false
    boolean unitaSenzaComponentiAbilitati = false

    String getDenominazione() {
        if (isComponente()) {
            return soggetto.denominazione
        }

        if (isUnita()) {
            if(Impostazioni.UNITA_CONCAT_CODICE.abilitato) {
                return unita.codice.concat(" - ").concat(unita.descrizione)
            } else {
                return unita.descrizione
            }
        }

        return "Componenti"
    }

    String getIcona() {
        if (isComponente()) {
            return conRuolo ? "componente_abilitato.png" : "componente_non_abilitato.png"
        }

        if (isUnita()) {
            if (unitaConComponentiAbilitati) {
                return "unita_con_componenti_abilitati.png"
            }

            if (unitaSenzaComponenti) {
                return "unita_senza_componenti.png"
            }

            return "unita_senza_componenti_abilitati.png"

        }

        return "componenti.png"
    }

    String getTipoNodo() {
        if (isComponente()) {
            return "COMPONENTE"
        }

        if (isUnita()) {
            return "UO"
        }

        return "COMPONENTI"
    }

    /**
     * Indica se il nodo è stato "pienamente" caricato.
     * Il nodo infatti può essere "parzialmente" caricato se ne sono state caricate solo le unità figlie o solo i componenti.
     * Questa è una ottimizzazione per evitare di caricare dati quando non necessario.
     *
     * @return
     */
    boolean isCaricato() {
        return isComponente() || (unitaCaricate && componentiCaricati)
    }

    /**
     * Carica i nodi figli di questo nodo.
     *
     * @param caricaComponenti indica se caricare i componenti
     * @param caricaUnita indica se caricare le unità
     * @param ruoloDiscriminante indica il ruolo con cui discriminare un componente abilitato da uno non abilitato.
     */
    void caricaNodi(List<Long> caricaComponenti, boolean caricaUnita, String ruoloDiscriminante) {
        if (isComponente()) {
            return
        }

        if (!componentiCaricati && unita != null && (caricaComponenti.contains(TUTTE_UNITA) || caricaComponenti.contains(unita.progr))) {
            componentiCaricati = true

            def componentiUnita = getComponentiUnita(unita, ruoloDiscriminante)

            unitaSenzaComponenti = componentiUnita.size() == 0
            unitaConComponentiAbilitati = componentiUnita.count { it.conRuolo } > 0
            unitaSenzaComponentiAbilitati = !unitaConComponentiAbilitati && !unitaSenzaComponenti

            if (componentiUnita.size() > 0) {
                // il nodo dei componenti deve essere il primo della lista.
                nodi.add(0, creaNodoComponente(componentiUnita, livello + 1))
            }
        }

        if (caricaUnita && !unitaCaricate && unita != null) {
            unitaCaricate = true

            def unita = getUnitaFiglie(unita.progr, unita.ottica.codice, ruoloDiscriminante)
            nodi.addAll(creaNodiUnita(livello + 1, unita))
        }
    }

    boolean isComponente() {
        if (componente != null) {
            return true
        }

        return false
    }

    boolean isUnita() {
        if (unita != null) {
            return true
        }

        return false
    }

    /**
     * Carica la radice dell'albero con i parametri richiesti
     *
     * @param codiceOttica il codice dell'ottica su cui cercare le unità
     * @param caricaComponenti indica se i componenti vanno caricati o no.
     * @param ruoloDiscriminante indica il ruolo con cui distinguere i componenti abilitati da quelli non abilitati
     * @param filtroRicerca il filtro di ricerca da utilizzare. La ricerca viene effettuata solo se questo filtro non è nullo ed ha una lunghezza > 0
     * @return la radice dell'albero da mostrare
     */
    static AlberoStrutturaOrganizzativaNodo getRoot(String codiceOttica, List<Long> caricaComponentiUnita, String ruoloDiscriminante, String filtroRicerca, List<Long> radici = null) {
        AlberoStrutturaOrganizzativaNodo root = new AlberoStrutturaOrganizzativaNodo()
        if (filtroRicerca?.trim()?.length() > 0) {
            root.nodi = ricerca(codiceOttica, caricaComponentiUnita, ruoloDiscriminante, filtroRicerca, radici)
        } else {
            if (radici?.size() > 0) {
                for (int i = 0; i < radici.size(); i++) {
                    root.nodi << creaNodoUnita(0, getUnitaConProgr(radici[i], codiceOttica, ruoloDiscriminante))
                }
            } else {
                root.nodi = creaNodiUnita(0, getUnitaFiglie(0, codiceOttica, ruoloDiscriminante))
            }
            root.nodi*.caricaNodi(caricaComponentiUnita, false, ruoloDiscriminante)
        }

        return root
    }

    /**
     * Effettua una ricerca su so4 cercando unità e componenti con il filtro richiesto.
     * Per ogni componente trovato, verrà mostrato il ramo di so4 in cui è presente.
     * Per ogni unità trovata, verrà mostrato il ramo in cui si trova oltre che al suo elenco di componenti.
     *
     * @param codiceOttica il codice dell'ottica su cui cercare le unità
     * @param caricaComponenti indica se i componenti vanno caricati o no.
     * @param ruoloDiscriminante indica il ruolo con cui distinguere i componenti abilitati da quelli non abilitati
     * @param filtroRicerca il filtro di ricerca da utilizzare. La ricerca viene effettuata solo se questo filtro non è nullo ed ha una lunghezza > 0
     * @return la radice dell'albero da mostrare
     */
    private
    static List<AlberoStrutturaOrganizzativaNodo> ricerca(String codiceOttica, List<Long> caricaComponentiUnita, String ruoloDiscriminante, String filtroRicerca, List<Long> radici = null) {
        // prima cerco tra unità con il filtro di ricerca
        Map mappaUnita = cercaUnita(codiceOttica, ruoloDiscriminante, filtroRicerca).collectEntries {
            [(it.unita.progr): it]
        }
        List<So4UnitaPubbDTO> listaUnita = mappaUnita.values().unita
        if(radici?.size()>0){

            for (So4UnitaPubbDTO unita : listaUnita) {
                boolean unitaInStruttura = true
                So4UnitaPubbDTO unitaAppoggio = unita
                //trovo radice di unita
                while (unitaAppoggio != null && unitaAppoggio.progrPadre > 0) {
                    So4UnitaPubb padre = unitaAppoggio.domainObject.getUnitaPubbPadre()
                    if (padre != null) {
                        unitaAppoggio = padre?.toDTO()
                    }
                }
                //verifico se la radice di unita è in radici
                if(!radici.contains(unitaAppoggio.progr)){
                    unitaInStruttura = false
                }
                //se la radice di unita non è in radici, tolgo unita da mappaUnita
                if (!unitaInStruttura) {
                    mappaUnita.remove(unita.progr)
                }
            }
        }


        listaUnita = mappaUnita.values().unita
        // per ogni unità, controllo se ho già il suo padre:
        for (def unita : listaUnita) {
            caricaPadre(unita.progrPadre, unita.ottica.codice, mappaUnita, ruoloDiscriminante, radici)
        }

        // cerco tra i componenti se richiesto
        def listaComponenti = cercaComponenti(codiceOttica, caricaComponentiUnita, ruoloDiscriminante, filtroRicerca)
        // per ogni componente, carico l'unità padre.
        for (def componente : listaComponenti) {
            caricaPadre(componente.componente.progrUnita, componente.componente.ottica.codice, mappaUnita, ruoloDiscriminante, radici)
        }

        return creaAlberoRicerca(mappaUnita, listaComponenti, caricaComponentiUnita, null, 0, ruoloDiscriminante, radici)
    }

    private static List<AlberoStrutturaOrganizzativaNodo> creaAlberoRicerca(Map mappaUnita,
                                                                            listaComponenti, List<Long> caricaComponentiUnita, Long uoPadre, int livello, String ruoloDiscriminante, List<Long> radici = null) {
        def uoRadici = mappaUnita.values().findAll { it.unita.progrPadre == uoPadre || radici?.contains(it.unita.progr) }.sort {
            it.unita.descrizione
        }.sort { it.unita.sequenza }

        List<AlberoStrutturaOrganizzativaNodo> nodi = creaNodiUnita(livello, uoRadici)

        for (AlberoStrutturaOrganizzativaNodo nodo : nodi) {
            def componentiUo = listaComponenti.findAll { it.componente.progrUnita == nodo.unita.progr }
            if (componentiUo.size() > 0) {
                nodo.nodi << creaNodoComponente(componentiUo, livello + 1)
            } else if (mappaUnita[nodo.unita.progr].unitaTrovata) {
                // carico i componenti dell'unità solo se questa è una delle unità trovate dalla ricerca.
                // potrebbe infatti essere una unità "padre" caricata successivamente, di queste non bisogna mostrare i componenti
                nodo.caricaNodi(caricaComponentiUnita, false, ruoloDiscriminante)
            }
            nodo.nodi.addAll(creaAlberoRicerca(mappaUnita, listaComponenti, caricaComponentiUnita, nodo.unita.progr, livello + 1, ruoloDiscriminante))
        }

        return nodi
    }

    /**
     * Carica ricorsivamente l'unità padre di quella richiesta.
     * Se il padre è già stato caricato e presente in mappaUnita
     *
     * @param progrUnita il progressivo dell'unità da caricare
     * @param codiceOttica il codice dell'ottica dell'albero
     * @param mappaUnita l'elenco delle unità già caricate indicizzate per progressivo
     * @param ruoloDiscriminante il ruolo per determinare se gli utenti sono abilitati o non abilitati.
     */
    private
    static void caricaPadre(Long progrUnita, String codiceOttica, Map mappaUnita, String ruoloDiscriminante, List<Long> radici = null) {
        if (progrUnita == null) {
            return
        }

        if (mappaUnita[progrUnita] != null) {
            return
        }

        def unita = getUnitaConProgr(progrUnita, codiceOttica, ruoloDiscriminante)
        if(unita == null){
            return
        }
        mappaUnita[progrUnita] = unita

        if (radici?.contains(progrUnita)) {
            return
        }
        caricaPadre(unita.unita.progrPadre, unita.unita.ottica.codice, mappaUnita, ruoloDiscriminante, radici)
    }

    private static getUnitaConProgr(long progr, String codiceOttica, String ruoloDiscriminante) {
        return So4UnitaPubb.executeQuery("""select unita
     , (select count(ruolo.id) from So4RuoloComponentePubb ruolo where ruolo.componente.progrUnita = unita.progr and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date))) and ruolo.ruolo.ruolo   = :ruoloDiscriminante) as N_COMPONENTI_ABILITATI
     , (select count(ruolo.id) from So4RuoloComponentePubb ruolo where ruolo.componente.progrUnita = unita.progr and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date)))) as N_COMPONENTI
  from So4UnitaPubb unita
 where unita.dal <= trunc(current_date)
   and (unita.al is null or unita.al >= trunc(current_date))
   and unita.progr = :progr
   and unita.ottica.codice = :codiceOttica
 order by unita.sequenza asc, unita.descrizione asc""", [progr: progr, codiceOttica: codiceOttica, ruoloDiscriminante: ruoloDiscriminante]).collect { row ->
            [unita: row[0].toDTO(), unitaConComponentiAbilitati: (row[1] > 0), unitaSenzaComponenti: (row[2] == 0), unitaSenzaComponentiAbilitati: (row[1] == 0)]
        }[0]
    }

    private static List cercaUnita(String codiceOttica, String ruoloDiscriminante, String filtroRicerca) {
        return So4UnitaPubb.executeQuery("""select unita
     , (select count(ruolo.id) from So4RuoloComponentePubb ruolo where ruolo.componente.progrUnita = unita.progr and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date))) and ruolo.ruolo.ruolo   = :ruoloDiscriminante) as N_COMPONENTI_ABILITATI
     , (select count(ruolo.id) from So4RuoloComponentePubb ruolo where ruolo.componente.progrUnita = unita.progr and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date)))) as N_COMPONENTI
  from So4UnitaPubb unita
 where unita.dal <= trunc(current_date)
   and (unita.al is null or unita.al >= trunc(current_date))
   and ( upper(unita.descrizione) like upper(:filtro) or ( upper(unita.codice) like upper(:filtro)) )
   and unita.ottica.codice = :codiceOttica
 order by unita.sequenza asc, unita.descrizione asc""", [filtro: "%${filtroRicerca}%", codiceOttica: codiceOttica, ruoloDiscriminante: ruoloDiscriminante]).collect { row ->
            [unita: row[0].toDTO(), unitaConComponentiAbilitati: (row[1] > 0), unitaSenzaComponenti: (row[2] == 0), unitaSenzaComponentiAbilitati: (row[1] == 0), unitaTrovata: true]
        }
    }

    private
    static List cercaComponenti(String codiceOttica, List<Long> caricaComponentiUnita, String ruoloDiscriminante, String filtroRicerca) {

        // se la lista dei progressivi delle unità è vuota, allora non devo caricare nessun componente.
        if (caricaComponentiUnita.size() == 0) {
            return []
        }

        // se la lista dei progressivi delle unità contiene il valore "TUTTE LE UNITA" allora devo cercare attraverso tutti i componenti di tutte le unità:
        if (caricaComponentiUnita.contains(TUTTE_UNITA)) {
            return So4ComponentePubb.executeQuery("""select 
           componente
         , componente.soggetto
         , (select 1 from So4RuoloComponentePubb ruolo where ruolo.componente = componente and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date))) and ruolo.ruolo.ruolo   = :ruoloDiscriminante and rownum = 1) as RUOLO
         , (select max(1) from So4AttrComponentePubb  attr  where attr.componente  = componente and (attr.dal  <= trunc(current_date) and (attr.al  is null or attr.al  >= trunc(current_date))) and attr.seResponsabile = true)  as RESP
      from So4ComponentePubb 		componente
     where componente.dal <= trunc(current_date)
       and (componente.al is null or componente.al >= trunc(current_date))
       and (upper(componente.soggetto.nome   ||' '||componente.soggetto.cognome) like upper(:filtroRicerca)
         or upper(componente.soggetto.cognome||' '||componente.soggetto.nome)    like upper(:filtroRicerca))
       and componente.ottica.codice = :codiceOttica
     order by RESP asc, componente.soggetto.denominazione
    """, [filtroRicerca: "%${filtroRicerca}%", codiceOttica: codiceOttica, ruoloDiscriminante: ruoloDiscriminante]).collect { row ->
                [componente: row[0].toDTO(), soggetto: row[1].toDTO(), conRuolo: row[2] == 1]
            }
        }

        // altrimenti, cerco i componenti per le unità richieste
        return So4ComponentePubb.executeQuery("""select 
           componente
         , componente.soggetto
         , (select 1 from So4RuoloComponentePubb ruolo where ruolo.componente = componente and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date))) and ruolo.ruolo.ruolo   = :ruoloDiscriminante and rownum = 1) as RUOLO
         , (select max(1) from So4AttrComponentePubb  attr  where attr.componente  = componente and (attr.dal  <= trunc(current_date) and (attr.al  is null or attr.al  >= trunc(current_date))) and attr.seResponsabile = true)  as RESP
      from So4ComponentePubb 		componente
     where componente.dal <= trunc(current_date)
       and (componente.al is null or componente.al >= trunc(current_date))
       and componente.progrUnita in (:progrUnita)
       and (upper(componente.soggetto.nome   ||' '||componente.soggetto.cognome) like upper(:filtroRicerca)
         or upper(componente.soggetto.cognome||' '||componente.soggetto.nome)    like upper(:filtroRicerca))
       and componente.ottica.codice = :codiceOttica
     order by RESP asc, componente.soggetto.denominazione
    """, [filtroRicerca: "%${filtroRicerca}%", codiceOttica: codiceOttica, ruoloDiscriminante: ruoloDiscriminante, progrUnita: caricaComponentiUnita]).collect { row ->
            [componente: row[0].toDTO(), soggetto: row[1].toDTO(), conRuolo: row[2] == 1]
        }
    }

    private static List getUnitaFiglie(long progrPadre, String codiceOttica, String ruoloDiscriminante) {
        return So4UnitaPubb.executeQuery("""select unita
     , (select count(ruolo.id) from So4RuoloComponentePubb ruolo where ruolo.componente.progrUnita = unita.progr and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date))) and ruolo.ruolo.ruolo   = :ruoloDiscriminante) as N_COMPONENTI_ABILITATI
     , (select count(componente.id) from So4ComponentePubb componente where componente.progrUnita = unita.progr and  componente.ottica.codice = :codiceOttica and (componente.dal <= trunc(current_date) and (componente.al is null or componente.al >= trunc(current_date)))) as N_COMPONENTI
  from So4UnitaPubb unita
 where unita.dal <= trunc(current_date) 
   and (unita.al is null or unita.al >= trunc(current_date))
   and coalesce(unita.progrPadre, 0) = :progrPadre
   and unita.ottica.codice = :codiceOttica
 order by unita.sequenza asc, unita.descrizione asc""", [progrPadre: progrPadre, codiceOttica: codiceOttica, ruoloDiscriminante: ruoloDiscriminante]).collect { row ->
            [unita: row[0].toDTO(), unitaConComponentiAbilitati: (row[1] > 0), unitaSenzaComponenti: (row[2] == 0), unitaSenzaComponentiAbilitati: (row[1] <= 0 && row[2] >0)]
        }
    }

    private static List getComponentiUnita(So4UnitaPubbDTO unita, String ruoloDiscriminante) {
        if (unita == null) {
            return []
        }

        return So4ComponentePubb.executeQuery("""select 
       componente
     , componente.soggetto
     , (select 1 from So4RuoloComponentePubb ruolo where ruolo.componente = componente and (ruolo.dal <= trunc(current_date) and (ruolo.al is null or ruolo.al >= trunc(current_date))) and ruolo.ruolo.ruolo   = :ruoloDiscriminante and rownum = 1) as RUOLO
     , (select max(1) from So4AttrComponentePubb  attr  where attr.componente  = componente and (attr.dal  <= trunc(current_date) and (attr.al  is null or attr.al  >= trunc(current_date))) and attr.seResponsabile = true)  as RESP
  from So4ComponentePubb 		componente
 where componente.dal <= trunc(current_date)
   and (componente.al is null or componente.al >= trunc(current_date))
   and componente.progrUnita    = :progrUnita
   and componente.ottica.codice = :codiceOttica
 order by RESP asc, componente.soggetto.denominazione
""", [progrUnita: unita.progr, codiceOttica: unita.ottica.codice, ruoloDiscriminante: ruoloDiscriminante]).collect { row ->
            [componente: row[0].toDTO(), soggetto: row[1].toDTO(), conRuolo: row[2] == 1]
        }
    }

    private static AlberoStrutturaOrganizzativaNodo creaNodoComponente(listaComponenti, int livello) {
        if (listaComponenti?.size() > 0) {
            AlberoStrutturaOrganizzativaNodo componenti = new AlberoStrutturaOrganizzativaNodo(livello: livello)
            componenti.nodi = []
            for (def soggetto : listaComponenti) {
                componenti.nodi << new AlberoStrutturaOrganizzativaNodo(componente: soggetto.componente, soggetto: soggetto.soggetto, conRuolo: soggetto.conRuolo, livello: Integer.MAX_VALUE)
            }

            return componenti
        }

        return null
    }

    private static List<AlberoStrutturaOrganizzativaNodo> creaNodiUnita(int livello,
                                                                        listaUnita) {
        List<AlberoStrutturaOrganizzativaNodo> nodi = []

        if (listaUnita?.size() > 0) {
            for (def row : listaUnita) {
                nodi << new AlberoStrutturaOrganizzativaNodo(unita: row.unita
                        , unitaConComponentiAbilitati: row.unitaConComponentiAbilitati
                        , unitaSenzaComponenti: row.unitaSenzaComponenti
                        , unitaSenzaComponentiAbilitati: row.unitaSenzaComponentiAbilitati
                        , livello: livello)
            }
        }

        return nodi
    }

    private static AlberoStrutturaOrganizzativaNodo creaNodoUnita(int livello,
                                                                  unita) {
        AlberoStrutturaOrganizzativaNodo nodo = []

        if (unita?.size() > 0) {
            nodo = new AlberoStrutturaOrganizzativaNodo(unita: unita.unita
                    , unitaConComponentiAbilitati: unita.unitaConComponentiAbilitati
                    , unitaSenzaComponenti: unita.unitaSenzaComponenti
                    , unitaSenzaComponentiAbilitati: unita.unitaSenzaComponentiAbilitati
                    , livello: livello)
        }

        return nodo
    }
}
