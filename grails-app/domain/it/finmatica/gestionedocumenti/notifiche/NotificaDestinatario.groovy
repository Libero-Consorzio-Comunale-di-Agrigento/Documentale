package it.finmatica.gestionedocumenti.notifiche

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.annotations.LazyToOne
import org.hibernate.annotations.LazyToOneOption

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_notifiche_destinatari")
@CompileStatic
class NotificaDestinatario extends AbstractDomainMultiEnte {

    @GeneratedValue
    @Id
    @Column(name = "id_notifica_destinatario")
    Long id

    // competenza o conoscenza
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    NotificaSoggetto.Assegnazione assegnazione

    // indirizzo email a cui spedire la notifica.
    String email

    // eventuale nome della funzione da invocare
    String funzione

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_notifica")
    Notifica notifica

    // eventuale ruolo a cui inviare la notifica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruolo")
    Ad4Ruolo ruolo

    // eventuale soggetto a cui inviare la notifica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_soggetto")
    As4SoggettoCorrente soggetto

    // eventuale unità a cui inviare la notifica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns([@JoinColumn(name = "unita_progr", referencedColumnName = "progr"),
            @JoinColumn(name = "unita_dal", referencedColumnName = "dal"),
            @JoinColumn(name = "unita_ottica", referencedColumnName = "ottica")])
    So4UnitaPubb unita

    @Version
    Long version
}