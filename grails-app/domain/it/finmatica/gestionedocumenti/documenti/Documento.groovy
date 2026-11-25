package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.HibernateUtils
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.soggetti.DocumentoSoggetto
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.annotations.Type

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_documenti")
@Inheritance(strategy = InheritanceType.JOINED)
class Documento extends AbstractDomainMultiEnte implements IDocumentoEsterno, IDocumentoIterabile {

    @GeneratedValue
    @Id
    @Column(name = "id_documento")
    Long id

    @Column(name = "campi_protetti", length = 4000)
    String campiProtetti

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "documento", orphanRemoval = true)
    Set<DocumentoCollegato> documentiCollegati

    @OrderColumn(name = "sequenza", nullable = false, updatable = false)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "documento", orphanRemoval = true)
    List<FileDocumento> fileDocumenti

    @Column(name = "id_documento_esterno")
    Long idDocumentoEsterno

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_engine_iter")
    WkfIter iter

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean riservato = false

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "documento", orphanRemoval = true)
    Set<DocumentoSoggetto> soggetti

    @Enumerated(EnumType.STRING)
    StatoDocumento stato

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_conservazione")
    StatoConservazione statoConservazione

    @Column(name = "anni_conservazione")
    Integer anniConservazione

    @Enumerated(EnumType.STRING)
    @Column(name = "stato_firma")
    StatoFirma statoFirma

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_oggetto")
    WkfTipoOggetto tipoOggetto

    @Version
    Long version

    Documento addToDocumentiCollegati(DocumentoCollegato value) {
        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegato>()
        }
        this.documentiCollegati.add(value);
        value.documento = this
        return this
    }

    Documento removeFromDocumentiCollegati(DocumentoCollegato value) {

        if (this.documentiCollegati == null) {
            this.documentiCollegati = new HashSet<DocumentoCollegato>()
        }
        this.documentiCollegati.remove((Object) value);
        value.documento = null

        return this
    }

    Documento addToFileDocumenti(FileDocumento value) {

        if (this.fileDocumenti == null) {
            this.fileDocumenti = new ArrayList<FileDocumento>()
        }

        FileDocumento file = null
        int index = 0
        for (FileDocumento f : this.fileDocumenti) {
            if (value.id != null && f.id == value.id) {
                file = value
                break
            }
            index = index + 1
        }
        if (file == null) {
            this.fileDocumenti.add(value)
        } else {
            this.fileDocumenti.set(index, value)
        }
        value.documento = this

        return this
    }

    List<FileDocumento> getFileDocumentiValidi(){
        return fileDocumenti.findAll {it?.valido}
    }


    Documento removeFromFileDocumenti(FileDocumento value) {

        if (this.fileDocumenti == null) {
            this.fileDocumenti = new ArrayList<FileDocumento>()
        }
        this.fileDocumenti.remove((Object) value);
        value.documento = null

        return this
    }

    Documento addToSoggetti(DocumentoSoggetto value) {
        if (this.soggetti == null) {
            this.soggetti = new HashSet<DocumentoSoggetto>()
        }
        this.soggetti.add(value);
        value.documento = this
        return this
    }

    Documento removeFromSoggetti(DocumentoSoggetto value) {
        if (this.soggetti == null) {
            this.soggetti = new HashSet<DocumentoSoggetto>()
        }

        this.soggetti.remove((Object) value);
        value.documento = null
        return this
    }

    /**
     * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
     * @param tipoSoggetto il tipo soggetto da settare
     * @param utenteAd4 l'utente del soggetto
     * @param unitaSo4 l'unità del soggetto
     */
    void setSoggetto(String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4) {
        setSoggetto(tipoSoggetto, utenteAd4, unitaSo4, 0)
    }

    /**
     * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
     *
     * Se utenteAd4 e unitaSo4 sono nulli, allora significa che voglio "eliminare" quel soggetto. Quindi se trovato, il soggetto viene eliminato.
     *
     * @param tipoSoggetto il tipo soggetto da settare
     * @param utenteAd4 l'utente del soggetto
     * @param unitaSo4 l'unità del soggetto
     */
    void setSoggetto(String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4, int sequenza) {
        DocumentoSoggetto documentoSoggetto = this.getSoggetto(tipoSoggetto)

        if (documentoSoggetto == null) {
            // http://svi-redmine/issues/14559
            // se non ho trovato il soggetto e i valori sono pure null, esco
            if (utenteAd4 == null && unitaSo4 == null) {
                return
            }

            documentoSoggetto = new DocumentoSoggetto()
            documentoSoggetto.tipoSoggetto = tipoSoggetto
            documentoSoggetto.sequenza = sequenza

            // quando aggiungo un nuovo soggetto, lo imposto subito come "attivo".
            documentoSoggetto.attivo = true
            addToSoggetti(documentoSoggetto)
        }

        // http://svi-redmine/issues/14559
        // se ho trovato il soggetto ma ne voglio "svuotare" i campi, allora lo elimino:
        if (documentoSoggetto != null &&
                utenteAd4 == null && unitaSo4 == null) {
            removeFromSoggetti(documentoSoggetto)
            documentoSoggetto.delete()
            // TODO: qui vanno gestiti i soggetti "multipli": attivo/non-attivo, sequenza.
            return
        }

        // infine, aggiorno i valori del soggetto.
        documentoSoggetto.utenteAd4 = utenteAd4

        // l'id di So4UnitaPubb e' So4UnitaId (chiave composta) che non funziona bene in Envers se questa è una relazione uno a molti
        // La gestione che non funziona è in questo metodo: org.hibernate.envers.internal.entities.mapper.id.SingleIdMapper#mapToMapFromEntity
        // che se l'entity è un proxy recupera la classe chiave (So4UnitaId);
        // quindi vengono fatti dei ragionamenti sullo stato dello storico riferiti alla chiave e non all'unità

        documentoSoggetto.unitaSo4  = HibernateUtils.unwrapIfProxy(unitaSo4)
    }

    /**
     * Ritorna il soggetto del documento
     *
     * @param tipoSoggetto il codice del soggetto da ritornare
     * @return il soggetto trovato, null altrimenti.
     */
    DocumentoSoggetto getSoggetto(String tipoSoggetto) {
        for (DocumentoSoggetto s in soggetti) {

            if (s.tipoSoggetto == tipoSoggetto && s.attivo) {
                return s
            }
        }
        return null
    }

    /**
     * Metodi per la gestione dei documenti collegati
     */
    void addDocumentoCollegato(Documento collegato, String codiceCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceCollegamento)
        if (tipoCollegamento == null) {
            throw new GestioneDocumentiRuntimeException("Non è stato possibile collegare il documento ${collegato} al documento ${this} perché il tipo di collegamento richiesto (codice: ${codiceCollegamento}) non esiste.")
        }

        addDocumentoCollegato(collegato, tipoCollegamento)
    }

    /**
     * Metodi per la gestione dei documenti allegati
     */
    void addDocumentoAllegato(Documento collegato) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(Allegato.CODICE_TIPO_COLLEGAMENTO)
        if (tipoCollegamento == null) {
            throw new GestioneDocumentiRuntimeException("Non è stato possibile collegare il documento ${collegato} al documento ${this} perché il tipo di collegamento richiesto (codice: ${codiceCollegamento}) non esiste.")
        }

        addDocumentoCollegato(collegato, tipoCollegamento)
    }

    void addDocumentoCollegato(Documento collegato, TipoCollegamento tipoCollegamento) {
        // controllo se ho già un documento per questo tipo di collegamento
        DocumentoCollegato documentoCollegato = getDocumentoCollegato(collegato, tipoCollegamento)
        if (documentoCollegato != null) {
            return
        }

        addToDocumentiCollegati(new DocumentoCollegato(documento: this, collegato: collegato, tipoCollegamento: tipoCollegamento))
    }

    DocumentoCollegato removeDocumentoCollegato(Documento collegato, String codiceCollegamento) {
        TipoCollegamento tipoCollegamento = TipoCollegamento.findByCodice(codiceCollegamento)
        if (tipoCollegamento == null) {
            throw new GestioneDocumentiRuntimeException("Non è stato possibile collegare il documento ${collegato} al documento ${this} perché il tipo di collegamento richiesto (codice: ${codiceCollegamento}) non esiste.")
        }
        return removeDocumentoCollegato(collegato, tipoCollegamento)
    }

    DocumentoCollegato removeDocumentoCollegato(Documento collegato, TipoCollegamento tipoCollegamento) {
        DocumentoCollegato documentoCollegato = getDocumentoCollegato(collegato, tipoCollegamento)
        if (documentoCollegato == null) {
            return documentoCollegato
        }

        removeFromDocumentiCollegati(documentoCollegato)
        documentoCollegato.delete()
        return documentoCollegato
    }

    List<Documento> getDocumentiCollegati(String codiceCollegamento, boolean ancheNonValidi = false) {
        List<Documento> documenti = []
        for (DocumentoCollegato documentoCollegato : this.documentiCollegati) {
            if(codiceCollegamento != null){
                if (documentoCollegato.tipoCollegamento.codice == codiceCollegamento && (documentoCollegato.valido || ancheNonValidi)) {
                    documenti << documentoCollegato.collegato
                }
            }
            else{
                throw new GestioneDocumentiRuntimeException("Specificare un tipo di collegamento")
            }
        }
        return documenti
    }

    DocumentoCollegato getDocumentoCollegato(Documento collegato, String codiceTipoCollegamento) {
        return getDocumentoCollegato(collegato, codiceTipoCollegamento)
    }

    DocumentoCollegato getDocumentoCollegato(Documento collegato, TipoCollegamento tipoCollegamento) {
        for (DocumentoCollegato documentoCollegato : this.documentiCollegati) {
            if (documentoCollegato.collegato.id == collegato.id && documentoCollegato.tipoCollegamento.id == tipoCollegamento.id) {
                return documentoCollegato
            }
        }

        return null
    }

    Documento getDocumentoCollegato(String codiceTipoCollegamento) {
        return getDocumentoCollegato(TipoCollegamento.findByCodice(codiceTipoCollegamento))
    }

    Documento getDocumentoCollegato(TipoCollegamento tipoCollegamento) {
        for (DocumentoCollegato documentoCollegato : this.documentiCollegati) {
            if (documentoCollegato.tipoCollegamento.id == tipoCollegamento.id) {
                return documentoCollegato.collegato
            }
        }

        return null
    }

    long getNumeroDocumentiCollegati(String codiceTipoCollegamento) {
        return getDocumentiCollegati(codiceTipoCollegamento).size()
    }

    FileDocumento getFile(String codice) {
        return fileDocumentiValidi?.find { it.codice == codice }
    }

    List<FileDocumento> getListaFile(String codice) {
        return fileDocumentiValidi?.findAll { it.codice == codice }
    }

    FileDocumento getFilePrincipale() {
        return getFile(FileDocumento.CODICE_FILE_PRINCIPALE)
    }

    FileDocumento getFileOriginale() {
        return getFile(FileDocumento.CODICE_FILE_ORIGINALE)
    }

    FileDocumento getFileFrontespizio() {
        return getFile(FileDocumento.CODICE_FILE_FRONTESPIZIO)
    }

    /**
     * Ritorna un elenco di tutti gli allegati validi ordinati per la sequenza di ordinamento.
     * @return
     */
    List<Allegato> getAllegati() {
        List<Allegato> allegatiValidi = []
        for (Documento allegato : getDocumentiCollegati(Allegato.CODICE_TIPO_COLLEGAMENTO)) {
            if (allegato.valido) {
                // Sono costretto a fare così perché Hibernate crea un proxy basato sull'entità base "Documento" non su "Allegato"
                allegatiValidi << (Allegato) HibernateUtils.unwrapIfProxy(allegato)
            }
        }
        return allegatiValidi.sort { [it.sequenza, it.id] }
    }

    long getIdDocumento() {
        if (id == null) {
            return -1
        }
        return id;
    }

    /*
     * Gestione dei file
     */

    static namedQueries = {
        numeroFilePerNome { Long idDocumento, String nomeFile ->
            projections {
                count("id")
            }

            eq("id", idDocumento)

            delegate.fileDocumenti {
                eq("nome", nomeFile)
            }
        }
    }

    List<String> getListaCampiProtetti() {
        return campiProtetti?.tokenize(',') ?: []
    }

    void proteggiCampo(String campo) {
        List<String> listaCampi = getListaCampiProtetti()
        if (!listaCampi.contains(campo)) {
            listaCampi << campo
            campiProtetti = listaCampi.join(',')
        }
    }

    void abilitaCampo(String campo) {
        List<String> listaCampi = getListaCampiProtetti()
        if (listaCampi.contains(campo)) {
            listaCampi.remove(campo)
            campiProtetti = listaCampi.join(',')
        }
    }

    Map<String, Boolean> getMappaCampiProtetti() {
        Map<String, Boolean> map = [:]
        List<String> lista = getListaCampiProtetti()
        for (String campo : lista) {
            map[campo] = true
        }

        return map
    }

    String getApplicativo() {
        return null
    }

    String getEstremi() {
        return null
    }

    transient boolean salvaRevisioneDocumentaleSuccessiva = false
}