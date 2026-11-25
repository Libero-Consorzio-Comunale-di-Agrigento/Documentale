package it.finmatica.gestionedocumenti.utils

import groovy.transform.CompileStatic

/**
 * Questa classe serve per eliminare i file da filesystem una volta che il loro stream è stato consumato.
 */
@CompileStatic
class FileInputStreamDeleteOnClose extends InputStream {

    @Delegate
    private InputStream inputStream
    private File file

    FileInputStreamDeleteOnClose(File file) {
        this.file = file
        inputStream = file.newInputStream()
    }

    void close() {
        inputStream.close()
        file.delete()
    }
}
