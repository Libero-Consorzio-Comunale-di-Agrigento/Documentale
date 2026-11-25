package it.finmatica.gestionedocumenti.documenti.beans

import groovy.util.logging.Slf4j
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.firmadigitale.FirmaDigitaleFile
import it.finmatica.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.firmadigitale.FirmaDigitaleTransazioneService
import it.finmatica.firmadigitale.utils.BlobInputStream
import it.finmatica.firmadigitale.utils.FirmaDigitaleUrlBuilder
import it.finmatica.gestionedocumenti.HibernateUtils
import it.finmatica.gestionedocumenti.documenti.Allegato
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.DocumentoService
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.StatoFirma
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.integrazioni.firma.FineFirmaCallback
import it.finmatica.gestionedocumenti.integrazioni.firma.GestioneDocumentiFirmaService
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.smartdoc.api.DocumentaleRuntimeException
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.persistence.EntityManagerFactory
import javax.sql.rowset.serial.SerialBlob

/**
 * TODO[SPRINGBOOT]: Questa classe potrebbe essere anche eliminata, di fatto delega tutte le chiamate al firmaDigitaleTransazioneService
 * @author esasdelli
 */
@Slf4j
class GestoreTransazioneFirma {

    @Autowired
    private FirmaDigitaleTransazioneService firmaDigitaleTransazioneService
    @Autowired
    private SpringSecurityService springSecurityService
    @Autowired
    private DocumentoService documentoService
    @Autowired
    private FineFirmaCallback fineFirmaCallback

    FirmaDigitaleTransazione inizializzaTransazioneFirma(Ad4Utente firmatario) {
        firmaDigitaleTransazioneService.inizializzaTransazioneFirma(firmatario)
        return firmaDigitaleTransazioneService.transazioneCorrente
    }

    void addFileDaFirmare(String idRiferimento, String filename, InputStream is) {
        firmaDigitaleTransazioneService.aggiungiFileDaFirmare(idRiferimento, filename, is)
    }

    void addFileDaFirmare(String idRiferimento, String filename, InputStream is, int numFirma) {
        firmaDigitaleTransazioneService.aggiungiFileDaFirmare(idRiferimento, filename, is, numFirma)
    }  //TODO issue 71114

    FirmaDigitaleTransazione getTransazioneCorrente() {
        return firmaDigitaleTransazioneService.transazioneCorrente
    }

    /**
     * Termina la transazione di firma costruendo l'url della popup di firma digitale
     * @return l'url della popup di firma digitale
     */
    String finalizzaTransazioneFirma() {
        return finalizzaTransazioneFirma(false, Impostazioni.FIRMA_PADES.abilitato)
    }

    String finalizzaTransazioneFirma(boolean firmaPades) {
        return finalizzaTransazioneFirma(false, firmaPades)
    }

    String finalizzaTransazioneFirma(boolean firmaAutografa, boolean firmaPades) {
        return buildUrlFirma(firmaAutografa, firmaPades)
    }

    /**
     * Termina la transazione di firma costruendo l'url della popup di fine firma.
     * @return l'url della popup di fine firma (non passa dalla popup di firma digitale)
     */
    void finalizzaTransazioneFirmaAutografa() {
        verificaAlmenoUnFileDaFirmare()

        FirmaDigitaleTransazione transazione = firmaDigitaleTransazioneService.transazioneCorrente

        //def urlFirma = urlFineFirma
        log.debug("Url popup di firma autografa: ${urlFirma}")

        transazione.dataFirma = new Date()
        transazione.dateCreated = new Date()
        transazione.lastUpdated = new Date()
        transazione.save()

        // inserito il refresh della transazione per ricaricare il blob degli allegati
        transazione = firmaDigitaleTransazioneService.transazioneCorrente

        // per firma autografa invece che firmare prendo il file convertito in PDF e basta
        for (FirmaDigitaleFile file : transazione.firmaFile) {
            file.nomeFirmato = file.nome
            file.fileDaFirmare.binaryStream.reset()
            // deve essere un SerialBlob se no non funziona
            file.fileFirmato = new SerialBlob(file.fileDaFirmare.binaryStream.getBytes())
            file.save()
        }

        // chiamiamo direttamente il fineFirma
        fineFirmaCallback.fineFirma(transazione.id)
    }

    void terminaTransazione(){
        firmaDigitaleTransazioneService.terminaTransazione()
    }

    private void verificaAlmenoUnFileDaFirmare() {
        if (firmaDigitaleTransazioneService.transazioneCorrente.firmaFile.size() == 0) {
            throw new DocumentaleRuntimeException("Non è possibile proseguire con la firma perché non sono presenti file da firmare.")
        }
    }

    String getUrlFineFirma() {
        def url = Impostazioni.AG_SERVER_URL.valore
        def context = Impostazioni.AG_CONTEXT_NAME.valore
        def idTransazione = firmaDigitaleTransazioneService.transazioneCorrente.id
        def utente = springSecurityService.currentUser.nominativo
        def ente = springSecurityService.principal.amministrazione.codice
        String link = "$url$context/fineFirma/index?idTransazioneFirma=$idTransazione&utente=$utente&ente=$ente?idTransazioneFirma=$idTransazione"
        return link
    }

    String getUrlFirma() {
        return firmaDigitaleTransazioneService.configurazioneFirma.buildURL()
    }

    private String buildUrlFirma(boolean firmaAutografa, boolean firmaPades) {
        FirmaDigitaleUrlBuilder configurazione = firmaDigitaleTransazioneService.getConfigurazioneFirma().setFirmaHash(Impostazioni.FIRMA_HASH.abilitato).setTimeEnabled(Impostazioni.FIRMA_CON_TIMESTAMP.abilitato)
        if (firmaPades) {
            configurazione.setTipoFirma(FirmaDigitaleUrlBuilder.TipoFirma.PDF).setFirmaHash(false)
            if (Impostazioni.FIRMA_PDF_VISIBILE.valore == "SI") {
                FirmaDigitaleUrlBuilder.PosizioneFirmaPdf posizione = FirmaDigitaleUrlBuilder.PosizioneFirmaPdf.BASSO_DX
                if (Impostazioni.FIRMA_PDF_POSIZIONE.valore == "BASSO_DX") {
                    posizione = FirmaDigitaleUrlBuilder.PosizioneFirmaPdf.BASSO_DX
                } else if (Impostazioni.FIRMA_PDF_POSIZIONE.valore == "ALTO_DX") {
                    posizione = FirmaDigitaleUrlBuilder.PosizioneFirmaPdf.ALTO_DX
                } else if (Impostazioni.FIRMA_PDF_POSIZIONE.valore == "BASSO_SX") {
                    posizione = FirmaDigitaleUrlBuilder.PosizioneFirmaPdf.BASSO_SX
                } else if (Impostazioni.FIRMA_PDF_POSIZIONE.valore == "ALTO_SX") {
                    posizione = FirmaDigitaleUrlBuilder.PosizioneFirmaPdf.ALTO_SX
                }

                configurazione.setFirmaPDFVisibile(true).setFirmaPDFPosizione(posizione)

                if (Impostazioni.FIRMA_PDF_PAGINA.valore != "ULTIMA") {
                    configurazione.firmaPDFPagina = Impostazioni.FIRMA_PDF_PAGINA.valoreInt
                }

            }
        }
        configurazione.firmaAutografa = firmaAutografa
        return configurazione.buildURL()
    }
}
