package it.finmatica.gestionedocumenti.integrazioni.firma

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.firmadigitale.FirmaDigitaleFile
import it.finmatica.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.firmadigitale.FirmaDigitaleTransazioneService
import it.finmatica.gestionedocumenti.deleghe.DelegaService
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.DocumentoService
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.FileDocumentoFirmatario
import it.finmatica.gestionedocumenti.documenti.FileDocumentoService
import it.finmatica.gestionedocumenti.documenti.IGestoreFile
import it.finmatica.gestionedocumenti.documenti.StatoFirma
import it.finmatica.gestionedocumenti.documenti.beans.GestoreTransazioneFirma
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzioneService
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.so4.login.So4SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource

@Slf4j
@Service
@Transactional
class GestioneDocumentiFirmaService {

    // servizi
    @Autowired
    private FirmaDigitaleTransazioneService firmaDigitaleTransazioneService
    @Autowired
    private GestoreTransazioneFirma gestoreTransazioneFirma
    @Autowired
    private So4SpringSecurityService springSecurityService
    @Autowired
    private DocumentoService documentoService
    @Autowired
    private WkfAzioneService wkfAzioneService
    @Autowired
    private WkfIterService wkfIterService
    @Autowired
    private DelegaService delegaService
    @Autowired
    private IGestoreFile gestoreFile
    @Autowired
    private FileDocumentoService fileDocumentoService
    @Autowired
    private DataSource dataSource

    @Autowired(required = false)
    List<FirmaEventListener> eventListeners

    /*
     * GESTIONE DEI FILE
     */

    /**
     * Prepara il testo per la firma.
     *
     * Se il testo esiste ed è un PDF, non fa nulla e ritorna le informazioni sul file per quello che sono già.
     * Questo è necessario perché possono verificarsi errori nella prima fase di firma (ad es. l'utente apre la maschera di firma ma non firma).
     *
     * Come prima cosa, questo metodo verifica che l'editor del testo sia correttamente chiuso.
     *
     * Se il testo non esiste, viene creato dal modello "standard" usando i dati "live" della transazione oracle corrente.
     * Se il testo esiste ed è ancora modificabile, viene riaggiornato con i dati "live" della transazione oracle corrente. (per intenderci: fa la stampa unione con il n. della determina/delibera appena ottenuto nella transazione).
     * Se il testo è ancora modificabile (quindi è odt/doc/docx), ed è richiesta la trasformazione in pdf, viene convertito.
     *
     * @param documento il documento da cui parte la transazione di firma
     * @param fileDocumenti i documenti da aggiungere alla firma (NON devono essere per forza "figli" del parametro "documento" ma possono essere file di altri documenti)
     * @param trasformaInPdf se true, converte il testo in pdf. Default: false
     * @param generaTesto se true rigenera il testo del file solo se questo ha un modello associato (da' errore se non ha il modello associato). Default: false.
     * @param datiLive se true, utilizza la connessione corrente per eseguire la query reporter con cui rigenerare il testo. Default: false.
     */
    void preparaFilePerFirma(List<FileDocumento> fileDocumenti, boolean trasformaInPdf = false, boolean generaTesto = false, boolean datiLive = false, boolean firmaPades = Impostazioni.FIRMA_PADES.abilitato) {
        for (FileDocumento file : fileDocumenti) {
            preparaFilePerFirma(file, trasformaInPdf, generaTesto, datiLive, 0, firmaPades)
        }
    }

    /**
     * Prepara il file per la firma.
     *
     * Il file viene processato solo se:
     * - il documento è valido e con statoFirma DA_FIRMARE o IN_FIRMA
     * - il file allegato è valido
     *
     * Se il testo esiste ed è un PDF, non fa nulla e ritorna le informazioni sul file per quello che sono già.
     * Questo è necessario perché possono verificarsi errori nella prima fase di firma (ad es. l'utente apre la maschera di firma ma non firma).
     *
     * Come prima cosa, questo metodo verifica che l'editor del testo sia correttamente chiuso.
     *
     * Se il testo non esiste, viene creato dal modello "standard" usando i dati "live" della transazione oracle corrente.
     * Se il testo esiste ed è ancora modificabile, viene riaggiornato con i dati "live" della transazione oracle corrente. (per intenderci: fa la stampa unione con il n. della determina/delibera appena ottenuto nella transazione).
     * Se il testo è ancora modificabile (quindi è odt/doc/docx), ed è richiesta la trasformazione in pdf, viene convertito.
     *
     * @param documento il documento da cui parte la transazione di firma
     * @param fileDocumento i documenti da aggiungere alla firma (NON devono essere per forza "figli" del parametro "documento" ma possono essere file di altri documenti)
     * @param trasformaInPdf se true, converte il testo in pdf. Default: false
     * @param generaTesto se true rigenera il testo del file solo se questo ha un modello associato (da' errore se non ha il modello associato). Default: false.
     * @param datiLive se true, utilizza la connessione corrente per eseguire la query reporter con cui rigenerare il testo. Default: false.
     * @param firmaPades se true verifica i prerequisiti per la firma pades (pdf).
     */
    void preparaFilePerFirma(FileDocumento fileDocumento, boolean trasformaInPdf, boolean generaTesto, boolean datiLive, int numFirma, boolean firmaPades) {
        // verifico che l'editor del testo sia correttamente chiuso e che il testo non sia lockato da nessuno
        if (fileDocumento.codice == FileDocumento.CODICE_FILE_PRINCIPALE) {
            if (documentoService.editorAperto) {
                throw new GestioneDocumentiRuntimeException("Non è possibile proseguire con la firma del testo: il documento è ancora aperto nell'editor di testo. Chiudere l'editor di testo.");
            }
            if (documentoService.uploadEUnlockTesto(fileDocumento.documento, fileDocumento)) {
                throw new GestioneDocumentiRuntimeException("Non è possibile proseguire con la firma del testo: il documento è ancora aperto da un altro utente.");
            }
        }

        // non aggiungo file nulli
        if (fileDocumento == null) {
            throw new GestioneDocumentiRuntimeException("Non è possibile aggiungere file nulli alla firma.")
        }

        // se devo rigenerare il testo, bisogna che il file abbia il modello di testo
        if (generaTesto && fileDocumento.modelloTesto == null) {
            throw new GestioneDocumentiRuntimeException("Non è possibile generare il testo del file '${fileDocumento.nome}' perché non ha un modello di testo associato.")
        }

        // se devo rigenerare il testo, bisogna che questo sia ancora "modificabile"
        if (generaTesto && !fileDocumento.modificabile) {
            throw new GestioneDocumentiRuntimeException("Non è possibile generare il testo del file '${fileDocumento.nome}' perché non è più modificabile.")
        }

        // non aggiungo un file non valido
        if (!fileDocumento.valido) {
            return
        }

        Documento documento = fileDocumento.documento

        // non aggiungo file se il documento non è valido o non è in stato da firmare o in firma.
        if (!(documento.valido && (documento.statoFirma == StatoFirma.DA_FIRMARE || documento.statoFirma == StatoFirma.IN_FIRMA))) {
            return
        }

        // se richiesto, rigenero il testo
        if (generaTesto) {
            // genero/aggiorno il testo.
            documentoService.generaTestoFile(documento, fileDocumento, Impostazioni.FORMATO_DEFAULT.valore, datiLive)
        }

        // se richiesto e se non lo è già, trasformo in pdf:
        // serve perché alcuni utilizzano la "firma autografa" che non trasforma il file in pdf
        // in un primo momento ma in un secondo. Ad es. Trezzano: prima "firma" il dirigente, poi la Ragioneria.
        // alla firma il testo deve rimanere modificabile (perché ancora non tutti i tag sono riempiti)
        String nomeFile = fileDocumento.nome

        InputStream inputStreamTesto = gestoreFile.getFile(documento, fileDocumento, true)

        if (trasformaInPdf && fileDocumento.modificabile) {
            inputStreamTesto = documentoService.convertiStreamInPdf(inputStreamTesto, fileDocumento.nome, fileDocumento.documento)
            nomeFile = fileDocumento.nomePdf
        }

        if (firmaPades) {
            nomeFile = calcolaNomeFilePades(nomeFile)
        }

        // Issue #32968
        // Se sto facendo una firma PADES e sto firmando un file non pdf, non lo aggiungo alla firma:
        if (firmaPades && !nomeFile.toLowerCase().endsWith('.pdf')) {
            return
        }

        // salvo il file nello storico per mantenere la copia prima della firma
        documentoService.salvaFileStorico(documento, fileDocumento, FileDocumento.CODICE_FILE_ORIGINALE)

        FileDocumento fileOriginale = fileDocumentoService.getFileOriginale(documento.id, fileDocumento.id)

        gestoreFile.addFile(documento, fileOriginale, gestoreFile.getFile(documento, fileDocumento, true), true)

        fileOriginale?.idFileEsterno = null

        // imposto il documento come "in_firma"
        documento.statoFirma = StatoFirma.IN_FIRMA
        documento.save()


        // aggiungo il file alla lista di quelli da firmare
        gestoreTransazioneFirma.addFileDaFirmare(creaIdRiferimento(fileDocumento), nomeFile, inputStreamTesto, numFirma)
        //issue 71114
    }

    void preparaFilePerFirma(FileDocumento fileDocumento, boolean trasformaInPdf = false, boolean generaTesto = false, boolean datiLive = false, int numFirma = 0) {
        preparaFilePerFirma(fileDocumento, trasformaInPdf, generaTesto, datiLive, numFirma, Impostazioni.FIRMA_PADES.abilitato)
    }

    void processaFirmatario(Documento documento, Ad4Utente firmatario) {
        CodaFirma coda = CodaFirma.cheStaFirmando(documento).get()
        if (coda == null) {
            // non ho una riga sulla coda per questo documento, significa che non è il documento da cui la firma è "partita"
            // oppure significa che il firmatario per questo documento è già stato processato.
            // in entrambi i casi, non devo fare niente.
            return
        }

        // se invece trovo un firmatario, verifico che vada bene:
        if (coda.firmatario.id != springSecurityService.principal.id && !delegaService.hasDelega(springSecurityService.currentUser, coda.firmatario)) {
            throw new GestioneDocumentiRuntimeException("Non è possibile proseguire la firma: il firmatario predisposto per il documento con id ${documento.id} è ${coda.firmatario?.nominativo}!")
        }

        // se invece arrivo qui, allora ho trovato il firmatario giusto, così che dopo lo possa ritrovare più facilmente.
        coda.firmatarioEffettivo = springSecurityService.currentUser
        coda.firmato = true

        coda.save()
    }

    void processaFileFirmato(FileDocumento fileDocumento, FirmaDigitaleFile fileFirmato,boolean saltaVerifica=false) {
        // se sto processando un allegato, allora mi assicuro di cambiare anche lo statoFirma sul suo documento padre.
        // questo viene fatto perché ci possono essere casi in cui si vuole firmare solo gli allegati ma non il testo del documento principale.
        fileDocumento.documento.statoFirma = StatoFirma.FIRMATO_DA_SBLOCCARE

        // rimuovo i troppi .p7m se ci sono.
        fileDocumento.nome = fileFirmato.nomeFirmato.replaceAll(/(\.[pP]7[mM])+$/, ".p7m")
        String estensione = fileDocumento.nome.substring(fileDocumento.nome.lastIndexOf("."))
        fileDocumento.contentType = estensione.toLowerCase().equals("p7m") ? "application/pkcs7-mime" : GestioneTestiService.getContentType(estensione)

        // Questo è (e DEVE) essere l'unico punto in cui viene impostato a "TRUE" il flag "firmato"
        fileDocumento.firmato = true
        gestoreFile.addFile(fileDocumento.documento, fileDocumento, fileFirmato.fileFirmato.binaryStream, true)

        // salvo i dati della firma appena fatta
        FileDocumentoFirmatario fileDocumentoFirmatario = new FileDocumentoFirmatario(dataFirma: fileFirmato.transazione.dataFirma, utenteAd4: Ad4Utente.get(fileFirmato.transazione.utente))
        if (Impostazioni.ALLEGATO_VERIFICA_FIRMA.abilitato && !saltaVerifica) {
            fileDocumentoService.aggiornaVerificaFirma(fileDocumento)
            fileDocumentoFirmatario.stato = FileDocumentoFirmatario.CALCOLATO
            fileDocumentoFirmatario.dataFirma = fileFirmato.transazione.dataFirma
            fileDocumentoFirmatario.nominativo = Ad4Utente.get(fileFirmato.transazione.utente).getNominativoSoggetto()
        }
        fileDocumento.addToFirmatari(fileDocumentoFirmatario)
        fileDocumento.save()

        // salvo il documento
        fileDocumento.documento.save()

        for (FirmaEventListener eventListener : eventListeners) {
            eventListener.dopoFirma(new FirmaEvent(fileDocumento,saltaVerifica))
        }
    }

    /*
     * GESTIONE DELLA CODA DEI FIRMATARI
     */

    /**
     * Aggiunge l'utente alla coda dei firmatari per il documento richiesto.
     * Siccome la firma può essere interrotta e poi ripresa, questa funzione potrebbe essere invocata più volte.
     * Perciò questa funzione non crea un nuovo firmatario se esiste già un record con firmato = 'N' per il documento e utente richiesti.
     *
     * @param documento
     * @param utenteAd4
     */
    void aggiungiFirmatarioAllaCoda(Documento documento, Ad4Utente utenteAd4) {
        if (utenteAd4 == null) {
            throw new GestioneDocumentiRuntimeException("Attenzione! Manca il Firmatario per il documento ${documento}. Non è possibile continuare.")
        }

        log.debug("aggiungiFirmatarioAllaCoda: ${documento.id} ${utenteAd4.id}")
        def firmatario = CodaFirma.inCodaPerFirmare(documento, utenteAd4).get()

        if (firmatario != null) {
            log.warn("aggiungiFirmatarioAllaCoda: esiste già un firmatario (${firmatario.id}) per l'utente ${utenteAd4.id} e il documento con id ${documento.id}. Non lo riaggiungo e proseguo.")
            return
        }

        // conto i firmatari presenti in modo da settare correttamente la sequenza:
        long nFirmatari = CodaFirma.ultimoInCoda(documento).get()?.sequenza ?: 0
        log.debug("aggiungiFirmatarioAllaCoda: ho contato ${nFirmatari} per il documento con id: ${documento.id}")

        // aggiungo il firmatario alla coda
        new CodaFirma(documento: documento, firmatario: utenteAd4, firmatarioEffettivo: springSecurityService.currentUser, firmato: false, dataFirma: null, sequenza: (nFirmatari + 1)).save()
    }

    /**
     * Predispone il firmatario richiesto per la firma.
     * Se il parametro utenteFirmatario è null, viene predisposto il primo disponibile in coda.
     *
     */
    void preparaFirmatarioInCoda(Documento documento, Ad4Utente utenteFirmatario = null) {
        CodaFirma firmatarioInCoda = CodaFirma.cheStaFirmando(documento, utenteFirmatario).get()

        // se trovo un firmatario che sta già firmando, verifico che quel firmatario sia lo stesso che mi è stato richiesto:
        if (firmatarioInCoda != null) {
            if (utenteFirmatario != null && firmatarioInCoda.firmatario.id != utenteFirmatario.id) {
                throw new GestioneDocumentiRuntimeException("L'utente ${utenteFirmatario.nominativo} non può proseguire la firma perché è già stata iniziata dall'utente: ${firmatarioInCoda.firmatario.nominativo}")
            }

            // inizializzo la firma
            gestoreTransazioneFirma.inizializzaTransazioneFirma(firmatarioInCoda.firmatario)

            log.debug("Firmatario che sta firmando: ${firmatarioInCoda.firmatario.nominativo}")
            return
        }

        // se non trovo un firmatario per questo documento, dovrò selezionare il primo:
        firmatarioInCoda = CodaFirma.prossimoInCoda(documento).get()
        log.debug("Firmatario che deve firmare: ${firmatarioInCoda}")

        // se ancora non trovo alcun firmatario, vuol dire che nessun firmatario è stato aggiunto alla coda, quindi do' errore.
        if (firmatarioInCoda == null) {
            throw new GestioneDocumentiRuntimeException("Non è possibile proseguire la firma: non c'è nessun firmatario predisposto sul documento ${documento.id}!")
        }

        // imposto il documento come in_firma (in caso non aggiungessi file direttamente collegati ad esso)
        documento.statoFirma = StatoFirma.IN_FIRMA
        documento.save()

        // se arrivo qui, allora ho trovato il firmatario giusto, imposto data di firma e transazione, così che dopo lo possa ritrovare dalla query di firma nei modelli testo
        firmatarioInCoda.transazioneFirma = gestoreTransazioneFirma.inizializzaTransazioneFirma(firmatarioInCoda.firmatario)
        firmatarioInCoda.dataFirma = new Date()
        firmatarioInCoda.save()
    }

    void eliminaUltimoFirmatarioInCoda(Documento documento) {
        CodaFirma.prossimoInCoda(documento).get()?.delete()
    }

    void eliminaFirmatarioInCoda(Documento documento, Ad4Utente utente) {
        CodaFirma coda = CodaFirma.inCodaPerFirmare(documento, utente).get()
        if (coda == null) {
            return
        }

        coda.delete()

        riordinaCodaFirma(documento)
    }

    void eliminaFirmatariInCoda(Documento documento) {
        CodaFirma.inCodaPerFirmare(documento).list()*.delete()
    }

    void riordinaCodaFirma(Documento documento) {
        def coda = CodaFirma.inCodaPerFirmare(documento).list()
        int sequenza = 0
        for (CodaFirma firmatario : coda) {
            firmatario.sequenza = (++sequenza)
            firmatario.save()
        }
    }

    /**
     * Calcola l'url a cui aprire la pagina di firma.
     *
     * @return l'url della pagina di firma
     */
    String getUrlPopupFirma() {
        return gestoreTransazioneFirma.urlFirma
    }

    /**
     * Calcola l'url a cui aprire la pagina di firma.
     *
     * @return l'url della pagina di firma
     */
    String getUrlPopupFirmaAutografa() {
        return gestoreTransazioneFirma.urlFineFirma
    }

    FirmaDigitaleTransazione getTransazioneCorrente(){
        return gestoreTransazioneFirma.transazioneCorrente
    }

    void finalizzaTransazioneFirma(boolean firmaAutografa=false, boolean firmaPades=Impostazioni.FIRMA_PADES.abilitato) {
        gestoreTransazioneFirma.finalizzaTransazioneFirma(firmaAutografa, firmaPades)
    }

    void finalizzaTransazioneFirmaAutografa() {
        gestoreTransazioneFirma.finalizzaTransazioneFirmaAutografa()
    }

    void terminaTransazione(){
        gestoreTransazioneFirma.terminaTransazione()
    }

    List<Long> getDocumentiDaSbloccare(long idTransazioneFirma) {
        return getDocumentiDaSbloccare((FirmaDigitaleTransazione) FirmaDigitaleTransazione.get(idTransazioneFirma)).id
    }

    List<Documento> getDocumentiDaSbloccare(FirmaDigitaleTransazione transazione) {
        // trovo tutti i documenti "da cui la firma è partita":
        List<Documento> documentiPrincipali = CodaFirma.documentiPerTransazione(transazione).list()

        // trovo tutti i documenti "padri" dei file firmati:
        List<Documento> documentiPadri = FirmaDigitaleFile.createCriteria().list {
            projections {
                groupProperty("idRiferimentoFile")
            }
            eq("transazione", transazione)
            isNotNull("nomeFirmato")
        }.collect { String idRiferimento ->
            parseIdRiferimento(idRiferimento).documento
        }

        return documentiPadri + documentiPrincipali
    }

    void fineFirmaSalvaFileFirmati(long idTransazione,boolean saltaVerifica=false) {
        fineFirmaSalvaFileFirmati(FirmaDigitaleTransazione.get(idTransazione),saltaVerifica)
    }

    void fineFirmaSalvaFileFirmati(FirmaDigitaleTransazione transazione,boolean saltaVerifica=false) {
        // recupero i file che sono stati correttamente firmati (o tutti, o nessuno)
        Collection<FirmaDigitaleFile> listaFileFirmati = transazione.firmaFile

        // se la lista è vuota, significa che c'è stato un errore in firma
        if (listaFileFirmati == null || listaFileFirmati.size() == 0) {
            throw new GestioneDocumentiRuntimeException("Attenzione: la transazione non contiene alcun file firmato!")
        }

        // processo i file firmati:
        for (FirmaDigitaleFile fileFirmato : listaFileFirmati) {

            // scompongo il nome del file e capisco cosa devo fare
            FileDocumento fileDocumento = parseIdRiferimento(fileFirmato.idRiferimentoFile)
            processaFileFirmato(fileDocumento, fileFirmato,saltaVerifica)
        }

        // trovo tutti i documenti "da cui la firma è partita" e che hanno lo stato ancora IN_FIRMA (può capitare se la firma parte da un certo documento ma vengono messi in firma solo file di documenti collegati.
        List<Documento> documentiPrincipali = CodaFirma.documentiPerTransazione(transazione, StatoFirma.IN_FIRMA).list()

        // imposto ciascuno di questi documenti come "DA SBLOCCARE"
        for (Documento doc : documentiPrincipali) {
            doc.statoFirma = StatoFirma.FIRMATO_DA_SBLOCCARE
            doc.save()
        }
    }

    void fineFirmaSbloccaFlusso(long idDocumento) {
        fineFirmaSbloccaFlusso(Documento.get(idDocumento), springSecurityService.currentUser)
    }

    /**
     * Sblocca un documento solo se in stato "FIRMATO_DA_SBLOCCARE"
     * Processa il firmatario impostandolo
     *
     * @param documento
     * @param utenteFirmatario
     * @return
     */
    void fineFirmaSbloccaFlusso(Documento documento, Ad4Utente utenteFirmatario) {
        if (documento.statoFirma != StatoFirma.FIRMATO_DA_SBLOCCARE) {
            return
        }

        processaFirmatario(documento, utenteFirmatario)

        documento.statoFirma = StatoFirma.FIRMATO
        documento.save()

        // se ho un iter da sbloccare, lo sblocco.
        if (documento.iter?.stepCorrente?.cfgStep != null) {
            wkfIterService.sbloccaDocumento(documento)
        }
    }


    /*
     * Utilità varie
     */
    /**
     * Elimina le transazioni di firma più vecchie di un giorno.
     * Funzione richiamata dal job notturno.
     */
    void eliminaTransazioniVecchie() {

        // elimino le transazioni di firma più vecchie di un giorno
        List<GroovyRowResult> transazioni = new Sql(dataSource).rows('''
                SELECT id
                FROM Firma_Digitale_Transazione
                WHERE date_created <= TRUNC (SYSDATE -1)
                AND id IN (SELECT transazione_id
                FROM firma_digitale_file
                WHERE NVL (DBMS_LOB.GETLENGTH (file_firmato), 0) > 0
                or NVL (DBMS_LOB.GETLENGTH (file_da_firmare), 0) > 0)
        ''')

        log.info("Elimino ogni transazione di firma troppo vecchia.")
        for (GroovyRowResult t : transazioni) {
            eliminaTransazione(((BigDecimal) t.get("ID")).longValue())
        }
    }

    /**
     * Svuota i blob della transazione di firma.
     *
     * @param transazione
     */
    void eliminaTransazione(long idTransazione) {
        eliminaTransazione(FirmaDigitaleTransazione.get(idTransazione))
    }

    void eliminaTransazione(FirmaDigitaleTransazione transazione) {
        for (FirmaDigitaleFile file : transazione.firmaFile) {
            file.fileDaFirmare = null
            file.fileFirmato = null
            file.save()
        }
//        List<CodaFirma> firmatari = CodaFirma.createCriteria().list {
//            eq("transazioneFirma.id", transazione.id)
//        }
//        for(CodaFirma f: firmatari){
//            f.transazioneFirma = null
//            f.save()
//            transazione.save()
//        }
        log.info("Eliminazione dei file della transazione :" + transazione.id)
        transazione.save()
    }

    /**
     * Parsa l'idRiferimento costruito da creaIdRiferimento
     *
     * @param idRiferimento l'id riferimento da parsare
     * @return la mappa con i valori trovati nell'idRiferimento, la mappa è della forma: [tipoOggetto:'${documento.TIPO_OGGETTO}', id:${documento.id}, idFile: ${idFile}, sbloccaFlusso:${documento instanceof IDocumentoIterabile}]
     */
    static FileDocumento parseIdRiferimento(String idRiferimento) {
        return FileDocumento.get(Long.parseLong(idRiferimento))
    }

    /**
     * Costruisce l'idRiferimento da passare alla firma per un certo file
     *
     * @param documento la domain a cui appartiene il file da firmare. Questa domain DEVE avere la proprietà statica TIPO_OGGETTO e la proprietà "id".
     * @param propertyName il nome della proprietà della domain in cui trovare il file da firmare
     * @param file l'istanza del FileDocumento da firmare
     * @return una stringa della forma: [tipoOggetto:'${documento.TIPO_OGGETTO}', id:${documento.id}, idFile: ${idFile}, sbloccaFlusso:${documento instanceof IDocumentoIterabile}]
     */
    static String creaIdRiferimento(FileDocumento file) {
        return Long.toString(file.id)
    }

    /*
     * GESTIONE MULTI-FIRMA
     */
    String multiFirma(Collection<Documento> documentiDaFirmare, boolean firmaAutografa=false, boolean firmaPades=Impostazioni.FIRMA_PADES.abilitato) {
        String finalizzaTransazioneFirma = null

        // ciclo su ogni documento e per ciascuno prendo il pulsante che contiene l'azione di firma:
        for (Documento d : documentiDaFirmare) {

            // FIXME: in realtà questa è una approssimazione: da un cliente o è abilitata la firma digitale o non lo è.
            // quindi l'azione che verrà ritornata dirà se bisognerà eseguire la finalizzazione della firma digitale oppure autografa.
            finalizzaTransazioneFirma = firmaDocumento(d)
        }

        return finalizzaFirma(finalizzaTransazioneFirma, firmaAutografa, firmaPades)
    }

    private String finalizzaFirma(String azioneFirma, boolean firmaAutografa=false, boolean firmaPades=Impostazioni.FIRMA_PADES.abilitato) {
        switch (azioneFirma) {
            case "finalizzaTransazioneFirma":
                return gestoreTransazioneFirma.finalizzaTransazioneFirma(firmaAutografa, firmaPades)

            case "finalizzaTransazioneFirmaAutografa":
                return gestoreTransazioneFirma.finalizzaTransazioneFirmaAutografa()
        }

        throw new GestioneDocumentiRuntimeException("Errore di configurazione: non ho trovato l'azione di finalizzazione della firma con nome: '${azioneFirma}'. Verificare la configurazione del flusso.")
    }

    /**
     * Esegue le azioni del pulsante di firma.
     * FIXME: questa è una approssimazione fintanto che gestione-iter non gestirà i pulsanti in multi-selezione.
     *
     * @param documento
     * @return ritorna l'azione di finalizzazione della firma (autografa o digitale) supponendo che sia l'ultima azione del pulsante.
     */
    private String firmaDocumento(Documento documento) {
        // ottengo l'elenco delle azioni AUTOMATICHE che sono presenti nel pulsante di firma.
        List<WkfAzione> azioni = getAzioniPulsanteFirma(documento.iter.stepCorrente.id)

        // eseguo le azioni del pulsante:
        for (WkfAzione azione : azioni) {
            // assumo che l'ultima azione del pulsante sia quella di finalizzazione della firma:
            if (azione.nomeMetodo.startsWith('finalizzaTransazioneFirma')) {
                return azione.nomeMetodo
            } else if (azione.automatica) {
                wkfAzioneService.eseguiAzioneAutomatica(azione, documento)
            }
        }
        throw new GestioneDocumentiRuntimeException("Errore di configurazione: non ho trovato l'azione di finalizzazione della firma. Verificare la configurazione del flusso.")
    }

    /**
     * Ritorna tutte le azioni di tipo automatico/automatico_calcolo_attore
     */
    private List<WkfAzione> getAzioniPulsanteFirma(long idStep) {
        def azioniFinalizzazioneFirma
        azioniFinalizzazioneFirma = ["finalizzaTransazioneFirma", "finalizzaTransazioneFirmaAutografa"]

        return WkfStep.executeQuery("""select azione
									   from WkfStep s join s.cfgStep as cfgStep
											join cfgStep.cfgPulsanti as cfgPulsanti
											join cfgPulsanti.pulsante as pulsante
											join pulsante.azioni as azione
								 	  where exists (select a.id
													  from WkfPulsante p
													  join p.azioni as a
													 where p = pulsante
													   and a.nomeMetodo in :nomeMetodo
													   and a.nomeBean 	= :nomeBean)
										and azione.tipo in :tipiAzione
									    and s.id 		= :idStep
									order by index(azione) asc
							""", [tipiAzione: [Action.TipoAzione.AUTOMATICA, Action.TipoAzione.AUTOMATICA_CALCOLO_ATTORE], idStep: idStep, nomeMetodo: azioniFinalizzazioneFirma, nomeBean: 'firmaAction'])
    }

    /**
     * Sblocca i documenti solo se in stato "FIRMATO_DA_SBLOCCARE"
     * Processa il firmatario impostandolo
     *
     * @param documento
     * @param utenteFirmatario
     * @return
     */
    void sbloccaDocumentiFirmati(Collection<Documento> documentiDaSbloccare, Ad4Utente utenteFirmatario) {
        for (Documento d : documentiDaSbloccare) {
            fineFirmaSbloccaFlusso(d, utenteFirmatario)
        }
    }

    String calcolaNomeFilePades(String filename) {
        //se esiste un SUFFISSO per la firma pades e il nomeFile non contiene questo suffisso (per evitare che lo scriva due volte)
        try {
            if ( Impostazioni.SUFFISSO_FIRMA_PADES.valore != null
                    && Impostazioni.SUFFISSO_FIRMA_PADES.valore.length() > 0
                    && filename.indexOf(Impostazioni.SUFFISSO_FIRMA_PADES.valore) < 0
            ) {
                String nome = filename.substring(0, filename.lastIndexOf("."))
                String estensione = filename.substring(filename.lastIndexOf("."))
                // concateno nomeFile senza estensione con il suffisso e l'estensione
                // filename = NOMEFILE + SUFF + ESTENSIONE
                filename = nome + Impostazioni.SUFFISSO_FIRMA_PADES.valore + estensione
            }
        } catch (Exception ex) {
            log.error("Errore nella creazione del nomeFile per firma Pades: " + ex.message)
        }
        return filename
    }
}
