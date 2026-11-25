package it.finmatica.gestionedocumenti.notifiche

import groovy.transform.CompileStatic
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_notifiche_email")
@CompileStatic
class NotificaEmail {

    @GeneratedValue
    @Id
    @Column(name = "id_notifica_email")
    Long id

    @Column(nullable = false)
    String destinatari

    @Column(name = "id_riferimento", nullable = false)
    String idRiferimento

    @Column(nullable = false)
    String oggetto

    @Column(nullable = false)
    String testo

    @Column(name = "tipo_notifica", nullable = false)
    String tipoNotifica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns([@JoinColumn(name = "unita_progr", referencedColumnName = "progr"),
            @JoinColumn(name = "unita_dal", referencedColumnName = "dal"),
            @JoinColumn(name = "unita_ottica", referencedColumnName = "ottica")])
    So4UnitaPubb unitaSo4

    @Version
    Long version
}