package it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist

import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by esasdelli on 07/04/2017.
 */
class DefaultNotificaJWorklistBuilder implements NotificaJWorklistBuilder {

    private @Autowired RepositoryRegoleCalcolo repositoryRegoleCalcolo

    @Override
    NotificaJWorklist creaNotificaJWorklist (Notifica notifica, Object documento, NotificaSoggetto soggetto, String testo, String oggetto, String messaggioTODO) {
        return new NotificaJWorklist (idRiferimento: repositoryRegoleCalcolo.getIdRiferimento(documento), testo: testo, tooltip: oggetto, messaggioToDo: messaggioTODO)
    }

    @Override
    List<NotificaJWorklist> creaNotificaJWorklist(Notifica notifica, Object documento, List<NotificaSoggetto> soggetto, String testo, String oggetto, String messaggioTODO) {
        return new ArrayList<NotificaJWorklist>(new NotificaJWorklist (idRiferimento: repositoryRegoleCalcolo.getIdRiferimento(documento), testo: testo, tooltip: oggetto, messaggioToDo: messaggioTODO))
    }
}
