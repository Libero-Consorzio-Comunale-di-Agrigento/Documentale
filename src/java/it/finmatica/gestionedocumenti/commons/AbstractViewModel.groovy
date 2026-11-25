package it.finmatica.gestionedocumenti.commons

import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.StatoFirma
import it.finmatica.gestionedocumenti.integrazioni.firma.GestioneDocumentiFirmaService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import it.finmatica.gestionedocumenti.zkutils.SuccessHandler
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.IGestoreCompetenze
import it.finmatica.gestioneiter.IMaschera
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgPulsante
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgPulsanteDTO
import it.finmatica.gestioneiter.exceptions.IterRuntimeException
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestionetesti.GestioneTestiService
import org.apache.commons.lang.StringUtils
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zkplus.spring.DelegatingVariableResolver
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

@VariableResolver(DelegatingVariableResolver)
abstract class AbstractViewModel<T extends IDocumentoIterabile> implements IMaschera<T> {

    // servizi.
    // Questi sono Bean Iniettati. Sono 'private' e hanno i relativi getter/setter
    // perché per qualche ragione non vengono iniettati quando questa classe viene ereditata dagli altri ViewModel.
    @WireVariable
    private GestioneDocumentiFirmaService gestioneDocumentiFirmaService
    @WireVariable
    private GestioneDocumentiSpringSecurityService springSecurityService
    @WireVariable
    private GestioneTestiService gestioneTestiService
    @WireVariable
    private SuccessHandler successHandler
    @WireVariable
    private WkfIterService wkfIterService
    @WireVariable
    private IGestoreCompetenze gestoreCompetenze
    @WireVariable
    private TransactionTemplate transactionTemplate
    @WireVariable
    private Ad4Service ad4Service

    // riverimento alla Window di questo viewModel.
    Window self

    // elenco dei pulsanti dell'iter da mostrare
    private List<WkfCfgPulsanteDTO> pulsanti

    protected boolean apriInSolaLettura = false

    /**
     * Può essere chiamata dal viewModel o dallo zul quando in maschera vengono eseguite azioni che possono cambiare i pulsanti
     */
    @Command
    void aggiornaPulsanti() {
        aggiornaPulsanti(getDocumentoIterabile(false))
    }

    void aggiornaPulsanti(Documento documento) {
        List<WkfCfgPulsante> pulsanti = wkfIterService.getPulsanti(documento, this)
        List<WkfCfgPulsanteDTO> pulsantiDto = null

        // se sono in stato IN_FIRMA o FIRMATO_DA_SBLOCCARE, mostro solo i pulsanti di firma:
        if (documento.statoFirma?.firmaInterrotta) {
            pulsanti = pulsanti.findAll { cfgPulsante -> cfgPulsante.pulsante.azioni.find { azione -> azione.nomeMetodo == "apriPopupFirma" } != null }
            this.pulsanti = pulsanti.toDTO()
            for (WkfCfgPulsanteDTO p : this.pulsanti) {
                p.getPulsante().etichetta = "Completa ${p.getPulsante().etichetta}"
            }
        } else {
            if (apriInSolaLettura) {
                pulsanti = pulsanti.findAll { !it.competenzaInModifica }
            }

            this.pulsanti = pulsanti.toDTO()
        }

        // notifico che ho aggiornato i pulsanti
        BindUtils.postNotifyChange(null, null, this, "pulsanti")

        // questo serve per riallineare la maschera con i pulsanti, senza questo, i pulsanti non si vedranno.
        invalidate()
    }

    @Command
    void clickPulsanteIter(@BindingParam("idPulsante") long idCfgPulsante) {
        Documento documentoIterabile = getDocumentoIterabile(true)

        // se sono in fase di completa firma
        switch (documentoIterabile.statoFirma) {
            case StatoFirma.FIRMATO_DA_SBLOCCARE:
                // sblocco il documento firmato
                gestioneDocumentiFirmaService.fineFirmaSbloccaFlusso(documentoIterabile, springSecurityService.currentUser)

                // aggiorno la maschera e i pulsanti al solito modo
                aggiornaMaschera(documentoIterabile)
                aggiornaPulsanti()
                break

            default:
                // eseguo il pulsante normalmente
                clickPulsanteIter(idCfgPulsante, -1)
                break
        }

        // mostro gli eventuali messaggi
        successHandler.showMessages()
    }

    void apriPopupFirma(boolean firmaAutografa = false) {
        def urlPopupFirma = firmaAutografa ? gestioneDocumentiFirmaService.urlPopupFirmaAutografa : gestioneDocumentiFirmaService.urlPopupFirma
        def idDocumento = getDocumentoIterabile(false).idDocumento
        def idTransazione = gestioneDocumentiFirmaService.transazioneCorrente.id
        Window w = Executions.createComponents("/commons/popupFirma.zul", self, [urlPopupFirma: urlPopupFirma, idDocumento: idDocumento, idTransazione: idTransazione])
        w.onClose { event ->
            aggiornaMaschera(getDocumentoIterabile(false))
            aggiornaPulsanti()
        }
        w.doModal()
    }

    void clickPulsanteIter(long idCfgPulsante, long idAzioneClient) {
        // prima di eseguire il pulsante, valido la maschera:
        Collection<String> messaggiValidazione = validaMaschera()
        if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
            Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", 3000, true)
            return
        }

        WkfCfgPulsante cfgPulsante = WkfCfgPulsante.get(idCfgPulsante)

        // blocco l'operazione se l'utente sta ancora editando il testo e se il pulsante prevede un cambio-step:
        if (gestioneTestiService.isEditorAperto() && cfgPulsante.cfgStepSuccessivo != null) {
            Clients.showNotification("Per poter inoltrare il documento, è necessario chiudere l'editor di testo.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true)
            return
        }

        // controllo se il pulsante richiede conferma (e non ho già fatto l'azione client)
        String messaggioConferma = cfgPulsante.pulsante.messaggioConferma
        if (messaggioConferma != null && messaggioConferma.length() > 0 && idAzioneClient <= 0) {
            // chiedi conferma
            Messagebox.show(messaggioConferma, "Attenzione - Richiesta Conferma",
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                    new org.zkoss.zk.ui.event.EventListener() {
                        void onEvent(Event e) {
                            if (Messagebox.ON_OK == e.getName()) {
                                AbstractViewModel.this.eseguiPulsante(idCfgPulsante, idAzioneClient)
                            }
                        }
                    }
            )
        } else {
            eseguiPulsante(idCfgPulsante, idAzioneClient)
        }
    }

    void eseguiPulsante(long idCfgPulsante, long idAzioneClient) {
        def documento
        boolean committed = false

        transactionTemplate.execute { TransactionStatus status ->

            // ottengo il documento iterabile:
            documento = getDocumentoIterabile(true)

            WkfCfgPulsante cfgPulsante = WkfCfgPulsante.get(idCfgPulsante)

            if (cfgPulsante.competenzaInModifica) {

                // se ho le competenze in modifica, allora procedo con l'aggiornamento del documento, altrimenti do errore.
                boolean modifica = gestoreCompetenze.getCompetenze(documento).modifica
                if (modifica) {
                    aggiornaDocumentoIterabile(documento)
                } else {
                    throw new IterRuntimeException("L'utente collegato ${springSecurityService.principal?.username} non ha i diritti di modifica sul documento ${documento.toString()}")
                }
            }

            // aggiorno subito il documento solo se lo sto creando.
            // altrimenti, se ne occuperà il wkfIterService.eseguiPulsante ad eseguire l'aggiornaDocumentoIterabile.
            if (!(documento.id > 0)) {
                aggiornaDocumentoIterabile(documento)
            }

            // lo salvo
            documento.save()

            // creo l'iter se non l'ho:
            if (documento.iter == null) {
                this.wkfIterService.istanziaIter(getCfgIter(), documento)
            }

            getAd4Service().eventoLogAd4(getTestoLogPulsante(cfgPulsante, documento), documento?.id?.toString())
            this.wkfIterService.eseguiPulsante(documento, idCfgPulsante, this, idAzioneClient)


            committed = !status.isRollbackOnly()
        }

        // alcune azioni (come ad esempio la "FirmaAction.scaricaAnteprimaTesto") eseguono un Rollback
        // senza lanciare eccezione. In tal caso, non c'è bisogno di refreshare la maschera siccome non è successo niente,
        // inoltre darebbe errore di sessione hibernate non trovata.
        if (committed) {
            // aggiorno l'interfaccia:
            aggiornaMaschera(documento)

            // fuori dalla transazione calcolo i pulsanti
            aggiornaPulsanti(documento)

            successHandler.addMessage("Documento salvato.")
        } else {
            successHandler.clearMessages()
        }
    }

    /**
     * Testo del log AD4 per l'esecuzione di un pulsante dell'iter.
     * Non tutti i documenti iterabili hanno categoria, numero e registro di protocollo, quindi li controllo prima di usarli.
     */
    protected String getTestoLogPulsante(WkfCfgPulsante cfgPulsante, def documento) {
        String categoria = documento?.respondsTo("getCategoriaProtocollo") ? documento.getCategoriaProtocollo()?.descrizione : documento?.getClass()?.simpleName
        String annoNumero = documento?.respondsTo("getAnnoNumeroProtocollo") ? documento.getAnnoNumeroProtocollo() : null
        String registro = documento?.hasProperty("tipoRegistro") ? documento.tipoRegistro?.commento : null
        String estremi = [categoria, annoNumero, registro].findAll().join(" ")
        return "Esecuzione pulsante ${cfgPulsante?.pulsante?.etichetta} su ${estremi} (${documento?.id})"
    }

    abstract Collection<String> validaMaschera()

    @Command
    void invalidate() {
        if (!successHandler.saltaInvalidate) {
            self.invalidate()
        }
    }

    List<WkfCfgPulsanteDTO> getPulsanti() {
        return pulsanti
    }

    WkfIterService getWkfIterService() {
        return this.wkfIterService
    }

    GestioneDocumentiSpringSecurityService getSpringSecurityService() {
        return springSecurityService
    }

    GestioneTestiService getGestioneTestiService() {
        return this.gestioneTestiService
    }

    // usato dentro la closure di eseguiPulsante: il campo privato non è visibile dalle sottoclassi
    Ad4Service getAd4Service() {
        return this.ad4Service
    }
}
