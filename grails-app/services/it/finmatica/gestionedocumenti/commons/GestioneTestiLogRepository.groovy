package it.finmatica.gestionedocumenti.commons


import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GestioneTestiLogRepository extends JpaRepository<GestioneTestiLog, Long> {

    @Query('''select d 
                from GestioneTestiLog d
               where d.documento.id = :idDocumento 
                 and (:dal is null or d.dateCreated >= :dal) 
                 and (:al  is null or d.dateCreated <= :al)
               order by d.dateCreated asc''')
    List<GestioneTestiLog> ricerca(@Param("idDocumento") long idDocumento, @Param("dal") Date dal, @Param("al") Date al)
}
