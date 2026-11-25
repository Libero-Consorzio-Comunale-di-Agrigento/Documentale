package it.finmatica.gestionedocumenti.soggetti

import it.finmatica.gestionedocumenti.RegolaCalcolo
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gorm.criteria.PagedResultList
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.FetchMode
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.zkoss.zk.ui.util.Clients

@Service
@Transactional
class TipologiaSoggettoService {

    private @Autowired
    ApplicationContext applicationContext
    @Autowired
    SessionFactory sessionFactory

    /**
     * Ottiene la map dei soggetti dal documento.
     *
     * @param documento il documento di cui ottenere i soggetti.
     * @return la mappa dei soggetti.
     */
    def calcolaSoggettiDto(documento) {
        def soggetti = [:]

        for (DocumentoSoggetto soggetto : documento.soggetti) {

            // salto i soggetti non attivi: con le modifiche fatte per Treviso, c'è la possibilità che un soggetto possa essere presente più volte
            // ma può essercene uno solo attivo. Con questo controllo mi assicuro di prendere solo quello.
            if (!soggetto.attivo) {
                continue
            }
            //se l'utenteAd4 è null ci troviamo sicuramente un soggetto di categoria UNITA
            if (/*soggetto.tipoSoggetto.categoria == Categoria.COMPONENTE &&*/ soggetto.utenteAd4 != null) {
                soggetti[soggetto.tipoSoggetto] = [descrizione   : soggetto.utenteAd4.nominativoSoggetto
                                                   , unita       : soggetto.unitaSo4?.toDTO()
                                                   , utente      : soggetto.utenteAd4.toDTO()
                                                   , tipoSoggetto: soggetto.tipoSoggetto]
            } else if (/*soggetto.tipoSoggetto.categoria == Categoria.UNITA &&*/ soggetto.unitaSo4 != null) {
                soggetti[soggetto.tipoSoggetto] = [unita: soggetto.unitaSo4.toDTO(), tipoSoggetto: soggetto.tipoSoggetto]
            }
        }

        return soggetti
    }

    /**
     * Calcola tutti i soggetti del documento sulla base della sua tipologia.
     *
     * @param documento può essere una domain class o un dto (a seconda di quale momento sono per il calcolo).
     * @param tipologia la tipologia da calcolare.
     * @return i soggetti calcolati.
     */
    def calcolaSoggetti(documento, TipologiaSoggetto tipologiaSoggetto) {
        def soggetti = [:]
        def tipiSoggettoCalcolati = []

        // per calcolare tutti i soggetti, inizio da quelli senza dipendenze
        for (TipologiaSoggettoRegola tipoSoggettoRegola : tipologiaSoggetto.regole) {
            if (tipoSoggettoRegola != null && tipoSoggettoRegola?.tipoSoggettoPartenza == null) {
                def sogg = creaSoggetto(documento, tipoSoggettoRegola, soggetti)

                if (sogg != null) {
                    soggetti[tipoSoggettoRegola.tipoSoggetto] = sogg
                    tipiSoggettoCalcolati << tipoSoggettoRegola.tipoSoggetto
                }
            }
        }

        // FIXME: gestisco solo un livello di profondità (in teoria bisognerebbe andare in ricorsione etc...)
        // poi quelli con dipendenze
        for (TipologiaSoggettoRegola tipoSoggetto : tipologiaSoggetto.regole) {
            if (tipoSoggetto?.tipoSoggettoPartenza != null &&
                    tipiSoggettoCalcolati.contains(tipoSoggetto.tipoSoggettoPartenza)) {

                def sogg = creaSoggetto(documento, tipoSoggetto, soggetti)

                if (sogg != null) {
                    soggetti[tipoSoggetto.tipoSoggetto] = sogg
                    tipiSoggettoCalcolati << tipoSoggetto.tipoSoggetto
                }
            }
        }

        return soggetti
    }

    /**
     * Aggiorna i soggetti sulla base del soggetto modificato.
     *
     * TODO: gestisco solo un livello di profondità (in teoria bisognerebbe andare in ricorsione etc...)
     *
     * @param idTipologia id della tipologia che descrive i soggetti da ricalcolare
     * @param documento il documento proprietario dei soggetti.
     * @param soggetti i soggetti da ricalcolare
     * @param tipoSoggetto il tipo soggetto che è stato modificato e da cui partire per il ricalcolo.
     */
    void aggiornaSoggetti(long idTipologia, documento, soggetti, String tipoSoggetto, int activePage = 0, int pageSize = 10) {
        // ricalcolo tutti i soggetti tranne quello con tipoSoggetto
        List<TipologiaSoggettoRegola> tipologiaSoggettoRegole = TipologiaSoggettoRegola.createCriteria().list {
            eq("tipologiaSoggetto.id", idTipologia)

            not {
                'in'("tipoSoggetto", [tipoSoggetto])    // escludo il tipoSoggetto che ho già calcolato.
            }

            order("sequenza", "asc")

            fetchMode("tipoSoggetto", FetchMode.JOIN)
            fetchMode("tipoSoggettoPartenza", FetchMode.JOIN)
            fetchMode("regolaDefault", FetchMode.JOIN)
        }

        def tipiSoggetto = [tipoSoggetto]

        for (TipologiaSoggettoRegola tipologiaSoggettoRegola : tipologiaSoggettoRegole) {
            // eseguo il calcolo di default solo se questo tipo soggetto dipende da quello che è cambiato:
            if (tipiSoggetto.contains(tipologiaSoggettoRegola.tipoSoggettoPartenza)) {

                def sogg = creaSoggetto(documento, tipologiaSoggettoRegola, soggetti)

                if (sogg != null) {
                    // siccome questa regola di calcolo dipende dal tipoSoggetto iniziale, mi segno anche il suo tipoSoggetto, così da valutare anche le prossime.
                    soggetti[tipologiaSoggettoRegola.tipoSoggetto] = sogg
                    tipiSoggetto << tipologiaSoggettoRegola.tipoSoggetto
                }
            }
        }
    }

    def calcolaListaSoggetti(long idTipologia, documento, soggetti, String tipoSoggetto, String filtro, int offset = 0, int pageSize = Integer.MAX_VALUE) {
        TipologiaSoggettoRegola tipologiaSoggettoRegola = TipologiaSoggettoRegola.createCriteria().get {
            eq("tipologiaSoggetto.id", idTipologia)
            eq("tipoSoggetto", tipoSoggetto)

            order("sequenza", "asc")

            fetchMode("tipoSoggetto", FetchMode.JOIN)
            fetchMode("tipoSoggettoPartenza", FetchMode.JOIN)
            fetchMode("regolaLista", FetchMode.JOIN)
        }

        if (tipologiaSoggettoRegola == null || tipologiaSoggettoRegola.regolaLista == null) {
            throw new GestioneDocumentiRuntimeException("Non esiste la regola chiamata per questa tipologia soggetto: " + tipoSoggetto)
        }

        if(tipologiaSoggettoRegola?.regolaLista?.nomeBean == "-- nessuno --"){
            throw new GestioneDocumentiRuntimeException("Attezione nessuna regalo Calcolo Lista inserita, inserirne una per continuare")
        }
        def risultato = eseguiRegola(tipologiaSoggettoRegola.regolaLista, documento, soggetti, tipologiaSoggettoRegola.ruolo?.ruolo, tipologiaSoggettoRegola.tipoSoggettoPartenza, filtro)
        // issue https://redmine.svi.finmatica.local/issues/37955 : la lista non è paginata; non posso fare da DB perché delego alla regola il calcolo della lista
        if (risultato instanceof List) {
            int totalCount = risultato.size()
            if(totalCount < offset){
                offset = 0
            }
            if (totalCount > 0 && totalCount > pageSize) {
                int endIndex = offset + pageSize
                if (endIndex >= risultato.size()) {
                    endIndex = risultato.size()
                }
                risultato = risultato.subList(offset, endIndex)
            }
            if (risultato instanceof PagedResultList) {
                risultato.totalCount = totalCount
            } else {
                risultato = new PagedResultList(risultato, totalCount)
            }
        }
        return risultato
    }

    boolean tipologiaHaMetodoCalcolo(TipologiaSoggetto c, String nomeMetodo, String tipoSoggetto) {
        long found = TipologiaSoggettoRegola.createCriteria().get {
            projections {
                rowCount()
            }
            eq("tipoSoggetto", tipoSoggetto)
            or {
                regolaCalcoloDefault {
                    eq("nomeMetodo", nomeMetodo)
                }

                regolaLista {
                    eq("nomeMetodo", nomeMetodo)
                }
            }
        }

        return (found > 0)
    }

    def creaSoggetto(documento, TipologiaSoggettoRegola tipologiaSoggettoRegola, soggetti) {
        // se non ho la regola di default, allora non creo il nuovo soggetto.
        // Questo mi serve per poter gestire quei soggetti "opzionali" del documento, ad esempio, per le determine congiunte di Treviso, non è detto che ci sia una Unità o un firmatario in più.
        if (tipologiaSoggettoRegola.regolaDefault == null || tipologiaSoggettoRegola.regolaDefault.nomeBean == "-- nessuno --") {
            return null
        }

        def result = eseguiRegola(tipologiaSoggettoRegola.regolaDefault, documento, soggetti, tipologiaSoggettoRegola.ruolo?.ruolo, tipologiaSoggettoRegola.tipoSoggettoPartenza, null)
        def soggetto = [:]
        soggetto.modificato = true

        if (result instanceof So4ComponentePubb) {
            soggetto.tipoSoggetto = tipologiaSoggettoRegola.tipoSoggetto
            soggetto.descrizione = result?.nominativoSoggetto
            soggetto.utente = result?.soggetto?.utenteAd4?.toDTO()
            soggetto.unita = result?.getUnitaPubb()?.toDTO()
        } else if (result instanceof So4UnitaPubb) {
            soggetto.tipoSoggetto = tipologiaSoggettoRegola.tipoSoggetto
            soggetto.unita = result?.toDTO()
        }

        return soggetto
    }

    private eseguiRegola(RegolaCalcolo regola, documento, soggetti, String codiceRuolo, String tipoSoggettoPartenza, String filtro) {
        return applicationContext.getBean(regola.nomeBean)."${regola.nomeMetodo}"(documento, soggetti, codiceRuolo, tipoSoggettoPartenza, filtro)
    }

    TipologiaSoggetto reloadFromDb(Long idTipologiaSoggetto ) {
        TipologiaSoggetto tipologiaSoggetto = null
        sessionFactory.getCurrentSession().disableFilter("soloValidiFilter")
        try {
            tipologiaSoggetto = TipologiaSoggetto.findById(idTipologiaSoggetto)
        } finally {
            sessionFactory.getCurrentSession().enableFilter("soloValidiFilter")
        }
        return tipologiaSoggetto
    }

    PagedResultList list(int pageSize, int activePage, String filterCondition, boolean visualizzaTutti) {
        if(visualizzaTutti) {
            sessionFactory.getCurrentSession().disableFilter("soloValidiFilter")
        }
        try {
            PagedResultList listaP = TipologiaSoggetto.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
                if (!visualizzaTutti) {
                    eq("valido", true)
                }

                tipoOggetto {
                    order ("nome", "asc")
                }

                if (filterCondition ?: "" != "") ilike("descrizione", "%${filterCondition}%")

                order("descrizione", "asc")

                fetchMode("tipoOggetto", FetchMode.JOIN)
            }
            return listaP
        } finally {
            sessionFactory.getCurrentSession().enableFilter("soloValidiFilter")
        }
    }
}