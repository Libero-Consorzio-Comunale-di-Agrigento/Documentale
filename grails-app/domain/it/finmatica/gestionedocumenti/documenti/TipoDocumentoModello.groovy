package it.finmatica.gestionedocumenti.documenti

import groovy.transform.CompileStatic
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.hibernate.annotations.Type

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_tipi_documento_modelli")
@CompileStatic
class TipoDocumentoModello {

    @GeneratedValue
    @Id
    @Column(name = "id_tipo_documento_modello")
    Long id

    @Column(nullable = false)
    String codice

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modello")
    GestioneTestiModello modelloTesto

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean predefinito

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento")
    TipoDocumento tipoDocumento

    @Version
    Long version
}