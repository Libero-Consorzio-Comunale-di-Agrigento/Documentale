package it.finmatica.gestionedocumenti.soggetti

import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzioneService
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by esasdelli on 02/02/2017.
 */
@Action
class DocumentoSoggettoAction {

    public static final String METODO_GET_UNITA		= "getUnita"
    public static final String METODO_GET_UTENTE	= "getUtente"
    public static final String METODO_HA_UTENTE 	= "haUtente"
    public static final String METODO_HA_UNITA 		= "haUnita"

    private @Autowired So4SpringSecurityService springSecurityService
    private @Autowired WkfAzioneService wkfAzioneService

    def methodMissing (String name, args) {
        // i nomi sono della forma:
        // getUnitaTIPO_SOGGETTO oppure getUtenteTIPO_SOGGETTO oppure addFirmatarioTIPO_SOGGETTO

        Documento documento = args[0]

        if (name.startsWith (METODO_GET_UNITA)) {
            Attore attore 			= new Attore ()
            String codiceSoggetto 	= name.substring(METODO_GET_UNITA.size())
            attore.unitaSo4 		= documento.getSoggetto(codiceSoggetto)?.unitaSo4
            if (attore.unitaSo4 == null) {
                throw new GestioneDocumentiRuntimeException ("Non è stata trovata l'unità per il soggetto con codice ${codiceSoggetto} sul documento con id ${documento.id}")
            }
            return attore

        } else if (name.startsWith(METODO_GET_UTENTE)) {
            Attore attore 			= new Attore ()
            String codiceSoggetto 	= name.substring(METODO_GET_UTENTE.size())
            attore.utenteAd4 		= documento.getSoggetto(codiceSoggetto)?.utenteAd4
            if (attore.utenteAd4 == null) {
                throw new GestioneDocumentiRuntimeException ("Non è stato trovato l'utente per il soggetto con codice ${codiceSoggetto} sul documento con id ${documento.id}")
            }
            return attore

        } else if (name.startsWith(METODO_HA_UTENTE)) {
            String codiceSoggetto 	= name.substring(METODO_HA_UTENTE.size())
            def utente = documento.getSoggetto(codiceSoggetto)?.utenteAd4
            return (utente != null)

        } else if (name.startsWith(METODO_HA_UNITA)) {
            String codiceSoggetto 	= name.substring(METODO_HA_UNITA.size())
            def unita = documento.getSoggetto(codiceSoggetto)?.unitaSo4
            return (unita != null)
        }

        throw new MissingMethodException(name, args)
    }

    /**
     * Crea / Aggiorna le azioni dei soggetti gestite da questa Action.
     * @param definizioneSoggettiDocumenti è una lista di mappe così composte: [tipoOggetto: "codice del WkfTipoOggetto", soggettiUtente: [..lista di codici..], soggettiUnita: [..lista di codici..]]
     */
    void creaAggiornaAzioni (mappaDocumentiSoggetti) {
        for (def doc : mappaDocumentiSoggetti) {
            WkfTipoOggetto tipoOggetto = WkfTipoOggetto.get(doc.tipoOggetto)
            for (def s : doc.soggettiUtente) {
                wkfAzioneService.insertOrUpdate(new WkfAzione([nome         : "Ritorna l'utente ${s}"
                                                               , descrizione: "Ritorna l'utente ${s} del documento ${tipoOggetto.nome}"
                                                               , nomeBean   : "documentoSoggettoAction"
                                                               , nomeMetodo : "${DocumentoSoggettoAction.METODO_GET_UTENTE}${s}"
                                                               , tipoOggetto: tipoOggetto
                                                               , tipo       : Action.TipoAzione.CALCOLO_ATTORE
                                                               , ente       : springSecurityService.principal.amministrazione]), tipoOggetto.codice)

                wkfAzioneService.insertOrUpdate(new WkfAzione([nome:          "Ritorna TRUE se è presente il soggetto ${s}"
                                                               , descrizione: "Ritorna TRUE se è presente il soggetto ${s} del documento ${tipoOggetto.nome}"
                                                               , nomeBean:    "documentoSoggettoAction"
                                                               , nomeMetodo:  "${DocumentoSoggettoAction.METODO_HA_UTENTE}${s}"
                                                               , tipoOggetto: tipoOggetto
                                                               , tipo: 	      Action.TipoAzione.CONDIZIONE
                                                               , ente:        springSecurityService.principal.amministrazione]), tipoOggetto.codice)
            }

            for (def s : doc.soggettiUnita) {
                wkfAzioneService.insertOrUpdate(new WkfAzione([ nome:           "Ritorna l'unità ${s}"
                                                                , descrizione: 	"Ritorna l'unità ${s} del documento ${tipoOggetto.nome}"
                                                                , nomeBean:    	"documentoSoggettoAction"
                                                                , nomeMetodo:  	"${DocumentoSoggettoAction.METODO_GET_UNITA}${s}"
                                                                , tipoOggetto: 	tipoOggetto
                                                                , tipo: 		Action.TipoAzione.CALCOLO_ATTORE
                                                                , ente: 		springSecurityService.principal.amministrazione]), tipoOggetto.codice)

                wkfAzioneService.insertOrUpdate(new WkfAzione([nome:            "Ritorna TRUE se è presente il soggetto ${s}"
                                                               , descrizione: 	"Ritorna TRUE se è presente il soggetto ${s} del documento ${tipoOggetto.nome}"
                                                               , nomeBean:    	"documentoSoggettoAction"
                                                               , nomeMetodo:  	"${DocumentoSoggettoAction.METODO_HA_UNITA}${s}"
                                                               , tipoOggetto: 	tipoOggetto
                                                               , tipo: 	   	    Action.TipoAzione.CONDIZIONE
                                                               , ente: 		    springSecurityService.principal.amministrazione]), tipoOggetto.codice)
            }
        }
    }
}
