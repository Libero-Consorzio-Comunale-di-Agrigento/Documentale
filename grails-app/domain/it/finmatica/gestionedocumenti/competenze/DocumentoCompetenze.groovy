package it.finmatica.gestionedocumenti.competenze

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgCompetenza
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.UnitaOrganizzativa
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
@Table(name = "gdo_documenti_competenze")
class DocumentoCompetenze {

    @GeneratedValue
    @Id
    @Column(name = "id_documento_competenza")
    Long id

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean cancellazione

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cfg_competenza")
    WkfCfgCompetenza cfgCompetenza

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_documento")
    Documento documento

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
    @JoinColumns([@JoinColumn(name = "unita_progr", referencedColumnName = "progr"),
            @JoinColumn(name = "unita_dal", referencedColumnName = "dal"),
            @JoinColumn(name = "unita_ottica", referencedColumnName = "ottica")])
    So4UnitaPubb unitaSo4

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente")
    Ad4Utente utenteAd4

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ente")
    Ente ente

    @Version
    Long version

    static namedQueries = {
        canRead { Documento d, So4UserDetail utente ->
            competenze(d, utente)
            eq("lettura", true)
        }

        canWrite { Documento d, So4UserDetail utente ->
            competenze(d, utente)
            eq("modifica", true)
        }

        canDelete { Documento d, So4UserDetail utente ->
            competenze(d, utente)
            eq("cancellazione", true)
        }

        competenze { Documento d, So4UserDetail utente ->
            if (d != null) {
                eq("documento", d)
            }

            or {
                utente.uo().each { UnitaOrganizzativa uo ->
                    or {
                        and {
                            isNull("utenteAd4")
                            eq("utenteAd4.id", utente.id)
                        }
                        and {
                            unitaSo4 {
                                eq("progr", uo.id)
                                eq("dal", uo.dal)
                                eq("ottica.codice", uo.ottica)
                            }
                            'in'("ruoloAd4.ruolo", uo.ruoli*.codice)
                        }
                        and {
                            isNull("unitaSo4")
                            'in'("ruoloAd4.ruolo", uo.ruoli*.codice)
                        }
                        and {
                            unitaSo4 {
                                eq("progr", uo.id)
                                eq("dal", uo.dal)
                                eq("ottica.codice", uo.ottica)
                            }
                            isNull("ruoloAd4")
                        }
                        and {
                            isNull("unitaSo4")
                            isNull("ruoloAd4")
                        }
                    }
                }
            }
        }
    }

    static void verificaCompetenza(Documento d, So4UserDetail utente, boolean lettura = true, boolean modifica = false, boolean cancellazione = false) {
        if (cancellazione) {
            int count = DocumentoCompetenze.canDelete(d, utente).count()
            if (count == 0) {
                throw new RuntimeException("L'utente corrente ${utente.username} non ha i permessi di cancellare il documento con id ${d.id}")
            }

            return
        }

        if (modifica) {
            int count = DocumentoCompetenze.canWrite(d, utente).count()
            if (count == 0) {
                throw new RuntimeException("L'utente corrente ${utente.username} non ha i permessi di modificare il documento con id ${d.id}")
            }

            return
        }

        if (lettura) {
            int count = DocumentoCompetenze.canRead(d, utente).count()
            if (count == 0) {
                throw new RuntimeException("L'utente corrente ${utente.username} non ha i permessi di leggere il documento con id ${d.id}")
            }

            return
        }
    }
}