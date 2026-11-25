package it.finmatica.gestionedocumenti.documenti

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "gdo_tipi_collegamento")
@CompileStatic
class TipoCollegamento extends AbstractDomainMultiEnte {

    @GeneratedValue
    @Id
    @Column(name = "id_tipo_collegamento")
    Long id

    // definito da java per identificare il tipo di collegamento
    @Column(name = "tipo_collegamento", nullable = false, unique = true)
    String codice

    String commento

    @Column(nullable = false)
    String descrizione

    @Type(type = "yes_no")
    boolean sistema
}