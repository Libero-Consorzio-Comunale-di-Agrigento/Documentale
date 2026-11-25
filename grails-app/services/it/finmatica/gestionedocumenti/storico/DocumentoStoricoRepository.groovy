package it.finmatica.gestionedocumenti.storico

import it.finmatica.gestionedocumenti.documenti.Documento
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DocumentoStoricoRepository extends JpaRepository<DocumentoStorico, Long> {

    /**
     * Ottiene l'ultima revisione del documento (se presente)
     *
     * @param documento
     * @return
     */
    @Deprecated
    DocumentoStorico findFirstByDocumentoOrderByDateCreatedDesc(@Param("documento") Documento documento)

    @Query('''select d 
                from DocumentoStorico d
               where d.documento = :documento                
               order by d.dateCreated desc''')
    List<DocumentoStorico> getDocumentiStoriciOrderByDateCreatedDesc(@Param("documento") Documento documento)

    @Query('''select d 
                from DocumentoStorico d
               where d.documento.id = :idDocumento 
                 and (:dal is null or d.dateCreated >= :dal) 
                 and (:al  is null or d.dateCreated <= :al)
               order by d.dateCreated asc''')
    List<DocumentoStorico> ricerca(@Param("idDocumento") long idDocumento, @Param("dal") Date dal, @Param("al") Date al)

    @Query('''select count(d)
                from DocumentoStorico d
               where d.documento.id= :idDocumento                
               and d.versioneStorico = :versioneStorico''')
    Integer countDocumentiStoriciTrascodificati(@Param("idDocumento") Long idDocumento, @Param("versioneStorico") String versioneStorico)

}
