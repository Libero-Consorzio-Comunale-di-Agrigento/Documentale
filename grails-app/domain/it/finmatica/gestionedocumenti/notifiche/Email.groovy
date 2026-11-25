package it.finmatica.gestionedocumenti.notifiche

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.commons.AbstractDomain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_email")
@CompileStatic
class Email extends AbstractDomain {

    @GeneratedValue
    @Id
    @Column(name = "id_email")
    Long id
    String cognome

    @Column(name = "indirizzo_email", nullable = false)
    String indirizzoEmail
    String nome

    @Column(name = "ragione_sociale")
    String ragioneSociale

    @Version
    Long version
}