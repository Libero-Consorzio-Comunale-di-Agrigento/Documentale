package it.finmatica.gestionedocumenti.soggetti

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.annotations.Type

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
@Table(name = "gdo_documenti_soggetti")
@CompileStatic
class DocumentoSoggetto {

    @GeneratedValue
    @Id
    @Column(name = "id_documento_soggetto")
    Long id

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean attivo = true

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_documento", nullable = false)
    Documento documento

    // campi per la gestione di più soggetti.
    @Column(nullable = false)
    int sequenza

    @Column(name = "tipo_soggetto", nullable = false)
    String tipoSoggetto

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumns([@JoinColumn(name = "unita_progr", referencedColumnName = "progr"),
            @JoinColumn(name = "unita_dal", referencedColumnName = "dal"),
            @JoinColumn(name = "unita_ottica", referencedColumnName = "ottica")])
    So4UnitaPubb unitaSo4

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente")
    Ad4Utente utenteAd4

    @Version
    Long version
}