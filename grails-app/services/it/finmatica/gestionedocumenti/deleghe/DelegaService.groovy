package it.finmatica.gestionedocumenti.deleghe
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.config.Ad4Properties
import org.springframework.beans.factory.annotation.Autowired

@Service
@Transactional
class DelegaService {

    @Autowired
    private Ad4Properties ad4Properties

    List<Ad4Utente> getDeleganti(Ad4Utente utente) {
        return Delega.createCriteria().list {
            projections {
                distinct "deleganteUtente"
            }
            or {
                isNull("al")
                ge("al", new Date())
            }
            or {
                isNull("dal")
                le("dal", new Date())
            }

            or {
                isNull("istanzaApplicativo")
                eq("istanzaApplicativo", ad4Properties.istanza)
            }
            or {
                isNull("moduloApplicativo")
                eq("moduloApplicativo", ad4Properties.modulo)
            }

            eq("delegatoUtente", utente)
        }
    }

    List<Delega> getDeleghe(Ad4Utente delegato, Ad4Utente delegante = null) {
        return Delega.createCriteria().list {
            or {
                isNull("al")
                ge("al", new Date())
            }
            or {
                isNull("dal")
                le("dal", new Date())
            }
            if (delegante != null) {
                eq("deleganteUtente", delegante)
            }
            or {
                isNull("istanzaApplicativo")
                eq("istanzaApplicativo", ad4Properties.istanza)
            }
            or {
                isNull("moduloApplicativo")
                eq("moduloApplicativo", ad4Properties.modulo)
            }
            eq("delegatoUtente", delegato)
        }
    }

    boolean hasDelega(Ad4Utente delegato, Ad4Utente delegante) {
        return getDeleghe(delegato, delegante) != null
    }

    List getTipologie(Ad4Utente delegato, Ad4Utente delegante) {
        return getDeleghe(delegato, delegante).tipologia
    }
}
