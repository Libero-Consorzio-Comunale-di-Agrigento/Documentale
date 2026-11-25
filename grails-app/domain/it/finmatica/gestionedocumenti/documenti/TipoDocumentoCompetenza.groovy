package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.annotations.LazyToOne
import org.hibernate.annotations.LazyToOneOption
import org.hibernate.annotations.Type

import javax.persistence.CascadeType
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
@Table(name = "gdo_tipi_documento_competenze")
class TipoDocumentoCompetenza {

    @GeneratedValue
    @Id
    @Column(name = "id_tipo_documento_competenze")
    Long id

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean cancellazione

    @Column(nullable = false)
    String descrizione

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean lettura

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean modifica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruolo")
    Ad4Ruolo ruoloAd4

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_documento")
    TipoDocumento tipoDocumento

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns([@JoinColumn(name = "unita_progr", referencedColumnName = "progr"),
            @JoinColumn(name = "unita_dal", referencedColumnName = "dal"),
            @JoinColumn(name = "unita_ottica", referencedColumnName = "ottica")])
    So4UnitaPubb unitaSo4

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente")
    Ad4Utente utenteAd4

    @Version
    Long version

    static namedQueries = {
        canRead { TipoDocumento a, So4UserDetail utente ->
            competenze(a, utente)
            eq("lettura", true)
        }

        canWrite { TipoDocumento a, So4UserDetail utente ->
            competenze(a, utente)
            eq("modifica", true)
        }

        canDelete { TipoDocumento a, So4UserDetail utente ->
            competenze(a, utente)
            eq("cancellazione", true)
        }

        competenze { TipoDocumento a, So4UserDetail utente ->
            if (a != null) {
                eq("tipoDocumento", a)
            }
            or {
                // se sono l'utente
                eq("utenteAd4.id", utente.id)

                // se ho il ruolo con unità null
                and {
                    isNull("unitaSo4")
                    or {
                        for (String codice : utente.uo().ruoli.flatten().codice.unique()) {
                            eq("ruoloAd4.ruolo", codice)
                        }
                    }
                }

                for (UnitaOrganizzativa uo : utente.uo()) {

                    // se ho il ruolo per l'unità
                    and {
                        and {
                            eq("unitaSo4.progr", uo.id)
                            eq("unitaSo4.ottica.codice", uo.ottica)
                        }
                        or {
                            for (def r : uo.ruoli) {
                                eq("ruoloAd4.ruolo", r.codice)
                            }

                            // se ho l'unità ma con il ruolo null
                            isNull("ruoloAd4")
                        }
                    }
                }
            }
        }
    }
}