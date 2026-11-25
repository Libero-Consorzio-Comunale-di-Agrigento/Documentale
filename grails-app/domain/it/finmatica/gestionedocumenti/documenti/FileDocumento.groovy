package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gestionedocumenti.commons.AbstractDomain
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.apache.commons.io.FilenameUtils
import org.hibernate.annotations.Type

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@Table(name = "gdo_file_documento")
class FileDocumento extends AbstractDomain implements IFileDocumento {

    // allegato "generico"
    public static final String CODICE_FILE_ALLEGATO = "FILE_ALLEGATO"
    public static final String CODICE_FILE_PRINCIPALE = "FILE_PRINCIPALE"
    public static final String CODICE_FILE_ORIGINALE = "FILE_ORIGINALE"
    public static final String CODICE_FILE_FRONTESPIZIO = "FILE_FRONTESPIZIO"
    public static final String CODICE_FILE_EML = "FILE_EML"

    public static final String ESITO_VERIFICATO = 'V'
    public static final String ESITO_FALLITO = 'N'
    public static final String ESITO_NON_VERIFICATO = ''
    public static final String ESITO_FORZATO = 'F'

    @GeneratedValue
    @Id
    @Column(name = "id_file_documento")
    Long id

    // identifica il "tipo di collegamento" che c'è tra il file documento ed il Documento. Ad es: ALLEGATO, STAMPA_UNICA, FILE_PRINCIPALE
    // in caso di FileDocumento "storico" (cioè con revisione > 0) questo campo indica il tipo di "snapshot" del file
    @Column(nullable = false)
    String codice = CODICE_FILE_ALLEGATO

    @Column(name = "content_type", nullable = false)
    String contentType

    @Column(nullable = false)
    long dimensione

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_documento")
    Documento documento

    // punta al file "padre" di questo file.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_originale_id")
    FileDocumento fileOriginale

    // elenco degli "snapshot" dei file storici salvati
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "fileOriginale", orphanRemoval = true)
    Set<FileDocumento> fileStorici

    @OrderColumn(name = "sequenza", nullable = false, updatable = false)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "fileDocumento", orphanRemoval = true)
    List<FileDocumentoFirmatario> firmatari

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean firmato = false

    // indica l'id del file se salvato su un repository esterno (ad es GDM)
    @Column(name = "id_file_esterno")
    Long idFileEsterno

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean marcato = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modello_testo")
    GestioneTestiModello modelloTesto

    // indica se il documento è un testo ancora modificabile dall'utente o no.
    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean modificabile = false

    @Column(nullable = false)
    String nome

    // indica la revisione dello storico su gdm a cui è possibile recuperare questo file. La revisione è riferita al documento GDM principale (quindi al documento.idDocumentoEsterno)
    // viene valorizzato solo per i file con codice "FILE_ORIGINALE" che riferiscono ai file prima della firma
    @Column(name = "revisione_storico")
    Long revisione

    @Column(nullable = false)
    int sequenza

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_verifica")
    Date dataVerifica

    @Column(name = "esito_verifica")
    String esitoVerifica

    @Lob
    String testo

    @Version
    Long version

    FileDocumento addToFileStorici(FileDocumento value) {
        if (this.fileStorici == null) {
            this.fileStorici = new HashSet<FileDocumento>()
        }
        this.fileStorici.add(value);
        value.fileOriginale = this
        return this
    }

    FileDocumento removeFromFileStorici(FileDocumento value) {
        if (this.fileStorici == null) {
            this.fileStorici = new HashSet<FileDocumento>()
        }
        this.fileStorici.remove((Object) value);
        value.fileOriginale = null
        return this
    }

    FileDocumento addToFirmatari(FileDocumentoFirmatario value) {
        if (this.firmatari == null) {
            this.firmatari = new ArrayList<FileDocumentoFirmatario>()
        }

        FileDocumentoFirmatario firmatario = null
        int index = 0
        for (FileDocumentoFirmatario f : this.firmatari) {
            if (value.id != null && f.id == value.id) {
                firmatario = value
                break
            }
            index = index + 1
        }
        if (firmatario == null) {
            this.firmatari.add(value)
        } else {
            this.firmatari.set(index, value)
        }
        value.fileDocumento = this
        return this
    }

    FileDocumento removeFromFirmatari(FileDocumentoFirmatario value) {
        if (this.firmatari == null) {
            this.firmatari = new ArrayList<FileDocumentoFirmatario>()
        }
        this.firmatari.remove((Object) value);
        value.fileDocumento = null
        return this
    }

    static namedQueries = {
        fileStorico { FileDocumento filePadre, String codice ->
            eq("fileOriginale", filePadre)
            eq("codice", codice)
        }
    }

    boolean isPdf() {
        return this.nome.toLowerCase().endsWith("pdf")
    }

    boolean isP7m() {
        return this.nome.toLowerCase().endsWith("p7m")
    }

    boolean isTsd(){
        return this.nome.toLowerCase().endsWith("tsd")
    }

    boolean isSbustabile(){
        return isP7m() || isTsd()
    }

    String getNomePdf() {
        return this.nome.replaceAll(/\..+$/, ".pdf")
    }

    @Override
    String getNomeFileSbustato() {
        return nomeSbustato(this.nome)
    }


    FileDocumento getFileStorico(String codice) {
        return FileDocumento.fileStorico(this, codice).get()
    }

    //Ripulisco il nome del file dagli spazi iniziali e finali
    void setNome(String nome) {
        this.nome = nome.trim()
    }

    boolean isConvertibilePdf() {
        return (Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(FilenameUtils.getExtension(nome).toLowerCase()));
    }

    static String nomeSbustato(String nome){
        String nomeFile = nome
        if (nomeFile.toLowerCase().endsWith("p7m")){
            return nomeSbustato(nomeFile.replaceAll(/(\.[pP]7[mM])$/, ""))
        }
        if (nomeFile.toLowerCase().endsWith("tsd")){
            return nomeSbustato(nomeFile.replaceAll(/(\.[tT][sS][dD])$/, ""))
        }
        return nomeFile
    }

}