package it.finmatica.gestionedocumenti.commons

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.Holders
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiFilter
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import it.finmatica.spring.hibernatefilter.config.FilterDefaults
import it.finmatica.spring.hibernatefilter.config.ParamResolver
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef

import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

@CompileStatic
@FilterDefaults(name = GestioneDocumentiFilter.FILTER_NAME, resolvers = [@ParamResolver(name = "ente", resolver = GestioneDocumentiFilter)])
@FilterDef(name = GestioneDocumentiFilter.FILTER_NAME, defaultCondition = "id_ente = :ente", parameters = [@ParamDef(name = "ente", type = "long")])
@Filter(name = GestioneDocumentiFilter.FILTER_NAME)
@MappedSuperclass
abstract class AbstractDomainMultiEnte extends AbstractDomain {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ente")
    Ente ente

    @PrePersist
    void beforeInsert() {
        ente = ente ?: (Ente) Holders.getApplicationContext().getBean(GestioneDocumentiSpringSecurityService).getPrincipal().getEnte()
    }

    @PreUpdate
    void beforeUpdate() {
        ente = ente ?: (Ente) Holders.getApplicationContext().getBean(GestioneDocumentiSpringSecurityService).getPrincipal().getEnte()
    }
}