package it.finmatica.gestionedocumenti.documenti;

import java.io.InputStream;

public interface IGestoreFile {

    void addFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, InputStream is);

    void addFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, InputStream is, boolean escludiControlloCompetenze);

    void addFileToRepositoryDocumentale(Documento documento, FileDocumento fileDocumento, InputStream is, Long idDocumentoRepository);

    InputStream getFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, boolean escludiControlloCompetenze);

    InputStream getFile(IDocumentoEsterno documento, IFileDocumento fileDocumento);

    void removeFile(IDocumentoEsterno documento, IFileDocumento fileDocumento);

    void removeFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, boolean spostaFile);

    void renameFileName(Long idDocumentoEsterno, Long idFileEsterno, String fileName);
}
