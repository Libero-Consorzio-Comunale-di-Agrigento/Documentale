package it.finmatica.gestionedocumenti.notifiche.dispatcher.aftercommit

import groovy.util.logging.Slf4j
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.commons.Utils
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.NotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Il compito di questo bean è solo quello di instaurare una nuova transazione con REQUIRES_NEW per l'invio delle notifiche.
 * Il REQUIRES_NEW è consigliato per quel codice che esegue nella AfterCommit, necessario per separare la commit della transazione corrente
 * (soprattutto quando effettuata su più connessioni come nel nostro caso) dalla nuova transazione.
 *
 * Inoltre, questo bean forzando la creazione di una nuova transazione, può essere invocato da un bean annotato con @Async siccome anche in questo caso
 * la documentazione di spring consiglia di separare bean @Async da bean @Transactional.
 *
 * Created by esasdelli on 07/04/2017.
 */
@Slf4j
class AfterCommitNotificheDispatcherDelegator {

    private final RepositoryRegoleCalcolo repositoryRegoleCalcolo
    private final NotificheDispatcher targetNotificheDispatcher

    AfterCommitNotificheDispatcherDelegator(RepositoryRegoleCalcolo repositoryRegoleCalcolo, NotificheDispatcher targetNotificheDispatcher) {
        this.repositoryRegoleCalcolo = repositoryRegoleCalcolo
        this.targetNotificheDispatcher = targetNotificheDispatcher
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void gestisciCodaOperazioni(String nominativoUtente, long idEnte, List<Operazione> codaOperazioni) {

        Utils.eseguiAutenticazione(nominativoUtente, idEnte)

        gestisciCodaOperazioni(codaOperazioni)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void gestisciCodaOperazioni(List<Operazione> codaOperazioni) {
        for (Operazione operazione : codaOperazioni) {
            log.info("Operazione tipo:${operazione.tipo}, Sequenza:${operazione.sequenza}, idRiferimento:${operazione.idRiferimento}, idDestinatarioNotificaAttivita:${operazione.idDestinatarioNotificaAttivita}, idNotifica:${operazione.idNotifica}, utenti:${operazione.destinatari}")
            switch (operazione.tipo) {
                case Operazione.Tipo.INVIA:
                    targetNotificheDispatcher.invia(Notifica.get(operazione.idNotifica), repositoryRegoleCalcolo.getDocumento(operazione.idRiferimento), operazione.destinatari, operazione.oggetto, operazione.testo, operazione.messaggioTODO, operazione.async)
                    break
                case Operazione.Tipo.ELIMINA:
                    targetNotificheDispatcher.elimina(operazione.codiceNotifica, operazione.idRiferimento, Ad4Utente.get(operazione.idUtente))
                    break
                case Operazione.Tipo.AGGIORNA:
                    targetNotificheDispatcher.aggiorna(Notifica.get(operazione.idNotifica), repositoryRegoleCalcolo.getDocumento(operazione.idRiferimento), operazione.destinatari, operazione.oggetto, operazione.testo, operazione.messaggioTODO, operazione.async)
                    break
            }
        }
    }

    @Transactional
    boolean isNotificaPresente(String tipoNotifica, Object documento, Ad4Utente utente) {
        return targetNotificheDispatcher.isNotificaPresente(tipoNotifica, documento, utente)
    }
}
