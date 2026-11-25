package it.finmatica.gestionedocumenti.documenti

import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfGState
import com.lowagie.text.pdf.PdfReader
import com.lowagie.text.pdf.PdfStamper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import it.finmatica.firmadigitale.utils.VerificatoreFirma
import it.finmatica.gestionedocumenti.commons.Ad4Service
import it.finmatica.gestionedocumenti.commons.GestioneTestiLog
import it.finmatica.gestionedocumenti.commons.GestioneTestiLogService
import it.finmatica.gestionedocumenti.documenti.beans.GestoreTransazioneFirma
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.exception.ValidaDimensioneAllegatiRuntimeException
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionedocumenti.integrazioni.documentale.IntegrazioneDocumentaleService
import it.finmatica.gestionedocumenti.integrazioni.gdm.IntegrazioneGdmService
import it.finmatica.gestionetesti.EsitoRichiestaLock
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.segreteria.common.FirmaUtilities
import it.finmatica.smartdoc.api.DocumentaleService
import it.finmatica.so4.login.So4SpringSecurityService
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.hibernate.jpa.AvailableSettings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.zkoss.util.media.Media
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Filedownload

import javax.persistence.EntityManager
import javax.persistence.LockModeType
import javax.sql.DataSource

@Slf4j
@Transactional
@Service
class DocumentoService {

    @Autowired
    GestoreTransazioneFirma gestoreTransazioneFirma
    @Autowired
    IntegrazioneGdmService integrazioneGdmService
    @Autowired
    IntegrazioneDocumentaleService integrazioneDocumentaleService
    @Autowired
    So4SpringSecurityService springSecurityService
    @Autowired
    GestioneTestiService gestioneTestiService
    @Autowired
    DocumentaleService documentaleService
    @Autowired
    IGestoreFile gestoreFile
    @Autowired
    DataSource dataSource
    @Autowired
    Ad4Service ad4Service
    @Autowired
    FileDocumentoService fileDocumentoService
    @Autowired
    EntityManager entityManager
    @Autowired
    DocumentoService documentoService
    @Autowired
    GestioneTestiLogService gestioneTestiLogService
    @Autowired
    TransactionTemplate transactionTemplate

    int getSequenzaNuovoAllegato(DocumentoDTO documentoPrincipaleDTO) {
        int sequenza = 1
        if (documentoPrincipaleDTO.domainObject.allegati.size() > 0) {
            sequenza = (documentoPrincipaleDTO.domainObject.allegati.max { it.sequenza }.sequenza) + 1
        }

        return sequenza
    }

    AllegatoDTO importaAllegatiEsterni(DocumentoDTO documentoPrincipaleDTO, AllegatoDTO allegatoDTO, List<IFileDocumento> allegatiEsterni, boolean forceImport = true) {
        Allegato allegato = (allegatoDTO == null) ? null : salvaAllegato(allegatoDTO, documentoPrincipaleDTO)

        boolean creaAllegatoNuovo
        creaAllegatoNuovo = false
        if (allegato == null) {
            creaAllegatoNuovo = true
        }

        // Caricamento dei file selezionati
        for (IFileDocumento allegatoEsterno : allegatiEsterni) {
            if (creaAllegatoNuovo) {
                //Se non l'ho passato, lo creo io
                allegato = new Allegato()
                allegato.descrizione = allegatoEsterno.nome
                allegato.tipoAllegato = TipoAllegato.findByCodice(TipoAllegato.CODICE_TIPO_ALLEGATO)
                allegato.statoFirma = StatoFirma.DA_NON_FIRMARE
                allegato.sequenza = getSequenzaNuovoAllegato(documentoPrincipaleDTO)
                allegato.save()
                documentoPrincipaleDTO.domainObject.addDocumentoAllegato(allegato)
                documentoPrincipaleDTO.domainObject.save()
            }

            FileDocumento fileDocumento = new FileDocumento()
            fileDocumento.contentType = allegatoEsterno.contentType
            fileDocumento.nome = getNomeFileUnivoco(allegato?.id, allegatoEsterno.nome)
            fileDocumento.dimensione = -1
            fileDocumento.modificabile = false

            allegato.addToFileDocumenti(fileDocumento)
            allegato.save()

            gestoreFile.addFile(allegato, fileDocumento, gestoreFile.getFile(allegatoEsterno.getDocumento(), allegatoEsterno))

            if (Impostazioni.ALLEGATO_VERIFICA_FIRMA.abilitato) {
                fileDocumentoService.aggiornaVerificaFirma(fileDocumento)
                allegato.addToFileDocumenti(fileDocumento)
            }
        }

        // validazione delle dimensioni del file
        validaDimensioneAllegati(documentoPrincipaleDTO?.domainObject, false, forceImport)

        return allegato?.toDTO()
    }

    FileDocumento uploadFile(Documento documento, Media media) {
        return uploadFile(documento, media.name, media.contentType, media.binary ? media.streamData : new ByteArrayInputStream(media.stringData.bytes))
    }

    FileDocumento uploadFile(Documento documento, String nomeFile, String contentType, InputStream inputStream) {
        FileDocumento fileDocumento = new FileDocumento()
        fileDocumento.nome = getNomeFileUnivoco(documento.id, nomeFile)
        fileDocumento.contentType = contentType
        fileDocumento.modificabile = false

        gestoreFile.addFile(documento, fileDocumento, inputStream)

        if (Impostazioni.ALLEGATO_VERIFICA_FIRMA.abilitato) {
            fileDocumentoService.aggiornaVerificaFirma(fileDocumento)
            documento.addToFileDocumenti(fileDocumento)
        }

        return fileDocumento
    }

    String getNomeFileUnivoco(long idDocumento, String nomeFile) {
        // conto i file che hanno il nome richiesto:
        String nome = nomeFile
        int numero = Documento.numeroFilePerNome(idDocumento, nome).get()

        // se esiste già un file con questo nome, rinomino e ne creo un altro:
        int counter = 1
        while (numero > 0) {
            nome = nomeFile.replaceAll(/(\..+)$/, "(${counter})\$1")
            counter++
            numero = Documento.numeroFilePerNome(idDocumento, nome).get()
        }

        return nome
    }

    void eliminaFileDocumento(Documento documento, FileDocumento fileDocumento) {
        FileDocumento fileOriginale = FileDocumento.findByFileOriginale(fileDocumento)
        if (fileOriginale && fileOriginale.idFileEsterno != null) {
            fileOriginale.valido = false
            cancellaDocumentale(fileOriginale, documento)
        }

        fileDocumento.valido = false
        cancellaDocumentale(fileDocumento, documento)
        documento.save()
    }

    private cancellaDocumentale(FileDocumento fileDocumento, Documento documento) {
        if(fileDocumento?.idFileEsterno != null){
            int size = fileDocumentoService.numeroDocumentiDocumentaleAssociati(fileDocumento.idFileEsterno)
            if (size == 1) {
                gestoreFile.removeFile(documento, fileDocumento)
            } else if (size > 1) {
                gestoreFile.removeFile(documento, fileDocumento, true)
            }
        }
    }

    void eliminaFileDocumento(Documento documento, long idFileDocumento) {
        FileDocumento fileDocumento = FileDocumento.get(idFileDocumento)

        // #29558 Lettera: eliminazione file allegati firmati da errore.
        // fileDocumento potrebbe essere già stato cancellato in quanto collegato ad
        // un file cancellato in precedenza (catena tra firmato e file originale)
        if (fileDocumento) {
            eliminaFileDocumento(documento, fileDocumento)
        }
    }

    /*
     *  Gestione Documenti Allegati
     */

    Allegato salvaAllegato(AllegatoDTO allegatoDTO, DocumentoDTO documentoPrincipaleDto = null, Media media = null) {
        Allegato allegato = allegatoDTO.getDomainObject() ?: new Allegato()

        allegato.tipoAllegato = allegatoDTO.tipoAllegato?.domainObject

        allegato.commento = allegatoDTO.commento
        allegato.descrizione = allegatoDTO.descrizione
        allegato.statoFirma = allegatoDTO.statoFirma

        allegato.quantita = allegatoDTO.quantita
        allegato.numPagine = allegatoDTO.numPagine
        allegato.sequenza = allegatoDTO.sequenza

        allegato.stampaUnica = allegatoDTO.stampaUnica
        allegato.riservato = allegatoDTO.riservato

        allegato.ubicazione = allegatoDTO.ubicazione
        allegato.origine = allegatoDTO.origine

        allegato = allegato.save()

        if (documentoPrincipaleDto != null && allegato.documentoPrincipale == null) {
            //Documento documento = documentoPrincipaleDto.domainObject
            //Ricarico la domain per ottenere un optimistic lock
            Documento documento = getDocumentoForOptimisticLock(documentoPrincipaleDto?.id)
            if (documento != null) {
                documento.addDocumentoAllegato(allegato)
                documento.save()
            }
        }

        // se ho il file, lo salvo e questo salverà anche il documento su gdm
        if (media != null) {
            uploadFile(allegato, media)
        } else {
            // se non ho il file, devo salvare comunque su gdm
            it.finmatica.smartdoc.api.struct.Documento documentoSmart = integrazioneDocumentaleService.salva(allegato)
            if (documentoSmart.id != null) {
                allegato.idDocumentoEsterno = Long.parseLong(documentoSmart.id)
            }
        }

        return allegato
    }

    void eliminaAllegato(Allegato allegato) {
        DocumentoCollegato collegamento = allegato.getCollegamentoDocumentoPrincipale()
        //Ottengo un lock ottimistico sul documento principale per far avanzare la version a seguito dell'update(remove) sull'entità figlia
        // Documento documento = documentoService.getDocumentoForOptimisticLock(collegamento?.documento?.id)
        if (collegamento != null) {
            collegamento.valido = false
            collegamento.save()
        }

        // elimino tutti i suoi fileAllegati
        List<Long> toDelete = allegato.fileDocumentiValidi.id
        for (idFileDocumento in toDelete) {
            eliminaFileDocumento(allegato, idFileDocumento)
        }

        try {

            it.finmatica.smartdoc.api.struct.Documento documento = new it.finmatica.smartdoc.api.struct.Documento(id: allegato.idDocumentoEsterno)
            documento.addChiaveExtra('ESCLUDI_CONTROLLO_COMPETENZE', 'Y')
            documentaleService.eliminaDocumento(documento)
        } catch (Exception t) {
            throw new GestioneDocumentiRuntimeException(t)
        }
    }

    /**
     * Calcola le dimensioni degli allegati di un documento
     *
     * @param documento
     */
    long calcolaDimensioneAllegati(Documento documento, boolean duranteFirma = false) {
        long dimensioneAllegati = 0
        List<Allegato> allegati = documento.getAllegati()
        if (!duranteFirma) {
            dimensioneAllegati = documento.getFilePrincipale()?.dimensione ?: 0
        } else {
            if (documento.getFilePrincipale()) {
                dimensioneAllegati = dimensioneFileDaFirmare(gestoreTransazioneFirma.getTransazioneCorrente().getId(), documento.getFilePrincipale().id)
            }
        }

        for (Allegato a : allegati) {
            if (a.tipoAllegato?.codice != TipoAllegato.CODICE_TIPO_STAMPA_UNICA) {
                for (FileDocumento f : a.fileDocumentiValidi) {
                    if (f.dimensione > 0) {
                        dimensioneAllegati = dimensioneAllegati + f.dimensione
                    }
                }
            }
        }

        return dimensioneAllegati
    }

    /**
     * Valida dimensioni degli allegati
     *
     * @param documento
     */
    @Transactional(readOnly = true)
    void validaDimensioneAllegati(Documento documento, boolean duranteFirma = false, boolean forzabile = true) {
        long dimensioneMaxAllegati = 0
        if (Impostazioni.MAXDIM_ATTACH.valore != null) {
            //Nel caso di eccezione (ad esempio se è stato inserito un valore troppo grande come parametro) setto la massima dimensione al valore massimo raprresentabile in Long
            try{
                dimensioneMaxAllegati = Long.parseLong(Impostazioni.MAXDIM_ATTACH.valore)
            } catch (NumberFormatException e) {
                dimensioneMaxAllegati = Long.MAX_VALUE
            }

        }
        if (dimensioneMaxAllegati <= 0) {
            return
        }

        int dimensioneAllegati = calcolaDimensioneAllegati(documento, duranteFirma)

        if (dimensioneAllegati > dimensioneMaxAllegati) {
            if (Impostazioni.MAXDIM_ATTACH_ALT.valore != "N") {
                throw new GestioneDocumentiRuntimeException("La dimensione dei file allegati e il documento principale superano la dimensione massima consentita: ${dimensioneMaxAllegati} bytes.")
            } else if (forzabile == false) {
                throw new ValidaDimensioneAllegatiRuntimeException("Controllo NON bloccante sulla dimensione degli allegati")
            }
        }
    }

    /**
     * Recupera la dimensione del file prima della firma
     * select dbms_lob.getlength(FILE_DA_FIRMARE) from FIRMA_DIGITALE_FILE WHERE ID_RIFERIMENTO_FILE = ? AND TRANSAZIONE_ID = ?
     */
    @Transactional(readOnly = true)
    long dimensioneFileDaFirmare(long idTransazione, long idFileDocumento) {
        Sql sql = new Sql(dataSource)
        GroovyRowResult dim = sql.firstRow("select dbms_lob.getlength(FILE_DA_FIRMARE) from FIRMA_DIGITALE_FILE WHERE ID_RIFERIMENTO_FILE = " + idFileDocumento + " AND TRANSAZIONE_ID = " + idTransazione)
        if (dim != null && dim.values().size() > 0) {
            return dim.values().getAt(0).longValue()
        }
        return 0
    }

    /*
     * Gestione dei testi
     */

    static String creaIdRiferimento(Documento documento, FileDocumento fileDocumento) {
        return "${documento.id}/${fileDocumento.id}"
    }

    @Transactional(readOnly = true)
    boolean isEditorAperto() {
        return gestioneTestiService.isEditorAperto()
    }

    @Transactional(readOnly = true)
    boolean isTestoLockato(Documento documento, FileDocumento fileDocumento) {
        return (gestioneTestiService.getDettaglioLock(creaIdRiferimento(documento, fileDocumento)) != null)
    }

    EsitoRichiestaLock editaTesto(DocumentoDTO documentoDto, FileDocumentoDTO fileDocumentoDto) {
        Documento documento = documentoDto.domainObject
        FileDocumento fileDocumento = fileDocumentoDto.domainObject

        String log = "Edita testo sul fileDocumento con id: ${documento} ed il file: ${fileDocumento}"
        ad4Service.logAd4(log, log)

        String logAd4 = "Edita testo. Protocollo oggetto: ${fileDocumentoDto.documento?.oggetto}"
        if(fileDocumentoDto?.documento?.numero) logAd4 += " estremi: ${fileDocumentoDto.documento?.numero}/${fileDocumentoDto.documento?.anno} (${fileDocumentoDto.documento?.tipoRegistro?.commento})"
        String annotazione = "id file_documento: ${fileDocumento?.id?.toString()}"
        ad4Service.eventoLogAd4(logAd4, fileDocumento?.documento?.id?.toString(), annotazione)

        // #23078 controllo che il documento non sia in una fase di firma (è possibile se più utenti sono sullo stesso documento ed uno di questi clicca "firma", l'altro ancora vede il pulsante "Edita Testo" cliccabile)
        if (documento.statoFirma?.firmaInterrotta) {
            Clients.showNotification("Non è possibile editare il testo: è in corso un'operazione di firma.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true)
            return new EsitoRichiestaLock(esitoLock: false, messaggio: "Non è possibile editare il testo: è in corso un'operazione di firma.")
        }

        // se il testo non è modificabile, do' errore:
        if (fileDocumento != null && !fileDocumento.modificabile) {
            Clients.showNotification("Il testo risulta non più modificabile.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true)
            return new EsitoRichiestaLock(esitoLock: false, messaggio: "Il testo risulta non più modificabile.")
        }

        // se ho già un fileDocumento aperto non faccio nulla.
        if (gestioneTestiService.isEditorAperto()) {
            Clients.showNotification("Il testo risulta già aperto nell'editor.", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 3000, true)
            return new EsitoRichiestaLock(esitoLock: true, messaggio: "Il testo risulta già aperto nell'editor.")
        }

        // se non ho già il testo generato, mi assicuro di aggiornare il modello testo che è presente nel dto.
        if (fileDocumento.idFileEsterno == null && fileDocumentoDto.modelloTesto != null) {
            fileDocumento.modelloTesto = fileDocumentoDto.modelloTesto.domainObject
        }

        // genero il testo del documento
        generaTestoFile(documento, fileDocumento)

        // Ottengo l'id riferimento dell testo
        String idRiferimentoTesto = creaIdRiferimento(documento, fileDocumento)
        EsitoRichiestaLock esito = gestioneTestiService.apriEditorTesto(idRiferimentoTesto, springSecurityService.currentUser, gestoreFile.getFile(documento, fileDocumento), fileDocumento.nome, { InputStream inputStream ->
            Documento doc = documentoDto.domainObject

            // #23078 Questo controllo serve per intercettare quegli utenti che fanno "edita-testo" di un documento che un altro utente sta "firmando".
            // siccome la procedura di firma modifica il file (sia subito prima della firma che subito dopo), con questo controllo si evita che un altro utente oltre al firmatario possa sovrascrivere il testo da firmare.
            if (fileDocumento.toDTO().version != doc.filePrincipale.version) {
                throw new GestioneDocumentiRuntimeException("Un altro utente ha modificato il testo, non è possibile sovrascriverlo.")
            }

            gestoreFile.addFile(doc, fileDocumentoDto.domainObject, inputStream)

            // allineo la versione del dto a quella del fileDocumento salvato così passo il check sulla versione nel viewmodel chiamante... ugly thing..
            documentoDto.version = doc.version
            Clients.showNotification("Testo Salvato", Clients.NOTIFICATION_TYPE_INFO, null, "before_center", 3000, true)
        })

        //log Edita testo

        // allineo la versione del dto a quella del fileDocumento salvato così passo il check sulla versione nel viewmodel chiamante... ugly thing..
        documentoDto.version = documento.version
        return esito
    }

    void uploadEUnlockTesto(Documento documento, FileDocumento fileDocumento) {
        String idRiferimentoTesto = creaIdRiferimento(documento, fileDocumento)
        InputStream inputStream = gestioneTestiService.unlock(idRiferimentoTesto, springSecurityService.currentUser)

        // carico il fileDocumento solo se effettivamente l'ho.
        if (inputStream != null) {
            gestoreFile.addFile(documento, fileDocumento, inputStream)
        }
    }

    void eliminaTesto(DocumentoDTO documentoDto, FileDocumentoDTO fileDocumentoDto) {
        Documento documento = documentoDto.domainObject
        FileDocumento fileDocumento = fileDocumentoDto.domainObject
        String testoLog = "Elimina testo sul fileDocumento con id: ${documento} ed il file: ${fileDocumento}"
        ad4Service.logAd4(testoLog, testoLog)

        String logAd4 = "Elimina testo. Protocollo oggetto: ${fileDocumentoDto.documento?.oggetto}"
        if(fileDocumentoDto?.documento?.numero) logAd4 += " estremi: ${fileDocumentoDto.documento?.numero}/${fileDocumentoDto.documento?.anno} (${fileDocumentoDto.documento?.tipoRegistro?.commento})"
        String annotazione = "id file_documento: ${fileDocumento?.id?.toString()}"
        ad4Service.eventoLogAd4(logAd4, fileDocumento?.documento?.id?.toString(), annotazione)

        // prima elimino il lock se presente, poi elimino il testo:
        gestioneTestiService.eliminaLock(creaIdRiferimento(documento, fileDocumento), springSecurityService.currentUser)
        eliminaFileDocumento(documento, fileDocumento)
    }

    /**
     * Salva lo stream del file (storicizzandolo) e crea un nuovo FileDocumento associato al file passato.
     * Questa funzione è utile per salvare degli "snapshot" dei file in determinate condizioni, ad esempio prima della firma. In modo da poterli recuperare in un momento successivo.
     *
     * @param documento il documento padre del file
     * @param fileDocumento il file da storicizzare
     * @param codice il codice identificativo del file storicizzato
     */
    void salvaFileStorico(Documento documento, FileDocumento fileDocumento, String codice) {
        // cerco, se esiste già un file con questo codice collegato al fileDocumento:
        FileDocumento fileStorico = fileDocumento.getFileStorico(codice)
        if (fileStorico == null) {
            // creo un nuovo fileOriginale da associare al documento (in questo modo posso salvare diverse copie dello stesso file per diverse situazioni, ad es. prima della firma, dopo la prima firma etc.
            fileStorico = new FileDocumento(codice: codice)
            documento.addToFileDocumenti(fileStorico)
            fileDocumento.addToFileStorici(fileStorico)
        }

        fileStorico.nome = fileDocumento.nome
        fileStorico.modificabile = fileDocumento.modificabile
        fileStorico.modelloTesto = fileDocumento.modelloTesto
        fileStorico.contentType = fileDocumento.contentType
        fileStorico.dimensione = fileDocumento.dimensione
        fileStorico.idFileEsterno = fileDocumento.idFileEsterno
        fileStorico.save()

        fileDocumento.save()
        documento.save()
    }

    /**
     * Converte lo stream passato in pdf
     *
     * @param is lo stream da convertire
     * @return lo stream convertito in pdf
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    InputStream convertiStreamInPdf(InputStream is, String nomeFile, Documento documento) {
        GestioneTestiLog testiLog = saveLogBeforeConversion(nomeFile, documento)
        return convertAndLog(is, testiLog)

    }

    @Transactional
    GestioneTestiLog saveLogBeforeConversion(String nomeFile, Documento documento) {
        GestioneTestiLog testiLog = new GestioneTestiLog()
        testiLog.nomeFile = nomeFile
        testiLog.documento = documento
        testiLog.operazione = GestioneTestiLog.Operazione.CONVERSIONE_PDF
        gestioneTestiLogService.log(testiLog)
        return testiLog
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    InputStream convertAndLog(InputStream is, GestioneTestiLog testiLog) {
        try {
            InputStream i = gestioneTestiService.converti(is, TipoFile.PDF.estensione)
            testiLog.stato = GestioneTestiLog.Stato.SUCCESSO
            gestioneTestiLogService.log(testiLog)
            return i
        }
        catch (RuntimeException e) {

            testiLog.errore = e.localizedMessage
            testiLog.stato = GestioneTestiLog.Stato.ERRORE
            gestioneTestiLogService.log(testiLog)
            throw e
        }
        finally {
            testiLog.dataFineElaborazione = new Date()
            gestioneTestiLogService.log(testiLog)
        }
    }


    /**
     * Genera e salva il testo del file principale usando il modello di testo presente sul file.
     * Se il fileDocumento non ha il modello testo, viene generata un'eccezione ed interrotta l'esecuzione e la transazione.
     *
     * @param fileDocumento il riferimento al file di cui generare il testo
     * @param formato il formato in cui generare il testo: "pdf / odt / doc / docx"
     * @param liveData indica se usare i dati presenti nella connessione corrente, altrimenti viene usata una nuova connessione (necessaria al momento per gestire l'inserimento dei BLOB all'interno del file)
     * @return l'input stream del testo generato
     */
    void generaTestoFilePrincipale(Documento documento, String formato = Impostazioni.FORMATO_DEFAULT.valore, boolean liveData = true) {
        generaTestoFile(documento, documento.filePrincipale, formato, liveData)
    }

    /**
     * Genera e salva il testo del file usando il modello di testo presente sul file.
     * Se il fileDocumento non ha il modello testo, viene generata un'eccezione ed interrotta l'esecuzione e la transazione.
     *
     * @param fileDocumento il riferimento al file di cui generare il testo
     * @param formato il formato in cui generare il testo: "pdf / odt / doc / docx"
     * @param liveData indica se usare i dati presenti nella connessione corrente, altrimenti viene usata una nuova connessione (necessaria al momento per gestire l'inserimento dei BLOB all'interno del file)
     * @return l'input stream del testo generato
     */
    void generaTestoFile(Documento documento, FileDocumento fileDocumento, String formato = Impostazioni.FORMATO_DEFAULT.valore, boolean liveData = true, Map parametri = [id: documento.id]) {
        InputStream testoOdt = generaStreamTestoDocumento(fileDocumento, formato, liveData, parametri)
        gestoreFile.addFile(documento, fileDocumento, testoOdt, true)
    }

    /**
     * Genera il testo del file usando il modello di testo presente sul file.
     * Se il fileDocumento non ha il modello testo, viene generata un'eccezione ed interrotta l'esecuzione e la transazione.
     *
     * @param fileDocumento il riferimento al file di cui generare il testo
     * @param formato il formato in cui generare il testo: "pdf / odt / doc / docx"
     * @param liveData indica se usare i dati presenti nella connessione corrente, altrimenti viene usata una nuova connessione (necessaria al momento per gestire l'inserimento dei BLOB all'interno del file)
     * @return l'input stream del testo generato
     */
    @Transactional
    InputStream generaStreamTestoDocumento(FileDocumento fileDocumento, String formato = Impostazioni.FORMATO_DEFAULT.valore, boolean liveData = true, Map parametri) {
        if (fileDocumento.modelloTesto == null) {
            throw new GestioneDocumentiRuntimeException("Non è possibile generare il testo per il file '${fileDocumento.nome}' perché non ha un modello di testo associato.")
        }

        log.debug("Genero il testo per il fileDocumento ${fileDocumento}, con il modello: ${fileDocumento.modelloTesto.tipoModello.codice} e il formato: ${formato}")
        InputStream template = null
        try {

            // se ho già il testo, allora devo fare la "seconda passata", quindi riparto da quello e non dal template originale
            if (fileDocumento.idFileEsterno != null) {
                template = gestoreFile.getFile(fileDocumento.documento, fileDocumento, true)
            } else {
                template = new ByteArrayInputStream(fileDocumento.modelloTesto.fileTemplate)
            }

            // per la query live mi assicuro che il db sia allineato
            if (liveData) {
                fileDocumento.save()
            }

            return gestioneTestiService.stampaUnione(template, new String(fileDocumento.modelloTesto.tipoModello.query), parametri, formato, liveData)
        } finally {
            IOUtils.closeQuietly((InputStream) template)
        }
    }

    DocumentoDTO caricaFilePrincipale(DocumentoDTO documento, FileDocumentoDTO testo, InputStream fs, String contentType, String fileName) {
        Documento d = documento.domainObject
        if (testo) {
            eliminaTesto(documento, testo)
        }

        FileDocumento testoPricipale = new FileDocumento(codice: FileDocumento.CODICE_FILE_PRINCIPALE, nome: fileName, contentType: contentType, valido: true, modificabile: true, firmato: false)
        d.addToFileDocumenti(testoPricipale)
        gestoreFile.addFile(d, testoPricipale, fs)
        testoPricipale.save()

        fileDocumentoService.aggiornaVerificaFirma(testoPricipale)

        documento.version = d.version
        documento = d?.toDTO()
        documento.version = d.version
        return documento
    }

    @Transactional(readOnly = true)
    void anteprimaAllegatoPdf(Allegato allegato, FileDocumento fileAllegato) {
        InputStream anteprimaIn
        InputStream anteprimaPdf
        try {
            anteprimaIn = gestoreFile.getFile(allegato, fileAllegato)
            anteprimaPdf = convertiStreamInPdf(anteprimaIn, fileAllegato.nome, fileAllegato.documento)
            Filedownload.save(anteprimaPdf, TipoFile.PDF.contentType, fileAllegato.nomePdf)
        } catch (RuntimeException e) {
            log.error(e)
            throw new GestioneDocumentiRuntimeException("Impossibile convertire il file \"${fileAllegato.nome}\" contenuto nell'allegato \"${allegato.descrizione}\"")
        } finally {
            anteprimaIn?.close()
            anteprimaPdf?.close()
        }
    }

    @Transactional(readOnly = true)
    boolean esistonoFileNonPdf(Documento documento) {
        List<Allegato> listaAllegati = documento.getAllegati()
        for (Allegato allegato : listaAllegati) {
            if (!allegato.statoFirma.equals(StatoFirma.DA_FIRMARE)) {
                continue
            }
            for (FileDocumento fileAllegato : allegato.fileDocumenti?.findAll { it.valido }) {
                if (!fileAllegato.isPdf()) {
                    return true
                }
            }
        }
        return false
    }

    @Transactional
    public InputStream converti(Allegato allegato, FileDocumento fileAllegato) {
        InputStream daConvertire
        try {
            daConvertire = gestoreFile.getFile(allegato, fileAllegato)
            return convertiStreamInPdf(daConvertire, fileAllegato.nome, fileAllegato.documento)
        } finally {
            IOUtils.closeQuietly((InputStream) daConvertire)
        }
    }

    void convertiAllegatoPdf(Allegato allegato) {
        if (!allegato.statoFirma.equals(StatoFirma.DA_FIRMARE)) {
            return
        }
        for (FileDocumento fileAllegato : allegato.fileDocumenti?.findAll { it.valido == true }) {
            if (!Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(FilenameUtils.getExtension(fileAllegato.nome).toLowerCase())) {
                continue
            }

            converteInPdf(allegato, fileAllegato)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void converteInPdf(Allegato allegato, FileDocumento fileAllegato) {

        InputStream inputStreamPdf
        try {
            inputStreamPdf = converti(allegato, fileAllegato)

            FileDocumento file = FileDocumento.get(fileAllegato.id)
            Allegato all = Allegato.get(allegato.id)

            file.nome = file.nomePdf
            file.modificabile = false
            file.contentType = TipoFile.PDF
            gestoreFile.addFile(all, file, inputStreamPdf, true)
        } catch (RuntimeException e) {
            log.error("Errore durante la conversione del fileAllegato ${fileAllegato.id} dell'allegato ${allegato.id}", e);
        } finally {
            IOUtils.closeQuietly((InputStream) inputStreamPdf)
        }
    }

    void convertiAllegatiPdf(Documento documento) {
        List<Allegato> listaAllegati = documento.getAllegati()
        for (Allegato allegato : listaAllegati) {
            convertiAllegatoPdf(allegato)
        }
    }

    @Transactional(readOnly = true)
    boolean esistonoAllegatiNonPdf(Documento documento) {
        List<Allegato> listaAllegati = documento.getAllegati()
        for (Allegato allegato : listaAllegati) {
            if (!allegato.statoFirma.equals(StatoFirma.DA_FIRMARE)) {
                continue
            }
            for (FileDocumento fileAllegato : allegato.fileDocumenti?.findAll { it.valido }) {
                if (Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(FilenameUtils.getExtension(fileAllegato.nome).toLowerCase())) {
                    return true
                }
            }
        }
        return false
    }

    public void estraiInformazioniFileFirmati() {
        List<FileDocumento> fileDocumentiDaVerificare = getFileDocumentiDaVerificare()
        for (FileDocumento fileDocumento : fileDocumentiDaVerificare) {
            estraiInformazioneFileFirmato(fileDocumento, false)
        }
    }

    List<VerificatoreFirma.RisultatoVerifica> estraiInformazioneFileFirmato(FileDocumento fileAllegato, boolean verificaFirmaDaJob = true) {
        List<VerificatoreFirma.RisultatoVerifica> risultatiVerifica = new ArrayList<VerificatoreFirma.RisultatoVerifica>()
        //verificaFirma va fatta solo in caso non provenga dal JOB
        if (verificaFirmaDaJob) {

            boolean firmato = FirmaUtilities.isSigned(gestoreFile.getFile(fileAllegato.documento, fileAllegato, true), fileAllegato.nome)
            fileAllegato.firmato = firmato
            fileAllegato.save()

            risultatiVerifica = verificaFirma(gestoreFile.getFile(fileAllegato.documento, fileAllegato, true))
        }

        // per prima cosa, elimino tutti i firmatari del fileallegato (se ci sono), altrimenti mi ritrovo con dei doppi
        if (fileAllegato.firmatari?.size() > 0) {
            List<FileDocumentoFirmatario> firmatari = fileAllegato.firmatari.collect()
            for (FileDocumentoFirmatario firmatario : firmatari) {
                if (null != firmatario) {
                    fileAllegato.removeFromFirmatari(firmatario)
                    firmatario.delete()
                } else {
                    log.error("Il firmatario per il documento: " + fileAllegato.nome + " con id:" + fileAllegato.id + " risulta nullo. Non è stato possibile procedere con la cancellazione del firmatario.")
                }
            }
        }

        for (VerificatoreFirma.RisultatoVerifica utenteFirmatario : risultatiVerifica) {
            if (utenteFirmatario.firmatario) {
                FileDocumentoFirmatario fileDocumentoFirmatario = new FileDocumentoFirmatario()
                fileDocumentoFirmatario.stato = FileDocumentoFirmatario.VERIFICATO
                fileDocumentoFirmatario.dataFirma = utenteFirmatario.data
                fileDocumentoFirmatario.nominativo = utenteFirmatario.firmatario.replaceAll("Documento firmato da: ", "").toUpperCase()
                fileDocumentoFirmatario.dataVerifica = new Date()
                fileAllegato.addToFirmatari(fileDocumentoFirmatario)
                fileAllegato.save()
                fileDocumentoFirmatario.save()
            }
        }
        return risultatiVerifica
    }

    private List<FileDocumento> getFileDocumentiDaVerificare() {
        return FileDocumento.createCriteria().listDistinct {
            firmatari {
                eq("stato", FileDocumentoFirmatario.CALCOLATO)
            }
        }
    }

    private List<VerificatoreFirma.RisultatoVerifica> verificaFirma(InputStream input) {
        try {
            return new VerificatoreFirma(input).verificaFirma()
        } catch (Exception e) {
            // il file non è firmato
            return []
        }
    }

    @Transactional(readOnly = true)
    boolean esistonoAllegatiDaFirmareNonConvertibili(Documento documento) {
        for (Allegato allegato : documento.allegati) {
            if (!allegato.statoFirma.equals(StatoFirma.DA_FIRMARE)) {
                continue
            }

            for (FileDocumento fileAllegato : allegato.fileDocumenti?.findAll { it.valido }) {
                String estensione = FilenameUtils.getExtension(fileAllegato.nome).toLowerCase()
                if (!fileAllegato.isPdf() && !fileAllegato?.nome?.toLowerCase()?.endsWith(".pdf.p7m") && !Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(estensione) && Impostazioni.ALLEGATO_FORMATI_POSSIBILI.valori.contains(estensione)) {
                    return true
                }
            }
        }
        return false
    }

    @Transactional(readOnly = true)
    void applicaWatermarkFacsimile(InputStream inputStream, OutputStream outputStream) {
        PdfReader reader = new PdfReader(inputStream)
        PdfStamper stamper = new PdfStamper(reader, outputStream)
        Font f = new Font(Font.HELVETICA, 100)
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            PdfContentByte over = stamper.getOverContent(page)
            Phrase p = new Phrase("FACSIMILE", f)
            over.saveState()
            PdfGState gs1 = new PdfGState()
            gs1.setFillOpacity(0.1f)
            over.setGState(gs1)
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, p, 297, 450, 60)
            over.restoreState()
        }
        stamper.close()
        reader.close()
    }

    @Transactional
    Documento getDocumentoForOptimisticLock(Long id) {

        Documento documentoDomainReload = entityManager.find(Documento.class, id)

        Map<String, Object> properties = new HashMap<>();
        //10 min
        properties.put(AvailableSettings.LOCK_TIMEOUT, 6000000L);
        entityManager.lock(documentoDomainReload, LockModeType.OPTIMISTIC_FORCE_INCREMENT, properties)

        return documentoDomainReload
    }

    @Transactional(readOnly = true)
    Documento getDocumento(Long idDocumento){
        return Documento.get(idDocumento)
    }

    @Transactional(readOnly = true)
    Documento findById(Long id){
        return Documento.findById(id)
    }

    @Transactional(readOnly = true)
    Documento findByIdDocumentoEsterno(Long id){
        return Documento.findByIdDocumentoEsterno(id)
    }
}
