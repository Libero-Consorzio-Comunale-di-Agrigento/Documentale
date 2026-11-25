package it.finmatica.gestionedocumenti.notifiche.dispatcher.aftercommit

import org.springframework.scheduling.annotation.Async

/**
 * Unico scopo di questo bean è di invocare in modo asincrono il bean di invio notifiche.
 *
 * Created by esasdelli on 07/04/2017.
 */
class AsyncNotificheDispatcherDelegator {

    private final AfterCommitNotificheDispatcherDelegator afterCommitNotificheDispatcherDelegator

    AsyncNotificheDispatcherDelegator(AfterCommitNotificheDispatcherDelegator afterCommitNotificheDispatcherDelegator) {
        this.afterCommitNotificheDispatcherDelegator = afterCommitNotificheDispatcherDelegator
    }

    @Async
    void gestisciCodaOperazioni(String nominativoUtente, long idEnte, List<Operazione> codaOperazioni) {
        afterCommitNotificheDispatcherDelegator.gestisciCodaOperazioni(nominativoUtente, idEnte, codaOperazioni)
    }
}
