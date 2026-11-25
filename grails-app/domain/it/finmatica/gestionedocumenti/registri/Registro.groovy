package it.finmatica.gestionedocumenti.registri

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version
import javax.persistence.JoinColumns

@Entity
@Table(name = "gdo_registri")
@CompileStatic
class Registro extends AbstractDomainMultiEnte {

    @GeneratedValue
    @Id
    @Column(name = "id_registro")
    Long id

    @Column(nullable = false)
    int anno

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean aperto

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ultima_data", nullable = false)
    Date dataUltimoNumero

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_registro")
    TipoRegistro tipoRegistro

    @Column(name = "ultimo_numero", nullable = false)
    int ultimoNumero

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns([@JoinColumn(name = "unita_emergenza_progr", referencedColumnName = "progr"),
            @JoinColumn(name = "unita_emergenza_dal", referencedColumnName = "dal"),
            @JoinColumn(name = "unita_emergenza_ottica", referencedColumnName = "ottica")])
    So4UnitaPubb  unitaEmergenza

    @Version
    Long version
}