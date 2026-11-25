package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import it.finmatica.gestionedocumenti.soggetti.TipologiaSoggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import org.hibernate.annotations.Type

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_tipi_documento")
@Inheritance(strategy = InheritanceType.JOINED)
class TipoDocumento extends AbstractDomainMultiEnte {

    @GeneratedValue
    @Id
    @Column(name = "id_tipo_documento")
    Long id

    String acronimo

    // codice univoco che identifica il tipo documento nell'applicativo
    String codice

    String commento

    // se il protocollo deve andare in conservazione
    @Type(type = "yes_no")
    @Column(name = "conservazione_sostitutiva", nullable = false)
    boolean conservazioneSostitutiva

    @Column(nullable = false)
    String descrizione

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tipoDocumento", orphanRemoval = true)
    Set<TipoDocumentoModello> modelliAssociati

    @Column(name = "progressivo_cfg_iter")
    Long progressivoCfgIter

    // indica se il testo deve essere presente tra uno step e l'altro.
    @Type(type = "yes_no")
    @Column(name = "testo_obbligatorio", nullable = false)
    boolean testoObbligatorio

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipologia_soggetto")
    TipologiaSoggetto tipologiaSoggetto

    @Version
    Long version

    WkfCfgIter getCfgIter() {
        return WkfCfgIter.getIterIstanziabile(progressivoCfgIter).get()
    }

    static namedQueries = {
        inUsoPerModelloTesto { long idModelloTesto ->
            or {
                modelliAssociati {
                    eq("modelloTesto.id", idModelloTesto)
                }
            }
            eq("valido", true)
        }

        inUsoPerTipologiaSoggetto { long idTipologia ->
            eq("tipologiaSoggetto.id", idTipologia)
            eq("valido", true)
        }

        modelliTesto { long idTipoDocumento, String codice ->
            projections {
                delegate.modelliAssociati {
                    property("modelloTesto")
                }
            }
            eq("id", idTipoDocumento)
            delegate.modelliAssociati {
                eq("codice", codice)
            }
        }

        modelloTestoPredefinito { long idTipoDocumento, String codice ->
            modelliTesto(idTipoDocumento, codice)
            delegate.modelliAssociati {
                eq("predefinito", true)
            }
        }
    }
}