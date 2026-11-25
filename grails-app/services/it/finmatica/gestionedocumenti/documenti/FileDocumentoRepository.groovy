package it.finmatica.gestionedocumenti.documenti


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FileDocumentoRepository extends JpaRepository<FileDocumento, Long> {

    @Query('''select f 
                from FileDocumento f
               where f.documento.id = :idDocumento 
                 and f.fileOriginale.id = :idFilePrincipale
    ''')
    FileDocumento getFileOriginale(@Param("idDocumento") long idDocumento, @Param("idFilePrincipale") long idFilePrincipale)

    @Query(value = '''select count(1)
        from gdo_file_documento f 
        where f.id_file_esterno = :idFileEsterno
        ''', nativeQuery = true)
    Integer numeroDocumentiDocumentaleAssociati(@Param("idFileEsterno") long idFileEsterno)
}
