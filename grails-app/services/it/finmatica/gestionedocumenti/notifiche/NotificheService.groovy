package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.gestionedocumenti.HibernateUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

import groovy.util.logging.Slf4j
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.commons.StrutturaOrganizzativaService
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RegolaCalcoloNotifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import it.finmatica.gestionedocumenti.notifiche.calcolo.TipoNotifica
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

@Slf4j
@Service
@Transactional
class NotificheService {

    private @Autowired
    StrutturaOrganizzativaService strutturaOrganizzativaService
    private @Autowired
    NotificheDispatcher notificheDispatcher
    private @Autowired
    RepositoryRegoleCalcolo repositoryRegoleCalcolo

    /*
     * Gestione della configurazione
     */

    List<TipoNotifica> getTipiNotifiche() {
        return repositoryRegoleCalcolo.getListaTipiNotifica()
    }

    List<String> getTipiDocumento() {
        return (getTipiNotifiche()*.oggetti).flatten().unique().sort()
    }

    List<RegolaCalcoloNotifica> getRegoleCalcoloDestinatari() {
        return repositoryRegoleCalcolo.getRegoleCalcoloDestinatari()
    }

    List<RegolaCalcoloNotifica> getRegoleCalcoloAllegati() {
        return repositoryRegoleCalcolo.getRegoleCalcoloAllegati()
    }

    List<RegolaCalcoloNotifica> getRegoleCalcoloTag() {
        return repositoryRegoleCalcolo.getRegoleCalcoloTag()
    }

    /*
     * Gestione dell'invio delle notifiche
     */

    void invia(String codiceNotifica, Object documento, String messaggioTodo = null, boolean async = true) {

        List<Notifica> notifiche = Notifica.perTipo(codiceNotifica, repositoryRegoleCalcolo.getTipoDocumento(documento)).list()

        log.info(getLogNotifica(documento, "invia"))

        for (Notifica notifica : notifiche) {
            log.info("Notifica Titolo:" + notifica.titolo)
            invia(notifica, documento, messaggioTodo, async)
        }
    }

    void invia(Notifica notifica, Object documento, String messaggioTodo = null, boolean async = true) {
        List<NotificaSoggetto> destinatari = calcolaSoggettiNotifica(notifica, documento)

        log.info("Notifica Numero destinatari:" + destinatari?.size())

        String oggetto = calcolaTestoNotifica(notifica.oggetto, documento)
        String testo = calcolaTestoNotifica(notifica.testo, documento)
        log.info(getLogNotifica(documento, "invia"))
        notificheDispatcher.invia(notifica, documento, destinatari, oggetto, testo, messaggioTodo, async)
    }

    /**
     * Elimina la notifica di un certo tipo associata al documento e all'utente
     *
     * @param documento il documento di cui eliminare le notifiche
     * @param utente l'utente di cui eliminare le notifiche
     * @param tipoNotifica il tipo di notifica da eliminare
     */
    void eliminaNotifica(String tipoNotifica, String idDocumentoEsterno, Ad4Utente utente) {
        notificheDispatcher.elimina(tipoNotifica, idDocumentoEsterno, utente)
    }

    /*
     * Gestione dell'invio delle notifiche
     */

    void aggiorna(String codiceNotifica, Object documento, String messaggioTodo = null, boolean async = true) {

        List<Notifica> notifiche = Notifica.perTipo(codiceNotifica, repositoryRegoleCalcolo.getTipoDocumento(documento)).list()

        log.info(getLogNotifica(documento, "aggiorna"))

        for (Notifica notifica : notifiche) {
            log.info("Notifica Titolo:" + notifica.titolo)
            aggiorna(notifica, documento, messaggioTodo, async)
        }
    }

    void aggiorna(Notifica notifica, Object documento, String messaggioTodo = null, boolean async = true) {
        List<NotificaSoggetto> destinatari = calcolaSoggettiNotifica(notifica, documento)

        log.info("Notifica Numero destinatari:" + destinatari?.size())

        String oggetto = calcolaTestoNotifica(notifica.oggetto, documento)
        String testo = calcolaTestoNotifica(notifica.testo, documento)
        log.info(getLogNotifica(documento, "aggiorna"))
        notificheDispatcher.aggiorna(notifica, documento, destinatari, oggetto, testo, messaggioTodo, async)
    }

    /**
     * Ritorna true se esiste una notifica del tipo richiesto per l'utente.
     *
     * Nota che le notifiche sono di due macro-tipi:
     * - per competenza: sono le notifiche DA_FIRMARE e ASSEGNAZIONE
     * - per conoscenza: tutte le altre notifiche.
     *
     * Queste due categorie hanno degli id_riferimento generati diversi per una retrocompatibilità col passato (che andrebbe smantellata).
     *
     * @param documento
     * @param utente
     * @return
     */
    boolean isNotificaPresente(String tipoNotifica, Object documento, Ad4Utente utente) {
        return notificheDispatcher.isNotificaPresente(tipoNotifica, documento, utente)
    }

    String calcolaTestoNotifica(String testo, Object documento) {
        // ciclo sui campi presenti nel testo e li sostituisco
        for (def match : (testo =~ /\[([\w]+)\]/)) {

            String matchTotale = match[0]    // il nome del campo con le quadre, ad es: [N_PROPOSTA]
            String campo = match[1] // il nome del campo senza le quadre, ad es: N_PROPOSTA

            // eseguo la funzione per ottenere il valore da sostituire
            String value = repositoryRegoleCalcolo.calcolaTag(campo, documento)

            if (value == null) {
                value = ""
            }

            // lo sostituisco
            testo = testo.replace(matchTotale, value)
        }

        return testo
    }

    List<NotificaSoggetto> calcolaSoggettiNotifica(Notifica notifica, Object documento) {
        List<NotificaSoggetto> soggettiNotifica = []

        // calcolo i soggetti a cui mandare la notifica
        for (NotificaDestinatario notificaDestinatario : notifica.destinatari) {

            // se la notifica email contiene un indirizzo email, uso quello:
            if (notificaDestinatario.email != null) {
                soggettiNotifica << new NotificaSoggetto(email: notificaDestinatario.email)
                continue
            }

            // se la notifica email contiene una funzione, la eseguo:
            if (notificaDestinatario.funzione != null) {
                // se la funzione è di tipo "after commit", non la eseguo subito, ma registro che andrà eseguita.
                // Questo serve per gestire il caso delle notifiche che vanno valutate solo quando la navigazione del flusso è terminata.
                def dest = calcolaNotificaSoggetto(notificaDestinatario, documento, notificaDestinatario.funzione)
                soggettiNotifica.addAll(dest)

                // in ogni caso, dopo aver valutato, proseguo il for.
                continue
            }

            // se il destinatario è un soggetto, lo aggiungo con il suo utente
            if (notificaDestinatario.soggetto != null) {
                soggettiNotifica.add(new NotificaSoggetto(email: notificaDestinatario.soggetto.indirizzoWeb, soggetto: notificaDestinatario.soggetto?.toDTO(), utente: notificaDestinatario.soggetto.utenteAd4?.toDTO()))
                continue
            }

            // se la notifica email contiene un ruolo e una unità, aggiungo tutti gli utenti di quella unità con quel ruolo:
            if (notificaDestinatario.ruolo != null && notificaDestinatario.unita != null) {
                def componenti = getUtentiConRuoloInUnita(notificaDestinatario.ruolo.ruolo, notificaDestinatario.unita)
                soggettiNotifica.addAll(componenti)
                continue
            }

            // se la notifica email contiene solo un ruolo, aggiungo gli utenti con quel ruolo:
            if (notificaDestinatario.ruolo != null) {
                def componenti = strutturaOrganizzativaService.getComponentiConRuoloInOttica(notificaDestinatario.ruolo.ruolo, Impostazioni.OTTICA_SO4.valore)
                for (def c : componenti) {
                    soggettiNotifica << new NotificaSoggetto(utente: c.soggetto.utenteAd4?.toDTO()
                            , email: c.soggetto.indirizzoWeb
                            , soggetto: c.soggetto?.toDTO()
                            , ruoloAd4: notificaDestinatario.ruolo?.toDTO())
                }

                continue
            }

            // se la notifica email contiene solo una unità, aggiungo gli utenti di quella unità:
            if (notificaDestinatario.unita != null) {
                def componenti = So4ComponentePubb.componentiUnitaPubb(notificaDestinatario.unita.progr, notificaDestinatario.unita.ottica.codice, new Date()).list()
                for (def c : componenti) {
                    //#77309 - forziamo l'unwrap dei proxy da hybernate
                    def utenteAd4 = c?.soggetto?.utenteAd4 ? HibernateUtils.unwrapIfProxy(c?.soggetto?.utenteAd4) : null
                    def unitaSo4 = notificaDestinatario.unita ? HibernateUtils.unwrapIfProxy(notificaDestinatario.unita) : null

                    if (utenteAd4 != null) {
                        soggettiNotifica << new NotificaSoggetto(
                                utente: utenteAd4?.toDTO()
                                , email: c?.soggetto?.indirizzoWeb
                                , soggetto: c?.soggetto?.toDTO()
                                , assegnazione: notificaDestinatario.assegnazione
                                , unitaSo4: unitaSo4?.toDTO())
                    }
                }
            }
        }

        // #77309 - sovrascrive 74531 - facciamo il controllo di unique per tutti, ma ci assicuriamo che utente e unità siano valorizzati se esistono
        // if(notifica.modalitaInvio == Notifica.MODALITA_EMAIL){
        // elimino i doppi.
        soggettiNotifica = soggettiNotifica.unique()
        //}

        return soggettiNotifica
    }

    List<NotificaSoggetto> calcolaNotificaSoggetto(NotificaDestinatario destinatario, Object documento, String codiceFunzione) {
        List<NotificaSoggetto> soggettiNotifica = repositoryRegoleCalcolo.calcolaDestinatari(codiceFunzione, documento, destinatario)

        // filtro i soggetti per ruolo/unità
        soggettiNotifica = filtraSoggetti(destinatario, soggettiNotifica)

        return soggettiNotifica
    }

    private List<NotificaSoggetto> getUtentiConRuoloInUnita(String ruolo, So4UnitaPubb unitaSo4, NotificaDestinatario destinatarioNotifica = null) {
        def utenti = []
        def componenti = strutturaOrganizzativaService.getComponentiConRuoloInUnita(ruolo, unitaSo4.progr, unitaSo4.ottica.codice)

        for (def c : componenti) {
            // seleziono solo i componenti che hanno un utente ad4.
            if (c.soggetto.utenteAd4 != null) {
                utenti.add(new NotificaSoggetto(utente: c.soggetto.utenteAd4?.toDTO()
                        , email: c.soggetto.indirizzoWeb
                        , soggetto: c.soggetto?.toDTO()
                        , competenza: destinatarioNotifica.assegnazione
                        , destinatarioNotifica: destinatarioNotifica
                        , unitaSo4: unitaSo4?.toDTO()
                        , ruoloAd4: Ad4Ruolo.get(ruolo)?.toDTO()))
            }
        }

        return utenti
    }

    private List<NotificaSoggetto> filtraSoggetti(NotificaDestinatario destinatarioNotifica, List<NotificaSoggetto> soggettiNotifica) {
        if (destinatarioNotifica.ruolo == null && destinatarioNotifica.unita == null) {
            return soggettiNotifica
        }

        List<NotificaSoggetto> destinatariFiltrati = []

        // ottengo tutti i soggetti che hanno il ruolo richiesto per l'unità richiesta
        for (NotificaSoggetto soggettoNotifica : soggettiNotifica) {

            if (destinatarioNotifica.ruolo != null || destinatarioNotifica.unita != null) {
                boolean result = strutturaOrganizzativaService.soggettoHaRuoloPerUnita(soggettoNotifica.soggetto.id, destinatarioNotifica.ruolo?.ruolo, destinatarioNotifica.unita?.progr ?: -1, Impostazioni.OTTICA_SO4.valore)

                if (result) {
                    destinatariFiltrati << soggettoNotifica
                }
            }
        }

        return destinatariFiltrati
    }

    // #80630
    private String getLogNotifica(Object documento, String action) {
        if (documento.hasProperty("id")) {
            return "NotificheService: Classe del documento ${documento.id} per cui ${action} la notifica: ${documento.class.canonicalName}"
        } else {
            return "NotificheService: Classe del documento per cui ${action} la notifica: ${documento.class.canonicalName}"
        }
    }
}
