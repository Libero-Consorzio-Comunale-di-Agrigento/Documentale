package it.finmatica.gestionedocumenti.notifiche.dispatcher.aftercommit

import groovy.util.logging.Slf4j
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.NotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import it.finmatica.so4.login.So4SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Scopo di questo bean è di salvare le varie notifiche da inviare per poi inviarle una volta fatta la commit della transazione corrente.
 * Per fare ciò, è necessario che questo bean abbia scope Thread siccome le transazioni sono thread-bounded.
 * L'invio delle notifiche, avverrà dopo la commit della transazione corrente, l'invio può avvenire in modo sincrono oppure asincrono in un altro thread.
 *
 * Created by esasdelli on 07/04/2017.
 */
@Slf4j
class AfterCommitNotificheDispatcher extends TransactionSynchronizationAdapter implements NotificheDispatcher {

    private final AfterCommitNotificheDispatcherDelegator afterCommitNotificheDispatcherDelegator
    private final AsyncNotificheDispatcherDelegator asyncNotificheDispatcherDelegator
    private final RepositoryRegoleCalcolo repositoryRegoleCalcolo
    private final GestioneDocumentiSpringSecurityService springSecurityService

    private ThreadLocal<List<Operazione>> codaOperazioni
    private ThreadLocal<Boolean> async

    AfterCommitNotificheDispatcher(AfterCommitNotificheDispatcherDelegator afterCommitNotificheDispatcherDelegator, AsyncNotificheDispatcherDelegator asyncNotificheDispatcherDelegator, RepositoryRegoleCalcolo repositoryRegoleCalcolo, GestioneDocumentiSpringSecurityService springSecurityService) {
        this.afterCommitNotificheDispatcherDelegator = afterCommitNotificheDispatcherDelegator
        this.asyncNotificheDispatcherDelegator = asyncNotificheDispatcherDelegator
        this.repositoryRegoleCalcolo = repositoryRegoleCalcolo
        this.springSecurityService = springSecurityService
        codaOperazioni = new ThreadLocal<>()
        async = new ThreadLocal<>()
    }

    void init(boolean async = true) {
        if (codaOperazioni.get() != null) {
            return
        }

        codaOperazioni.set(new ArrayList<Operazione>())
        this.async.set(async)
    }

    void clear() {
        codaOperazioni.remove()
        async.remove()
    }

    @Override
    void invia(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {
        register()
        long sequenza = calcolaSequenza()
        codaOperazioni.get() << new Operazione(tipo: Operazione.Tipo.INVIA
                , nominativoUtente: springSecurityService.principal.username
                , idEnte: springSecurityService.principal.idEnte
                , idNotifica: notifica.id
                , idRiferimento: repositoryRegoleCalcolo.getIdRiferimento(documento)
                , destinatari: destinatari
                , oggetto: oggetto
                , messaggioTODO: messaggioTODO
                , async: async
                , testo: testo
                , sequenza: sequenza)
    }

    @Override
    void elimina(String tipoNotifica, String idRiferimento, Ad4Utente utente) {
        register()
        long sequenza = calcolaSequenza()
        codaOperazioni.get() << new Operazione(tipo: Operazione.Tipo.ELIMINA
                , nominativoUtente: springSecurityService.principal.username
                , idEnte: springSecurityService.principal.idEnte
                , codiceNotifica: tipoNotifica
                , idRiferimento: idRiferimento
                , idUtente: utente?.id
                , sequenza: sequenza)
    }

    @Override
    void aggiorna(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {
        register()
        long sequenza = calcolaSequenza()
        codaOperazioni.get() << new Operazione(tipo: Operazione.Tipo.AGGIORNA
                , nominativoUtente: springSecurityService.principal.username
                , idEnte: springSecurityService.principal.idEnte
                , idNotifica: notifica.id
                , idRiferimento: repositoryRegoleCalcolo.getIdRiferimento(documento)
                , destinatari: destinatari
                , oggetto: oggetto
                , messaggioTODO: messaggioTODO
                , async: async
                , testo: testo
                , sequenza: sequenza)
    }

    private long calcolaSequenza() {
       List<Operazione> operazioni = this.codaOperazioni.get()?.sort { it.sequenza }
       if(operazioni?.size() > 0){
           return operazioni.last().sequenza + 1
       }
       return 0
    }

    @Override
    boolean isNotificaPresente(String tipoNotifica, Object documento, Ad4Utente utente) {
        return afterCommitNotificheDispatcherDelegator.isNotificaPresente(tipoNotifica, documento, utente)
    }

    @Override
    void afterCommit() {
        try {
            List<Operazione> codaOperazioni = this.codaOperazioni.get().findAll { it.async == true }.sort{ it.sequenza}
            List<Operazione> codaOperazioniSincrone = this.codaOperazioni.get().findAll { it.async == false }

            if (codaOperazioniSincrone != null && codaOperazioniSincrone.size() > 0) {
                afterCommitNotificheDispatcherDelegator.gestisciCodaOperazioni(codaOperazioniSincrone)
            }

            boolean async = this.async.get().booleanValue()

            // Svuoto la coda operazioni, in questo modo successive chiamate/commit nella stessa "request" (come la fase di fine firma) non invieranno notifiche già gestite.
            // Inoltre, torna utile per gestire le notifiche che devono attendere la fine del flusso, come la notifica agli attori dello step di destinazione.
            clear()

            if (codaOperazioni == null || codaOperazioni.size() == 0) {
                return
            }

            if (async) {
                asyncNotificheDispatcherDelegator.gestisciCodaOperazioni(springSecurityService.principal.username, springSecurityService.principal.idEnte, codaOperazioni)
            } else {
                afterCommitNotificheDispatcherDelegator.gestisciCodaOperazioni(codaOperazioni)
            }
        } catch (Throwable t) {
            // qualsiasi cosa succeda, non devo dare errore e rischiare di invalidare la commit che sto facendo, siccome sono su più transazioni diverse.
            log.error("Errore in invio notifiche:", t)
        }
    }

    @Override
    void afterCompletion(int status) {
        // #36276
        // in ogni caso (commit o rollback), devo svuotare la coda operazioni.
        // questo serve soprattutto quando si fa "l'anteprima di stampa" in cui viene fatto un rollback "controllato" alla fine.
        // vengono aggiunte notifiche da inviare ma non vanno inviate.
        clear()
    }

    private void register() {
        init()

        if (!TransactionSynchronizationManager.getSynchronizations().contains(this)) {
            TransactionSynchronizationManager.registerSynchronization(this)
        }
    }
}
