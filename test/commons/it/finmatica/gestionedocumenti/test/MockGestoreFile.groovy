package it.finmatica.gestionedocumenti.test

import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.FileDocumento
import it.finmatica.gestionedocumenti.documenti.IDocumentoEsterno
import it.finmatica.gestionedocumenti.documenti.IFileDocumento
import it.finmatica.gestionedocumenti.documenti.IGestoreFile

class MockGestoreFile implements IGestoreFile {
    private long index = 0
    private Map<Long, File> files = [:]

    @Override
    void addFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, InputStream is, boolean escludiControlloCompetenze = false) {
        File f = File.createTempFile("temp", "temp")
        f << is
        files.put(++index, f)
        fileDocumento.idFileEsterno = index
        documento.idDocumentoEsterno = index
    }

    @Override
    void addFileToRepositoryDocumentale(Documento documento, FileDocumento fileDocumento, InputStream is, Long idDocumentoRepository = null) {
        //todo
    }

    @Override
    InputStream getFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, boolean escludiControlloCompetenze = false) {
        return files[fileDocumento.idFileEsterno].newInputStream()
    }

    @Override
    void removeFile(IDocumentoEsterno documento, IFileDocumento fileDocumento) {
        files.remove(fileDocumento.idFileEsterno)
    }

    @Override
    void removeFile(IDocumentoEsterno documento, IFileDocumento fileDocumento, boolean spostafile) {
        files.remove(fileDocumento.idFileEsterno)
    }

    @Override
    void renameFileName(Long idDocumentoEsterno, Long idFileEsterno, String fileName) {
    }
}
