package it.finmatica.gestionedocumenti.notifiche.iter

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.gestionedocumenti.commons.StrutturaOrganizzativaService
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.notifiche.NotificaDestinatario
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RegolaCalcoloNotifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.TipoNotifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.annotated.AnnotatedRegolaCalcolo
import it.finmatica.gestionedocumenti.notifiche.calcolo.annotated.RegolaCalcolo
import it.finmatica.gestioneiter.motore.WkfAttoreStep
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by esasdelli on 11/04/2017.
 */
class RepositoryRegoleCalcoloIter implements AnnotatedRegolaCalcolo {

    @Autowired
    private StrutturaOrganizzativaService strutturaOrganizzativaService

    @Override
    List<TipoNotifica> getListaTipiNotifica() {
        return []
    }

    @Override
    String getTipoDocumento(Object documento) {
        return null
    }

    @Override
    String getIdRiferimento(Object documento) {
        return null
    }

    @Override
    Object getDocumento(String idRiferimento) {
        return null
    }

    @RegolaCalcolo(tipo=RegolaCalcoloNotifica.TipoMetodo.DESTINATARI, codice="GET_SOGGETTI_NODO_CORRENTE", titolo = "Ritorna i Soggetti del nodo corrente", descrizione = "Ritorna gli utenti che hanno in carico il documento nel nodo corrente")
    Collection<NotificaSoggetto> calcolaSoggettiNotificaStepCorrente (Documento documento, NotificaDestinatario notificaDestinatario) {
        if (documento.iter?.stepCorrente == null) {
            return []
        }

        return calcolaSoggettiNotificaStep (documento.iter.stepCorrente)
    }

    @RegolaCalcolo(tipo=RegolaCalcoloNotifica.TipoMetodo.DESTINATARI, codice="GET_SOGGETTI_FLUSSO", titolo = "Ritorna i Soggetti che hanno partecipato al flusso", descrizione = "Ritorna gli utenti che hanno partecipato al flusso")
    Collection<NotificaSoggetto> calcolaSoggettiNotificaFlusso (Documento documento, NotificaDestinatario notificaDestinatario) {
        if (documento.iter == null) {
            return []
        }

        List<WkfAttoreStep> attoriStep = WkfAttoreStep.createCriteria().list {
            step {
                eq ("iter.id", documento.iter.id)
            }
        }

        List<NotificaSoggetto> soggettiAs4 = [];
        for (WkfAttoreStep attore : attoriStep) {
            soggettiAs4.addAll(calcolaSoggettiNotifica(attore))
        }

        return soggettiAs4
    }

    /**
     * Ritorna l'elenco dei soggetti che corrispondono agli attori del flusso, meno i soggetti degli attori dello step corrente.
     *
     * @param iter
     * @return 	l'elenco dei soggetti che corrispondono agli attori del flusso, meno i soggetti degli attori dello step corrente.
     */
    @RegolaCalcolo(tipo=RegolaCalcoloNotifica.TipoMetodo.DESTINATARI, codice="GET_SOGGETTI_FLUSSO_TRANNE_NODO_CORRENTE", titolo = "Ritorna i Soggetti che hanno partecipato al flusso eccetto quelli del nodo corrente", descrizione = "Ritorna i Soggetti che hanno partecipato al flusso eccetto quelli del nodo corrente")
    Collection<NotificaSoggetto> calcolaSoggettiNotificaFlussoTranneStepCorrente (Documento documento, NotificaDestinatario notificaDestinatario) {
        if (documento.iter?.stepCorrente == null) {
            return []
        }

        List<NotificaSoggetto> soggettiStepCorrente = calcolaSoggettiNotificaStep(documento.iter.stepCorrente)
        List<NotificaSoggetto> soggettiAttoriFlusso = calcolaSoggettiNotificaFlusso(documento.iter)

        // prendo solo gli id dei soggetti che non sono attori dello step corrente
        // posso usare la 'removeAll' perché NotificaSoggetto implementa l'interfaccia Comparable
        soggettiAttoriFlusso.removeAll (soggettiStepCorrente)

        return soggettiAttoriFlusso
    }

    private Collection<NotificaSoggetto> calcolaSoggettiNotificaStep (WkfStep step) {
        List<NotificaSoggetto> soggettiAs4 = []
        for (WkfAttoreStep attore : step.attori) {
            soggettiAs4.addAll(calcolaSoggettiNotifica(attore))
        }

        return soggettiAs4
    }

    /**
     * Questa funzione calcola tutti i SoggettiNotifica per un Attore.
     * Ritorna la classe NotificaSoggetto perché in fase di assegnazione delle competenze dal JWorklistService ho bisogno di sapere
     * se devo assegnare le competenze per utente, unità, ruolo o ruolo + unità.
     *
     * -> http://svi-redmine/issues/14719
     *
     * @param attore
     * @return
     */
    private Collection<NotificaSoggetto> calcolaSoggettiNotifica (WkfAttoreStep attore) {
        List<NotificaSoggetto> soggettiAs4 = []
        if (attore.utenteAd4) {
            As4SoggettoCorrente sog = As4SoggettoCorrente.findByUtenteAd4(attore.utenteAd4)

            // in teoria è sempre != null
            if (sog != null) {
                soggettiAs4.add(new NotificaSoggetto(email: sog.indirizzoWeb, soggetto:sog?.toDTO(), utente:attore.utenteAd4?.toDTO()))
            }

        } else if (attore.unitaSo4 && attore.ruoloAd4) {
            List<So4ComponentePubb> listaComponenti = strutturaOrganizzativaService.getComponentiConRuoloInUnita (attore.ruoloAd4.ruolo, attore.unitaSo4.progr, Impostazioni.OTTICA_SO4.valore)

            for (So4ComponentePubb c : listaComponenti) {
                if (c.soggetto != null) {
                    soggettiAs4.add(new NotificaSoggetto(email: c.soggetto.indirizzoWeb, soggetto:c.soggetto?.toDTO(), utente:c.soggetto.utenteAd4?.toDTO(), ruoloAd4: attore.ruoloAd4?.toDTO(), unitaSo4: attore.unitaSo4?.toDTO()))
                }
            }

        } else if (attore.unitaSo4) {
            List<So4ComponentePubb> listaComponenti = So4ComponentePubb.componentiUnitaPubb(attore.unitaSo4.progr, Impostazioni.OTTICA_SO4.valore, new Date()).list()

            for (So4ComponentePubb c : listaComponenti) {
                if (c.soggetto != null) {
                    soggettiAs4.add(new NotificaSoggetto(email: c.soggetto.indirizzoWeb, soggetto:c.soggetto?.toDTO(), utente:c.soggetto.utenteAd4?.toDTO(), unitaSo4: attore.unitaSo4?.toDTO()))
                }
            }

        } else if (attore.ruoloAd4) {
            List<So4ComponentePubb> listaComponenti = strutturaOrganizzativaService.getComponentiConRuoloInOttica(attore.ruoloAd4.ruolo, Impostazioni.OTTICA_SO4.valore)

            for (So4ComponentePubb c : listaComponenti) {
                if (c.soggetto != null) {
                    soggettiAs4.add(new NotificaSoggetto(email: c.soggetto.indirizzoWeb, soggetto:c.soggetto?.toDTO(), utente:c.soggetto.utenteAd4?.toDTO(), ruoloAd4: attore.ruoloAd4?.toDTO()))
                }
            }
        }

        return soggettiAs4
    }
}
