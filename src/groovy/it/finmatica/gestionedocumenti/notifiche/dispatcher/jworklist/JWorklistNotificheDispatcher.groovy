package it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist


import groovy.sql.Sql
import groovy.util.logging.Slf4j
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.integrazioniws.ads.jworkflow.ExternalTaskServiceResponse
import it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService
import it.finmatica.atti.integrazioniws.ads.jworkflow.MetadatoUtente
import it.finmatica.atti.integrazioniws.ads.smartdesktop.JWorklist
import it.finmatica.atti.integrazioniws.ads.smartdesktop.Task
import it.finmatica.atti.integrazioniws.ads.smartdesktop.TaskBottone
import it.finmatica.atti.integrazioniws.ads.smartdesktop.TaskDettaglio
import it.finmatica.atti.integrazioniws.ads.smartdesktop.TaskResponse
import it.finmatica.atti.integrazioniws.ads.smartdesktop.TaskResponseMultipla
import it.finmatica.atti.integrazioniws.ads.smartdesktop.TaskUtenti
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.notifiche.Notifica
import it.finmatica.gestionedocumenti.notifiche.NotificaAttivitaJWorklist
import it.finmatica.gestionedocumenti.notifiche.NotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import it.finmatica.login.TokenManager
import it.finmatica.so4.login.So4SpringSecurityService
import org.apache.cxf.common.util.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import javax.xml.bind.JAXBElement
import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.namespace.QName
import javax.xml.ws.BindingProvider
import java.sql.SQLException

/**
 * Created by esasdelli on 07/04/2017.
 */

@Slf4j
class JWorklistNotificheDispatcher implements NotificheDispatcher {

    public static final String ATTIVITA_TODO = "TODO"

    @Qualifier("ad4DataSource")
    @Autowired
    DataSource ad4DataSource
    @Qualifier("dataSource_gdm")
    @Autowired
    DataSource dataSource_gdm

    @Autowired
    So4SpringSecurityService springSecurityService

    @Autowired
    NotificaJWorklistBuilder notificaJWorklistBuilder

    @Value("\${finmatica.gestionedocumenti.jworklist.wsdl:}")
    String urlWsdlWebService

    @Value("\${finmatica.gestionedocumenti.smartdesktop.wsdl:}")
    String urlSmartDesktopWsdlWebService

    @Value("\${finmatica.gestionedocumenti.jworklist.utente:JWF}")
    String utenteWebService

    @Autowired
    RepositoryRegoleCalcolo repositoryRegoleCalcolo

    @Override
    @Transactional
    void invia(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {
        def jworklistClient = getJWLClient()

        for (NotificaSoggetto destinatario : destinatari) {

            // non invio notifiche a destinatari senza utente ad4
            if (destinatario.utente == null) {
                continue
            }

            boolean notificaPresente = (NotificaAttivitaJWorklist.perIdRiferimento(notifica.tipoNotifica, repositoryRegoleCalcolo.getIdRiferimento(documento), destinatario.utente.domainObject).count() > 0)
            if (notificaPresente) {
                destinatari.remove(destinatario)
            }
        }
        List<NotificaJWorklist> notificaJWorklists = notificaJWorklistBuilder.creaNotificaJWorklist(notifica, documento, destinatari, testo, oggetto, messaggioTODO)

        for (NotificaJWorklist notificaJWorklist : notificaJWorklists) {
            if (!StringUtils.isEmpty(messaggioTODO)) {
                notificaJWorklist.espressione = ATTIVITA_TODO
            }

            HashMap<String, String> idAttivitaJWorklistMap = inviaNotificaWL(jworklistClient, notificaJWorklist)
            if (idAttivitaJWorklistMap != null && idAttivitaJWorklistMap.size() > 0) {
                for (Map.Entry idAttivitaJWorklist : idAttivitaJWorklistMap.entrySet()) {
                    if (idAttivitaJWorklist.getKey() != null) {
                        aggiungiNotificaJWorklist(notifica, documento, idAttivitaJWorklist.getValue(), idAttivitaJWorklist.getKey())
                    }
                }
            }
        }
    }


    @Override
    @Transactional
    void aggiorna(Notifica notifica, Object documento, List<NotificaSoggetto> destinatari, String oggetto, String testo, String messaggioTODO, boolean async) {

        it.finmatica.gestionedocumenti.ws.smartdesktop.JWorklist jworklistClient = getJWorklistClientProtocolloSmartDesktop()

        List<NotificaJWorklist> notificaJWorklists = notificaJWorklistBuilder.creaNotificaJWorklist(notifica, documento, destinatari, testo, oggetto, messaggioTODO)

        for (NotificaJWorklist notificaJWorklist : notificaJWorklists) {
            if (!StringUtils.isEmpty(messaggioTODO)) {
                notificaJWorklist.espressione = ATTIVITA_TODO
            }

            HashMap<String, String> idAttivitaJWorklistMap = aggiornaNotificaWL(jworklistClient, notificaJWorklist, documento, destinatari, notifica)
            if (idAttivitaJWorklistMap != null && idAttivitaJWorklistMap.size() > 0) {
                for (Map.Entry idAttivitaJWorklist : idAttivitaJWorklistMap.entrySet()) {
                    if (idAttivitaJWorklist.getKey() != null) {
                        aggiungiNotificaJWorklist(notifica, documento, idAttivitaJWorklist.getValue(), idAttivitaJWorklist.getKey())
                    }
                }
            }
        }
    }

    private Map<String, MetadatoUtente> aggiornaNotificaWL(jworklistClient, NotificaJWorklist notificaJWorklist, Object documento, List<NotificaSoggetto> destinatari, Notifica notifica) {
        return aggiornaNotificaSmartDesktop(jworklistClient, notificaJWorklist, documento, destinatari, notifica)
    }


    it.finmatica.gestionedocumenti.ws.smartdesktop.JWorklist getJWorklistClientProtocolloSmartDesktop() {
        String password = new TokenManager(DataSourceUtils.getConnection(ad4DataSource), utenteWebService).getToken()

        QName SERVICE_NAME = new QName("http://service.wscxf.JWorkflow.finmatica.it/", "JWorklistService")
        it.finmatica.gestionedocumenti.ws.smartdesktop.JWorklistService ss = new it.finmatica.gestionedocumenti.ws.smartdesktop.JWorklistService(new URL(urlSmartDesktopWsdlWebService))
        it.finmatica.gestionedocumenti.ws.smartdesktop.JWorklist port = ss.getJWorklistPort()

        java.util.Map<String, Object> requestContext = ((javax.xml.ws.BindingProvider) port).getRequestContext()

        requestContext.put(BindingProvider.USERNAME_PROPERTY, utenteWebService)
        requestContext.put(BindingProvider.PASSWORD_PROPERTY, password)
        return port
    }


    //TODO 02/12/2020 DA chiamata di Mannella per l'elimina occorre chiamare una nuovo metodo di autenticazione che mi fornira' a breve

    @Override
    @Transactional
    void elimina(String tipoNotifica, String idRiferimento, Ad4Utente utente) {

        List<NotificaAttivitaJWorklist> listaAttivita = NotificaAttivitaJWorklist.perIdRiferimento(idRiferimento, tipoNotifica, utente).list()
        boolean smartDesktop = isPresenteSmartDesktop()
        def jWorkListClient = getJWLClient()

        for (NotificaAttivitaJWorklist attivita : listaAttivita) {

            if (smartDesktop) {
                eliminaTaskSmartDesktop(jWorkListClient, attivita.idAttivitaJWorklist)
            } else {
                eliminaTask(jWorkListClient, attivita.idAttivitaJWorklist)
            }
            attivita.delete()
        }

        if (tipoNotifica == null) {
            if (smartDesktop) {
                eliminaTaskSmartDesktopPerIdDocumentoEsterno(jWorkListClient, idRiferimento)
            } else {
                eliminaTaskPerIdDocumentoEsterno(jWorkListClient, idRiferimento)
            }
        }
    }

    //web service smartdesktop
    void eliminaTaskSmartDesktop(jWorkListClient, String idAttivitaJWorklist) {

        TaskResponse response = null
        try {
            response = jWorkListClient.eliminaTaskUser(Long.valueOf(idAttivitaJWorklist), springSecurityService.currentUser.id)
        }
        catch (NoSuchMethodException e) {

            response = jWorkListClient.eliminaTask(Long.valueOf(idAttivitaJWorklist))
            log.info("Chiamato il vecchio metodo per eliminare il task (senza utente) " + e.message)
        }

        if (log.debugEnabled) {
            log.debug("Notifica JWorklist elimina con id: ${idAttivitaJWorklist}")
        }

        if (response.getCodiceErrore() != "0") {
            log.warn("Problemi durante l'eliminazione della notifica per il Task n." + idAttivitaJWorklist + " \n Errore: " + response.getDescrizioneErrore())
            return
        }
    }

    /**
     * Web service smartdesktop
     */
    void eliminaTaskSmartDesktopPerIdDocumentoEsterno(jWorkListClient, String idRiferimento) {

        TaskResponse response = null
        try {
            response = jWorkListClient.eliminaTaskUserByRif(idRiferimento, springSecurityService.currentUser.id)
        }
        catch (NoSuchMethodException e) {

            response = jWorkListClient.eliminaTaskByRif(idRiferimento)
            log.info("Chiamato il vecchio metodo per eliminare il task (senza utente) " + e.message)
        }

        if (log.debugEnabled) {
            log.debug("Notifica JWorklist elimina con id Riferimento: ${idRiferimento}")
        }

        if (response.getCodiceErrore() != "0") {
            log.warn("Problemi durante l'eliminazione della notifica per il Riferimento n." + idRiferimento + " \n Errore: " + response.getDescrizioneErrore())
            return
        }
    }

//web service jworklist
    void eliminaTaskPerIdDocumentoEsterno(jWorkListClient, String idRiferimento) {

        ExternalTaskServiceResponse response = jWorkListClient.deleteExternalTask("", "", idRiferimento, "")

        //ExternalTaskServiceResponse response = getJWorklistClient().deleteExternalTask("", attivita.idAttivitaJWorklist, "", "")

        if (log.debugEnabled) {
            log.debug("Notifica JWorklist elimina con id riferimento: ${idRiferimento}")
        }

        if (response.result != "0") {
            log.warn("Problemi durante l'eliminazione della notifica per il Riferimento n." + idRiferimento + " \n Errore: " + response.getErrStr())
            return
        }
    }

    void eliminaTask(jWorkListClient, String idAttivitaJWorklist) {

        ExternalTaskServiceResponse response = jWorkListClient.deleteExternalTask("", idAttivitaJWorklist, "", "")

        if (log.debugEnabled) {
            log.debug("Notifica JWorklist elimina con id task: ${idAttivitaJWorklist}")
        }

        if (response.result != "0") {
            log.warn("Problemi durante l'eliminazione della notifica per il Task n." + idAttivitaJWorklist + " \n Errore: " + response.getErrStr())
            return
        }
    }

    @Override
    boolean isNotificaPresente(String tipoNotifica, Object documento, Ad4Utente utente) {
        return NotificaAttivitaJWorklist.perIdRiferimento(repositoryRegoleCalcolo.getIdRiferimento(documento), tipoNotifica, utente).count() > 0
    }

    private Map<String, MetadatoUtente> inviaNotificaSmartDesktop(it.finmatica.atti.integrazioniws.ads.smartdesktop.JWorklist jworklistClient, NotificaJWorklist notificaJWorklist) {
        try {
            GregorianCalendar c = new GregorianCalendar()
            XMLGregorianCalendar dataScadenza = null
            if (notificaJWorklist.dataScadenza != null) {
                c.setTime(notificaJWorklist.dataScadenza)
                dataScadenza = DatatypeFactory.newInstance().newXMLGregorianCalendar(c)
            }
            c = new GregorianCalendar()
            c.setTime(new Date())
            XMLGregorianCalendar dataOggi = DatatypeFactory.newInstance().newXMLGregorianCalendar(c)
            Task task = new Task()
            //task.setUtente(utentiEsterni?:"")
            task.setEnte(springSecurityService.principal.ente.descrizione)
            task.setIdRiferimentoEsterno(notificaJWorklist.idRiferimento ?: "")
            task.setDescrizione(notificaJWorklist.testo ?: "")
            task.setTooltipDescrizione(notificaJWorklist.tooltip ?: "")
            task.setUrlVisualizzazione(notificaJWorklist.urlRiferimento ?: "")
            task.setDescrizioneUrlVisualizzazione(notificaJWorklist.tooltipUrlRiferimento ?: "")
            task.setUrlEsecuzione(notificaJWorklist.urlEsecuzione ?: "")
            task.setDescrizioneUrlEsecuzione(notificaJWorklist.tooltipUrlEsecuzione ?: "")
            task.setDataScadenza(dataScadenza)
            task.setParamInitIter(notificaJWorklist.paramInitIter ?: "")
            task.setNomeIter(notificaJWorklist.nomeIter ?: "")
            task.setDescrizioneIter(notificaJWorklist.descrizioneIter ?: "")
            task.setColore(notificaJWorklist.colore ?: "")
            task.setOrdinamento(notificaJWorklist.ordinamento ?: "")
            task.setDataAttivazione(dataOggi)
            task.setCategoria(notificaJWorklist.categoria ?: "")
            task.setDesktop(notificaJWorklist.desktop ?: "")
            task.setStato(notificaJWorklist.stato ?: "")
            task.setTipologia(notificaJWorklist.tipologia ?: "")
            task.setTipologiaDescrizione(notificaJWorklist.tipologiaDescrizione ?: "")
            task.setDatiApplicativi1(notificaJWorklist.datiApplicativi1 ?: "")
            task.setDatiApplicativi2(notificaJWorklist.datiApplicativi2 ?: "")
            task.setDatiApplicativi3(notificaJWorklist.datiApplicativi3 ?: "")
            task.setTaskTipoTODO(((notificaJWorklist.espressione ?: "").equals("TODO") ? 1 : 0))
            task.setMessaggioTaskTipoTODO(notificaJWorklist.messaggioToDo ?: "")
            task.setDataArrivo(dataOggi)
            task.setLivelloPriorita(notificaJWorklist.priorita ?: "")
            task.setNote(notificaJWorklist.note ?: "")
            task.setUrlAllegatiDinamici(notificaJWorklist.urlAllegatiDinamici ?: "")
            //String urlAllegatiDinamici
            if (notificaJWorklist.utenteEsternoList?.size() > 0) {
                for (MetadatoUtente utenteEsterno : notificaJWorklist.utenteEsternoList) {
                    TaskUtenti taskUtente = new TaskUtenti()
                    taskUtente.setUtente(utenteEsterno.utenteAD4)
                    task.taskUtenti.add(taskUtente)
                }
            }
            if (notificaJWorklist.ordinamentoStringaValore != null) {
                task.ordinamentoStringaLabel = notificaJWorklist.ordinamentoStringaLabel
                task.ordinamentoStringaValore = notificaJWorklist.ordinamentoStringaValore
            }
            if (notificaJWorklist.ordinamentoNumeroValore != null) {
                task.ordinamentoNumeroLabel = notificaJWorklist.ordinamentoNumeroLabel
                task.ordinamentoNumeroValore = notificaJWorklist.ordinamentoNumeroValore
            }
            if (notificaJWorklist.ordinamentoDataValore != null) {
                task.ordinamentoDataLabel = notificaJWorklist.ordinamentoDataLabel
                c.setTime(notificaJWorklist.ordinamentoDataValore)
                task.ordinamentoDataValore = DatatypeFactory.newInstance().newXMLGregorianCalendar(c)
            }
            if (notificaJWorklist.dettagliNotificaJWorklistList?.size() > 0) {
                for (DettaglioNotificaJWorklist dettaglioNotificaJWorklist : notificaJWorklist.dettagliNotificaJWorklistList) {
                    TaskDettaglio d = new TaskDettaglio()
                    d.nomeDettaglio = dettaglioNotificaJWorklist.nomeDettaglio
                    d.valoreDettaglio = dettaglioNotificaJWorklist.valoreDettaglio
                    d.url = dettaglioNotificaJWorklist.url
                    task.taskDettaglio.add(d)
                }
            }
            if (notificaJWorklist.bottoniNotificaJWorklistList?.size() > 0) {
                for (BottoneNotificaJWorklist bottoneNotifica : notificaJWorklist.bottoniNotificaJWorklistList) {
                    TaskBottone b = new TaskBottone()
                    b.multiplo = bottoneNotifica.multiplo
                    b.icona = bottoneNotifica.icona
                    b.tooltip = bottoneNotifica.tooltip
                    b.tipologia = bottoneNotifica.tipologia
                    b.urlAzione = bottoneNotifica.urlAzione
                    b.etichetta = bottoneNotifica.etichetta
                    b.parametri = bottoneNotifica.parametri
                    b.identificativoRiferimento = bottoneNotifica.identificativoRiferimento
                    task.taskBottone.add(b)
                }
            }
            TaskResponseMultipla responseMultipla = jworklistClient.inserisciMultiTask(task)
            HashMap<String, String> returnMap = new HashMap<String, String>()
            List<TaskResponse> taskResponseList = responseMultipla.getTaskResponses()

            // #80630 - se c'è stato un problema nella comunicazione con il webservice, lo scrivo nei log.
            if (taskResponseList.size()<=0) {
                log.warn("Problemi durante la creazione della notifica per il Documento n." + notificaJWorklist.idRiferimento + " \n Nessuna task response ritornata dal webservice")
                return null
            }

            for (TaskResponse resp : taskResponseList) {
                // se c'è stato un problema nella comunicazione con il webservice, lo scrivo nei log.
                if (!resp.getCodiceErrore().equals("0")) {
                    log.warn("Problemi durante la creazione della notifica per il Documento n." + notificaJWorklist.idRiferimento + " \n Errore: " + resp.getCodiceErrore() + " - " + resp.getDescrizioneErrore())
                    break
                }

                if (log.debugEnabled) {
                    log.debug("Notifica JWorklist inviata con id: ${resp.getIdTask()}, all'utente: ${resp.getUtente()}, testo: ${notificaJWorklist.testo}")
                }
                int index = notificaJWorklist.notificaSoggettoList.findIndexOf { it.utente.id == resp.getUtente() }
                returnMap.put(String.valueOf(resp.getIdTask()), notificaJWorklist.notificaSoggettoList.get(index))
            }

            return returnMap
        } catch (Exception e) {
            // in caso di errore in invio della notifica, ignoro l'eccezione perché voglio comunque poter inviare tutte le altre.
            log.warn("Problemi durante la creazione della notifica per il Documento n." + notificaJWorklist.idRiferimento, e)
            return null
        }
    }

    private Map<String, MetadatoUtente> aggiornaNotificaSmartDesktop(it.finmatica.gestionedocumenti.ws.smartdesktop.JWorklist jworklistClient,
                                                                     NotificaJWorklist notificaJWorklist, Object documento, List<NotificaSoggetto> destinatari, Notifica notifica) {
        try {
            GregorianCalendar c = new GregorianCalendar()
            XMLGregorianCalendar dataScadenza = null
            if (notificaJWorklist.dataScadenza != null) {
                c.setTime(notificaJWorklist.dataScadenza)
                dataScadenza = DatatypeFactory.newInstance().newXMLGregorianCalendar(c)
            }
            c = new GregorianCalendar()
            c.setTime(new Date())
            XMLGregorianCalendar dataOggi = DatatypeFactory.newInstance().newXMLGregorianCalendar(c)
            it.finmatica.gestionedocumenti.ws.smartdesktop.Task task = new it.finmatica.gestionedocumenti.ws.smartdesktop.Task()
            it.finmatica.gestionedocumenti.ws.smartdesktop.ObjectFactory factory = new it.finmatica.gestionedocumenti.ws.smartdesktop.ObjectFactory()
            task.setEnte(springSecurityService.principal.ente.descrizione)
            task.setIdRiferimentoEsterno(notificaJWorklist.idRiferimento ?: "")
            task.setDescrizione(notificaJWorklist.testo ?: "")
            task.setTooltipDescrizione(notificaJWorklist.tooltip ?: "")
            task.setUrlVisualizzazione(notificaJWorklist.urlRiferimento ?: "")
            task.setDescrizioneUrlVisualizzazione(notificaJWorklist.tooltipUrlRiferimento ?: "")
            task.setUrlEsecuzione(notificaJWorklist.urlEsecuzione ?: "")
            task.setDescrizioneUrlEsecuzione(notificaJWorklist.tooltipUrlEsecuzione ?: "")
            if (dataScadenza != null) {
                JAXBElement<XMLGregorianCalendar> dataScadenzaXml = factory.createTaskDataScadenza(dataScadenza)
                task.setDataScadenza(dataScadenzaXml)
            }
            task.setParamInitIter(notificaJWorklist.paramInitIter ?: "")
            task.setNomeIter(notificaJWorklist.nomeIter ?: "")
            task.setDescrizioneIter(notificaJWorklist.descrizioneIter ?: "")
            task.setColore(notificaJWorklist.colore ?: "")
            task.setOrdinamento(notificaJWorklist.ordinamento ?: "")
            task.setDataAttivazione(dataOggi)
            task.setCategoria(notificaJWorklist.categoria ?: "")
            task.setDesktop(notificaJWorklist.desktop ?: "")
            task.setStato(notificaJWorklist.stato ?: "")
            task.setTipologia(notificaJWorklist.tipologia ?: "")
            task.setTipologiaDescrizione(notificaJWorklist.tipologiaDescrizione ?: "")
            task.setDatiApplicativi1(notificaJWorklist.datiApplicativi1 ?: "")
            task.setDatiApplicativi2(notificaJWorklist.datiApplicativi2 ?: "")
            task.setDatiApplicativi3(notificaJWorklist.datiApplicativi3 ?: "")
            task.setTaskTipoTODO(((notificaJWorklist.espressione ?: "").equals("TODO") ? 1 : 0))
            task.setMessaggioTaskTipoTODO(notificaJWorklist.messaggioToDo ?: "")
            task.setDataArrivo(dataOggi)
            task.setLivelloPriorita(notificaJWorklist.priorita ?: "")
            task.setNote(notificaJWorklist.note ?: "")
            JAXBElement<String> urlAllegatiDinamici = factory.createTaskUrlAllegatiDinamici(notificaJWorklist.urlAllegatiDinamici ?: "")
            task.setUrlAllegatiDinamici(urlAllegatiDinamici)
            if (notificaJWorklist.utenteEsternoList?.size() > 0) {
                for (MetadatoUtente utenteEsterno : notificaJWorklist.utenteEsternoList) {
                    it.finmatica.gestionedocumenti.ws.smartdesktop.TaskUtenti taskUtente = new it.finmatica.gestionedocumenti.ws.smartdesktop.TaskUtenti()
                    taskUtente.setUtente(utenteEsterno.utenteAD4)
                    task.taskUtenti.add(taskUtente)
                }
            }
            if (notificaJWorklist.ordinamentoStringaValore != null) {
                JAXBElement<String> ordinamentoStringaLabel = factory.createTaskOrdinamentoStringaLabel(notificaJWorklist.ordinamentoStringaLabel)
                task.ordinamentoStringaLabel = ordinamentoStringaLabel
                task.ordinamentoStringaValore = notificaJWorklist.ordinamentoStringaValore
            }
            if (notificaJWorklist.ordinamentoNumeroValore != null) {
                JAXBElement<String> ordinamentoNumeroLabel = factory.createTaskOrdinamentoNumeroLabel(notificaJWorklist.ordinamentoNumeroLabel)
                task.ordinamentoNumeroLabel = ordinamentoNumeroLabel
                task.ordinamentoNumeroValore = notificaJWorklist.ordinamentoNumeroValore
            }
            if (notificaJWorklist.ordinamentoDataValore != null) {
                JAXBElement<String> ordinamentoDataLabel = factory.createTaskOrdinamentoDataLabel(notificaJWorklist.ordinamentoDataLabel)
                task.ordinamentoDataLabel = ordinamentoDataLabel
                c.setTime(notificaJWorklist.ordinamentoDataValore)
                task.ordinamentoDataValore = DatatypeFactory.newInstance().newXMLGregorianCalendar(c)
            }
            if (notificaJWorklist.dettagliNotificaJWorklistList?.size() > 0) {
                for (DettaglioNotificaJWorklist dettaglioNotificaJWorklist : notificaJWorklist.dettagliNotificaJWorklistList) {
                    it.finmatica.gestionedocumenti.ws.smartdesktop.TaskDettaglio d = new it.finmatica.gestionedocumenti.ws.smartdesktop.TaskDettaglio()
                    d.nomeDettaglio = dettaglioNotificaJWorklist.nomeDettaglio
                    d.valoreDettaglio = dettaglioNotificaJWorklist.valoreDettaglio
                    d.url = dettaglioNotificaJWorklist.url
                    task.taskDettaglio.add(d)
                }
            }
            if (notificaJWorklist.bottoniNotificaJWorklistList?.size() > 0) {
                for (BottoneNotificaJWorklist bottoneNotifica : notificaJWorklist.bottoniNotificaJWorklistList) {
                    it.finmatica.gestionedocumenti.ws.smartdesktop.TaskBottone b = new it.finmatica.gestionedocumenti.ws.smartdesktop.TaskBottone()
                    b.multiplo = bottoneNotifica.multiplo
                    b.icona = bottoneNotifica.icona
                    b.tooltip = bottoneNotifica.tooltip
                    b.tipologia = bottoneNotifica.tipologia
                    b.urlAzione = bottoneNotifica.urlAzione
                    b.etichetta = bottoneNotifica.etichetta
                    b.parametri = bottoneNotifica.parametri
                    b.identificativoRiferimento = bottoneNotifica.identificativoRiferimento
                    task.taskBottone.add(b)
                }
            }

            HashMap<String, String> returnMap = new HashMap<String, String>()

            //Richiamo query per id riferimento
            for (NotificaSoggetto destinatario : destinatari) {
                List<NotificaAttivitaJWorklist> listaAttivita = NotificaAttivitaJWorklist.perIdRiferimento(repositoryRegoleCalcolo.getIdRiferimento(documento), notifica.tipoNotifica, destinatario.utente?.domainObject).list()

                if (listaAttivita?.size() > 0) {
                    it.finmatica.gestionedocumenti.ws.smartdesktop.TaskResponse response = jworklistClient.sostituisciTask(listaAttivita.get(0)?.idAttivitaJWorklist, "TASK", task)

                    // se c'è stato un problema nella comunicazione con il webservice, lo scrivo nei log.
                    if (!response.getCodiceErrore().equals("0")) {
                        log.warn("Problemi durante l'aggiornamento della notifica per il Documento n." + notificaJWorklist.idRiferimento + " \n Errore: " + response.getCodiceErrore() + " - " + response.getDescrizioneErrore())
                    }

                    if (log.debugEnabled) {
                        log.debug("Notifica JWorklist inviata con id: ${response.getIdTask()}, all'utente: ${resp.getUtente()}, testo: ${notificaJWorklist.testo}")
                    }
                    //FIXME probabilmente questo pezzo è da togliere in quanto non dovrebbe essere mai verificato in aggiornamento
                    int index = notificaJWorklist.notificaSoggettoList.findIndexOf { it.utente.id == response.getUtente() }
                    if (index >= 0) {
                        returnMap.put(String.valueOf(response.getIdTask()), notificaJWorklist.notificaSoggettoList.get(index))
                    }
                } else {
                    log.warn("Problemi durante l'aggiornamento della notifica per il Documento n." + notificaJWorklist.idRiferimento + " \n Errore: Nessuna attività trovata")
                }

            }
            return returnMap
        } catch (Exception e) {
            // in caso di errore in invio della notifica, ignoro l'eccezione perché voglio comunque poter inviare tutte le altre.
            log.warn("Problemi durante la creazione della notifica per il Documento n." + notificaJWorklist.idRiferimento, e)
            return null
        }
    }

    private Map<String, MetadatoUtente> inviaNotificaWL(jworklistClient, NotificaJWorklist notificaJWorklist) {
        if (isPresenteSmartDesktop()) {
            return inviaNotificaSmartDesktop(jworklistClient, notificaJWorklist)
        } else {
            return inviaNotifica(jworklistClient, notificaJWorklist)
        }
    }

    private Map<String, MetadatoUtente> inviaNotifica(JWorklistService jworklistClient, NotificaJWorklist notificaJWorklist) {
        HashMap<String, String> returnMap = new HashMap<String, String>()
        try {
            for (MetadatoUtente utenteEsterno : notificaJWorklist.utenteEsternoList) {
                ExternalTaskServiceResponse resp = jworklistClient.createExternalTask(springSecurityService.principal.id
                        , notificaJWorklist.idRiferimento ?: ""
                        , notificaJWorklist.testo ?: ""
                        , notificaJWorklist.tooltip ?: ""
                        , notificaJWorklist.urlRiferimento ?: ""
                        , notificaJWorklist.tooltipUrlRiferimento ?: ""
                        , notificaJWorklist.urlEsecuzione ?: ""
                        , notificaJWorklist.tooltipUrlEsecuzione ?: ""
                        , notificaJWorklist.dataScadenza?.format("dd/MM/yyyy HH:mm:ss") ?: ""
                        , notificaJWorklist.paramInitIter ?: ""
                        , notificaJWorklist.nomeIter ?: ""
                        , notificaJWorklist.descrizioneIter ?: ""
                        , notificaJWorklist.colore ?: ""
                        , notificaJWorklist.ordinamento ?: ""
                        , new Date().format("dd/MM/yyyy HH:mm:ss")
                        , utenteEsterno
                        //, utenteEsterno.utenteAD4?:""
                        , notificaJWorklist.categoria ?: ""
                        , notificaJWorklist.desktop ?: ""
                        , notificaJWorklist.stato ?: ""
                        , notificaJWorklist.tipologia ?: ""
                        , notificaJWorklist.datiApplicativi1 ?: ""
                        , notificaJWorklist.datiApplicativi2 ?: ""
                        , notificaJWorklist.datiApplicativi3 ?: ""
                        , notificaJWorklist.espressione ?: ""
                        , notificaJWorklist.messaggioToDo ?: ""
                        , new Date().format("dd/MM/yyyy HH:mm:ss") // data di attivazione
                        , notificaJWorklist.priorita ?: ""
                        , notificaJWorklist.note ?: ""
                )

                // se c'è stato un problema nella comunicazione con il webservice, lo scrivo nei log.
                if (resp.result != "0") {
                    log.warn("Problemi durante la creazione della notifica per il Documento n." + notificaJWorklist.idRiferimento + " \n Errore: " + resp.getErrStr())
                    break
                }

                if (log.debugEnabled) {
                    log.debug("Notifica JWorklist inviata con id: ${resp.attivita[0]}, all'utente: ${utenteEsterno.utenteAD4}, testo: ${notificaJWorklist.testo}")
                }
                returnMap.put(resp.attivita[0], notificaJWorklist.notificaSoggettoList.get(0))
            }

            return returnMap
        } catch (Exception e) {
            // in caso di errore in invio della notifica, ignoro l'eccezione perché voglio comunque poter inviare tutte le altre.
            log.warn("Problemi durante la creazione della notifica per il Documento n." + notificaJWorklist.idRiferimento, e)
            return null
        }
    }

    private void aggiungiNotificaJWorklist(Notifica notifica, Object documento, NotificaSoggetto soggettoNotifica, String idAttivitaJWorklist) {
        NotificaAttivitaJWorklist notificaAttivita = new NotificaAttivitaJWorklist()
        notificaAttivita.idRiferimento = repositoryRegoleCalcolo.getIdRiferimento(documento)
        notificaAttivita.idAttivitaJWorklist = idAttivitaJWorklist
        notificaAttivita.tipoNotifica = notifica.tipoNotifica
        notificaAttivita.utente = soggettoNotifica.utente?.domainObject
        notificaAttivita.unitaSo4 = soggettoNotifica.unitaSo4?.domainObject
        notificaAttivita.save()
    }

    /**
     * ottiene il client webservice
     * @return
     */
    def getJWLClient() {
        if (isPresenteSmartDesktop()) {
            return getJWorklistClientSmartDesktop()
        } else {
            return getJWorklistClient()
        }
    }

    JWorklist getJWorklistClientSmartDesktop() {
        String password = new TokenManager(DataSourceUtils.getConnection(ad4DataSource), utenteWebService).getToken()

        QName SERVICE_NAME = new QName("http://service.wscxf.JWorkflow.finmatica.it/", "JWorklistService")
        it.finmatica.atti.integrazioniws.ads.smartdesktop.JWorklistService ss = new it.finmatica.atti.integrazioniws.ads.smartdesktop.JWorklistService(new URL(urlSmartDesktopWsdlWebService))
        it.finmatica.atti.integrazioniws.ads.smartdesktop.JWorklist port = ss.getJWorklistPort()

        java.util.Map<String, Object> requestContext = ((javax.xml.ws.BindingProvider) port).getRequestContext()

        requestContext.put(BindingProvider.USERNAME_PROPERTY, utenteWebService)
        requestContext.put(BindingProvider.PASSWORD_PROPERTY, password)
        return port
    }

    JWorklistService getJWorklistClient() {
        String password = new TokenManager(DataSourceUtils.getConnection(ad4DataSource), utenteWebService).getToken()

        QName SERVICE_NAME = new QName("http://service.wscxf.JWorkflow.finmatica.it/", "JWorklistServiceService")
        it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistServiceService ss = new it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistServiceService(new URL(urlWsdlWebService), SERVICE_NAME)
        it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService port = ss.getJWorklistServicePort()

        java.util.Map<String, Object> requestContext = ((javax.xml.ws.BindingProvider) port).getRequestContext()

        requestContext.put(BindingProvider.USERNAME_PROPERTY, utenteWebService)
        requestContext.put(BindingProvider.PASSWORD_PROPERTY, password)
        return port
    }

    boolean isPresenteSmartDesktop() {
        try {
            Sql sql = new Sql(dataSource_gdm)
            String select = """select ag_utilities.EXISTS_SMART_DESKTOP from dual"""
            boolean presente
            sql.query(select) { resultSet ->
                while (resultSet.next()) {
                    presente = resultSet.getString(1).equals("1")
                }
            }
            return presente
        } catch (SQLException e) {
            throw new GestioneDocumentiRuntimeException(e)
        }
    }
}
