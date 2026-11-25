package it.finmatica.gestionedocumenti.commons

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.documenti.Documento
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType

@Entity
@Table(name = "gte_log_view")
@CompileStatic
class GestioneTestiLog extends AbstractDomain {

    enum Stato {
        SUCCESSO, ERRORE
    }

    enum Operazione {
        CONVERSIONE_PDF
    }

    @GeneratedValue
    @Id
    @Column(name = "id_gte_log")
    Long id

    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "id_documento")
    Documento documento

    @Column
    String applicativo

    @Column(nullable = false)
    String stato

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_fine_elaborazione")
    Date dataFineElaborazione

    @Column(nullable = false)
    String operazione

    @Column
    String estremiDocumento

    @Column(name = "nome_file", nullable = false)
    String nomeFile

    @Lob
    String errore

    boolean isStatoSuccesso() {
        return (stato == Stato.SUCCESSO)
    }

    boolean isStatoErrore() {
        return (stato == Stato.ERRORE)
    }
}