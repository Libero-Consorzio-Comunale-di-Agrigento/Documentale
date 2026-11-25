package it.finmatica.gestionedocumenti.commons

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.hibernate.UtenteIns
import it.finmatica.ad4.hibernate.UtenteUpd
import it.finmatica.spring.hibernatefilter.config.FilterDefaults
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.Type
import org.hibernate.annotations.UpdateTimestamp

import javax.persistence.Column
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass
import javax.persistence.Temporal
import javax.persistence.TemporalType

@CompileStatic
@FilterDefaults(name = AbstractDomain.SOLO_VALIDI_FILTER)
// abilito il filtro di default
@FilterDef(name = AbstractDomain.SOLO_VALIDI_FILTER, defaultCondition = "valido = 'Y'")
@Filter(name = AbstractDomain.SOLO_VALIDI_FILTER)
@MappedSuperclass
abstract class AbstractDomain {

    public static final String SOLO_VALIDI_FILTER = "soloValidiFilter"

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_ins", nullable = false)
    Date dateCreated

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_upd", nullable = false)
    Date lastUpdated

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_ins")
    @UtenteIns
    Ad4Utente utenteIns

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_upd")
    @UtenteUpd
    Ad4Utente utenteUpd

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean valido = true
}