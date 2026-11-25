package it.finmatica.gestionedocumenti.integrazioni.jwf

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

import groovy.util.logging.Slf4j
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.integrazioniws.ads.jworkflow.ExternalTaskServiceResponse
import it.finmatica.atti.integrazioniws.ads.jworkflow.MetadatoUtente
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.IDocumentoCollegato
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.soggetti.TipoSoggetto
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.login.TokenManager
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.annotation.Transactional
import org.zkoss.util.resource.Labels

import javax.sql.DataSource
import javax.xml.namespace.QName
import javax.xml.ws.BindingProvider

@Slf4j
@Service
class JWorklistService {

	public static final String PRIORITA_ALTA 		= "PA";
	public static final String PRIORITA_NORMALE 	= "PN";

	// service
	private @Autowired SpringSecurityService springSecurityService

	// connessioni al db
	private @Qualifier("ad4DataSource") @Autowired DataSource ad4DataSource

	/**
	 * Invia una notifica sulla jworklist.
	 *
	 * Questa funzione richiede una nuova transazione perché va a scrivere anche sulla DestinatariNotificheAttività
	 * in modo tale che tutta l'operazione sia "consistente": se scrivo sulla jworklist scrivo anche sulla DestinatariNotificheAttivita
	 *
	 * @param documento
	 * @param tipoNotifica
	 * @param oggetto
	 * @param testo
	 * @param utenti
	 * @param priorita
	 * @param stepCorrente
	 */
    //@NotTransactional
	// TODO[SPRINGBOOT]: ci devo mettere Transactional REQUIRED_NEW ?
	String notifica (Documento documento, String tipoNotifica, String oggetto, String testo, NotificaSoggetto soggettoNotifica, String priorita, String stepCorrente) {

        if (soggettoNotifica.utente == null) {
            return null
        }

		it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService jworklistClient = getJWorklistClient()
        String idRiferimento = getIdRiferimento(documento, tipoNotifica)
        List<String> idAttivitaCreate

        try {
            idAttivitaCreate = creaNotificaJWorklist(jworklistClient, documento, idRiferimento, soggettoNotifica.utente, oggetto, testo, priorita, stepCorrente, tipoNotifica)
            if (idAttivitaCreate?.size() > 0) {
                return idAttivitaCreate[0]
            }
        } catch (Exception e) {
            // in caso di errore di invio della notifica non interrompo nulla perché altrimenti non saprei come ricominciare a mandare i dati.
            log.warn ("Si è verificato un errore nell'invio della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Procedo con un secondo tentativo.", e)
            return null
        }

        return null
	}

	void eliminaNotifica (String idAttivitaJWorklist) {
		it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService jworklistClient  = getJWorklistClient();
		ExternalTaskServiceResponse resp = jworklistClient.deleteExternalTask(null, idAttivitaJWorklist, null, null);

		String result = resp.getResult();
		if (!resp.getResult().equals("0")) {
            log.warn("Problemi durante l'eliminazione della notifica con id_attivita=" + idAttivitaJWorklist + "\n Errore: " + resp.getErrStr());
        }
	}

	@Transactional(readOnly=true)
	String getUrlDocumento (Documento documento) {
		String tipoDocumento = getTipoDocumento(documento);
		return Impostazioni.URL_SERVER.valore+"/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento=${tipoDocumento}&id=${documento.id}&_idEnte=${documento.ente.id}";
	}

	@Transactional(readOnly=true)
	String getIdRiferimento (Documento documento, String tipoNotifica) {
		return tipoNotifica+"_"+documento.tipoOggetto.codice + "_" + documento.id
	}

	private List<String> creaNotificaJWorklist (it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService jworklistClient, IDocumentoIterabile documentoIterabile, String idRiferimento, Ad4Utente utente, String oggetto, String testoAttivita, String priorita, String stepCorrente, String tipoNotifica) {
		String utenteApplicativo 		= springSecurityService.principal.id
		boolean hasRuoloFirma			= false
		String urlRiferimento 			= (hasRuoloFirma?Impostazioni.JWORKLIST_WSDL_URL.valore+"/standalone.zul?operazione=DA_FIRMARE":"")
		String urlRiferimentoDescrizione= (hasRuoloFirma?"Documenti da firmare":"")

		String urlEsecuzione 			= getUrlDocumento(documentoIterabile);
		String tooltipUrlEsecuzione 	= "Visualizza il Documento"
		String dataAttivazione			= new Date().format("dd/MM/yyyy HH:mm:ss")
		MetadatoUtente utenteEsterno	= creaUtenteEsterno(utente.id)
		String dataArrivo 		= new Date().format("dd/MM/yyyy HH:mm:ss")
		String livelloPriorita 	= priorita;
//		String note 			= getNote(documentoIterabile)
//		String paramInitIter 	= getParamInitIter(documentoIterabile)
//		String descIter         = stepCorrente;
//		String scadenza			= getScadenza(documentoIterabile, tipoNotifica)

		if (log.debugEnabled) {
			log.debug("Invio la notifica con idRiferimento=$idRiferimento all'utente: ${utente.id}");
		}

		ExternalTaskServiceResponse resp = null;
		resp = jworklistClient.createExternalTask(utenteApplicativo
                                                , idRiferimento
                                                , oggetto			// testo del record sulla maschera della jworklist
                                                , testoAttivita 	// tooltip sul record nella maschera della jworklist
                                                , urlRiferimento
                                                , urlRiferimentoDescrizione
                                                , urlEsecuzione
                                                , tooltipUrlEsecuzione
                                                , "" // data di scadenza
                                                , "" // param init iter ???
                                                , "" // nome iter
                                                , "" // descrizione iter
                                                , ""  // colore
                                                , ""  // ordinamento
                                                , dataAttivazione
                                                , utenteEsterno
                                                , "" // categoria
                                                , "" // desktop
                                                , ""
                                                , "" // tipologia attività
                                                , "", "", "", "", ""
                                                , dataArrivo
                                                , livelloPriorita
                                                , ""    // note
        )


		// se c'è stato un problema nella comunicazione con il webservice, lo scrivo nei log.
		if (!resp.getResult().equals("0")) {
			log.warn("Problemi durante la creazione della notifica per il Documento n."+idRiferimento+" \n Errore: "+resp.getErrStr());
			return null;
		}

		if (log.debugEnabled) {
			log.debug ("Notifica (${resp.getAttivita()}) con idRiferimento: $idRiferimento inviata all'utente: ${utente.id}")
		}

		return resp.getAttivita();
	}

	/**
	 * ottiene il client webservice.
	 * @return
	 */
	private it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService getJWorklistClient (String utente = "JWF") {
		String password = new TokenManager(DataSourceUtils.getConnection(ad4DataSource), utente).getToken()

		QName SERVICE_NAME 	= new QName("http://service.wscxf.JWorkflow.finmatica.it/", "JWorklistServiceService");
		URL wsdlURL 		= new URL  (Impostazioni.JWORKLIST_WSDL_URL.valore);

		it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistServiceService ss = new it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistServiceService(wsdlURL, SERVICE_NAME);
		it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService port = ss.getJWorklistServicePort();

		java.util.Map<String, Object> requestContext = ((javax.xml.ws.BindingProvider)port).getRequestContext();

		requestContext.put(BindingProvider.USERNAME_PROPERTY, utente);
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);
		return port;
	}

	private String getTipoDocumento(IDocumentoIterabile documentoIterabile) {
		return documentoIterabile.getProperties().TIPO_OGGETTO
	}

	private String getParamInitIter (IDocumentoIterabile documentoIterabile) {
		def documento = documentoIterabile;
		if (documento instanceof IDocumentoCollegato) {
			documento = documento.documentoPrincipale;
		}

		String unita 		= documento.getSoggetto(TipoSoggetto.UO_PROTOCOLLANTE)?.unitaSo4?:""
		String dirigente 	= documento.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto?:""
		String param		= Labels.getLabel("label.determinaStandard.unita").toUpperCase()+" " + unita + "\nOGGETTO: " + documento.oggetto + "\n"+Labels.getLabel("label.determinaStandard.dirigente").toUpperCase()+" " + dirigente

		return param
	}

	private MetadatoUtente creaUtenteEsterno(String utenteAD4) {
		MetadatoUtente ue = new MetadatoUtente()
		ue.setUtenteAD4(utenteAD4)
		return ue
	}
}
