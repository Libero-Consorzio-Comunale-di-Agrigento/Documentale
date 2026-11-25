package it.finmatica.gestionedocumenti.multiente

import it.finmatica.multiente.Ente
import it.finmatica.multiente.MultiEnteService
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class GestioneDocumentiMultiEnteService implements MultiEnteService {

    private final GestioneDocumentiSpringSecurityService springSecurityService

    GestioneDocumentiMultiEnteService(GestioneDocumentiSpringSecurityService so4SpringSecurityService) {
        this.springSecurityService = so4SpringSecurityService
    }

    @Override
    Ente getEnteDefault() {
        List<it.finmatica.gestionedocumenti.commons.Ente> enti = getEntiConfigurati()

        if (enti.size() == 1) {
            return enti[0]
        }

        return null
    }

    @Override
    void selezionaEnte(Ente ente) {
        long idEnte = 0
        if (ente.id instanceof String) {
            idEnte = Long.parseLong((String)ente.id)
        }
        if (ente.id instanceof Number) {
            idEnte = ((Number)ente.id).longValue()
        }
        springSecurityService.getPrincipal().setIdEnte(idEnte)
    }

    @Override
    Ente getEnte() {
        return springSecurityService.getPrincipal().getEnte()
    }

    @Override
    boolean isEnteSelezionato() {
        return springSecurityService.getPrincipal().getIdEnte() > 0
    }

    @Override
    List<Ente> getListaEnti() {
        return getEntiConfigurati()
    }

    @Override
    void clearEnte() {

    }

    private List<it.finmatica.gestionedocumenti.commons.Ente> getEntiConfigurati() {
        List<String> codiciAmm = springSecurityService.principal.amministrazioni.codice

        // tra gli enti di so4, quanti ce ne sono configurati sulla tabella Enti ?
        return it.finmatica.gestionedocumenti.commons.Ente.createCriteria().list {
            amministrazione {
                'in' ("codice", codiciAmm)
            }
            eq ("valido", true)
        }
    }
}
