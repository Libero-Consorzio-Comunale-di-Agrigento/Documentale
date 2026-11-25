package it.finmatica.gestionedocumenti.impostazioni

import groovy.util.logging.Slf4j
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiMultiEnteService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import it.finmatica.so4.login.So4UserDetail
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import java.util.concurrent.ConcurrentHashMap

@Slf4j
@Transactional(readOnly = true)
class ImpostazioniMap {

    public static final Long ENTE_FALLBACK = -1
    public static final String ENTE_SENZA_LOGIN = "ENTE_SENZA_LOGIN"

    @Autowired GestioneDocumentiSpringSecurityService springSecurityService

    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, String>> map
    private final Object lock
    private boolean initialized = false

    ImpostazioniMap() {
        lock = new Object()
        map = new ConcurrentHashMap<Long, ConcurrentHashMap<String, String>>()
    }

    void init() {
        Impostazioni.map = this
    }

    void clear() {
        synchronized (lock) {
            map.clear()
            initialized = false
        }
    }

    void refresh() {
        synchronized (lock) {

            // ripulisco le impostazioni
            clear()

            // ottengo tutte le impostazioni
            Collection<Impostazione> impostazioni = getListaImpostazioni()

            // per ogni impostazione, riempio la mappa delle impostazioni per ente
            for (Impostazione impostazione : impostazioni) {

                // se l'impostazione letta da db non ha l'ente, uso quello di fall-back (-1)
                Long ente = impostazione.ente?.id ?: ENTE_FALLBACK

                // leggo la mappa delle impostazioni per l'ente dell'impostazione che sto iterando
                ConcurrentHashMap<String, String> impostazioniPerEnte = map.get(ente)

                // se non ho la mappa delle impostazioni per l'ente, allora ne creo una nuova
                if (impostazioniPerEnte == null) {
                    impostazioniPerEnte = new ConcurrentHashMap<String, String>()
                    map.put(ente, impostazioniPerEnte)
                }

                // setto il valore dell'impostazione nella mappa per ente
                if (impostazione.valore != null) {
                    impostazioniPerEnte.put(impostazione.codice, impostazione.valore)
                }
            }

            // segnalo che la mappa è inizializzata
            initialized = true
        }
    }

    /**
     * Ritorna l'elenco delle impostazioni.
     * Metto questo metodo pubblico solo per esigenze di test (ahimè), siccome negli unit-test non c'è verso di fare override di Impostazioni.list ed Impostazioni è una domain con id composite non gestito in unit-test.
     * @return l'elenco delle impostazioni
     */
    Collection<Impostazione> getListaImpostazioni() {
        return Impostazione.createCriteria().list {
            createAlias('ente', 'e', CriteriaSpecification.LEFT_JOIN)

            or {
                isNull("e.id")
                eq("e.valido", true)
            }
            order("ente", "asc")
        }
    }

    /**
     * Questo metodo imposta il codice ente come valore di request.
     * È necessario nel caso in cui l'utente non abbia fatto login e quindi non ci sia lo springSecurityService.principal
     *
     * @param ente
     * @return
     */
    // TODO[SPRINGBOOT]: va sostituito con la gesitone dell'ente
    void setCodiceEnteSenzaLogin(Long ente) {
        //WebUtils.retrieveGrailsWebRequest().getSession()[ENTE_SENZA_LOGIN] = ente
    }

    // TODO[SPRINGBOOT]: va sostituito con la gesitone dell'ente
    Long getCodiceEnteSenzaLogin() {
        return null // (Long) WebUtils.retrieveGrailsWebRequest().getSession()[ENTE_SENZA_LOGIN]
    }

    String getValore(String impostazione, String valoreDefault, Long ente) {
        if (!initialized) {
            refresh()
        }

        ConcurrentHashMap<String, String> enteMap = null
        if (ente != null) {
            enteMap = map.get(ente)
        }

        // se l'ente richiesto non ha l'impostazione richiesta, vado sull'ente di fallback
        if (enteMap == null || !enteMap.containsKey(impostazione)) {
            enteMap = map.get(ENTE_FALLBACK)
        }

        // se ancora non ho l'ente oppure non ho l'impostazione, ritorno il valore di default dell'impostazione
        if (enteMap == null || !enteMap.containsKey(impostazione)) {
            return valoreDefault
        }

        return enteMap.get(impostazione)
    }

    String getValore(String impostazione, String valoreDefault) {
        return getValore(impostazione, valoreDefault, getCodiceEnte())
    }

    private Long getCodiceEnte() {
        if (springSecurityService.isLoggedIn()) {
            return springSecurityService.getPrincipal().getIdEnte()
        }

        if (getCodiceEnteSenzaLogin() > 0) {
            return getCodiceEnteSenzaLogin()
        }

        return ENTE_FALLBACK
    }
}
