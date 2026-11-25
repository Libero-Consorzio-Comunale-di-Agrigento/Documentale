package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.gestionetesti.reporter.GestioneTestiModelloDTO
import org.apache.commons.io.FilenameUtils

public class FileDocumentoDTO implements it.finmatica.dto.DTO<FileDocumento>, IFileDocumento {
    private static final long serialVersionUID = 1L;

    Long id
    Long version
    String codice
    String contentType
    Date dateCreated
    long dimensione
    long sequenza
    GestioneTestiModelloDTO modelloTesto
    DocumentoDTO documento
    List<FileDocumentoFirmatarioDTO> firmatari
    Set<FileDocumentoDTO> fileStorici
    boolean firmato
    boolean marcato;
    Long idFileEsterno
    Date lastUpdated
    boolean modificabile
    String nome
    String testo
    Ad4UtenteDTO utenteIns
    Ad4UtenteDTO utenteUpd
    boolean valido
    FileDocumentoDTO fileOriginale
    Date dataVerifica
    String esitoVerifica
    Long revisione

    public void addToFirmatari(FileDocumentoFirmatarioDTO fileDocumentoFirmatario) {
        if (this.firmatari == null)
            this.firmatari = new ArrayList<FileDocumentoFirmatarioDTO>()
        this.firmatari.add(fileDocumentoFirmatario);
        fileDocumentoFirmatario.fileDocumento = this
    }

    public void removeFromFirmatari(FileDocumentoFirmatarioDTO fileDocumentoFirmatario) {
        if (this.firmatari == null)
            this.firmatari = new ArrayList<FileDocumentoFirmatarioDTO>()
        this.firmatari.remove(fileDocumentoFirmatario);
        fileDocumentoFirmatario.fileDocumento = null
    }

    public void addToFileStorici(FileDocumentoDTO fileDocumento) {
        if (this.fileStorici == null)
            this.fileStorici = new ArrayList<FileDocumentoDTO>()
        this.fileStorici.add(fileDocumento);
        fileDocumento.fileOriginale = this
    }

    public void removeFromFileStorici(FileDocumentoFirmatarioDTO fileDocumentoFirmatario) {
        if (this.firmatari == null)
            this.firmatari = new ArrayList<FileDocumentoFirmatarioDTO>()
        this.firmatari.remove(fileDocumentoFirmatario);
        fileDocumentoFirmatario.fileDocumento = null
    }

    public FileDocumento getDomainObject() {
        return FileDocumento.get(this.id)
    }

    public FileDocumento copyToDomainObject() {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    // Questo metodo serve per evitare un bug sul toDTO causato dalle BeanUtils. Issue #31429
    void setDocumento(DocumentoDTO documentoDTO) {
        this.documento = documentoDTO
    }

    // Questo metodo serve per evitare un bug sul toDTO causato dalle BeanUtils. Issue #31429
    void setDocumento(IDocumentoEsterno documentoEsterno) {
        this.documento = (DocumentoDTO) documentoEsterno
    }

    // Questo metodo serve per evitare un bug sul toDTO causato dalle BeanUtils. Issue #31429
    IDocumentoEsterno getDocumento() {
        return this.documento
    }

    public String getDimensioneMB() {
        return new Double(((dimensione) / 1_000_000)).round(2) + " MB"
    }

    @Override
    boolean isPdf() {
        return this.nome.toLowerCase().endsWith("pdf")
    }

    @Override
    boolean isP7m() {
        return this.nome.toLowerCase().endsWith("p7m")
    }

    @Override
    boolean isTsd(){
        return this.nome.toLowerCase().endsWith("tsd")
    }

    @Override
    boolean isSbustabile(){
        return isP7m() || isTsd()
    }

    @Override
    String getNomePdf() {
        return this.nome.replaceAll(/\..+$/, ".pdf")
    }

    @Override
    String getNomeFileSbustato() {
        return FileDocumento.nomeSbustato(this.nome)
    }

    public boolean isConvertibilePdf() {
        return (Impostazioni.ALLEGATO_CONVERTI_PDF_FORMATO.valori.contains(FilenameUtils.getExtension(nome).toLowerCase()));
    }
}
