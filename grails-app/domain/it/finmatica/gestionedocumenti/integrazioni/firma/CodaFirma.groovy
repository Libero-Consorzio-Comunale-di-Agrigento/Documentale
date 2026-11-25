package it.finmatica.gestionedocumenti.integrazioni.firma

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.gestionedocumenti.documenti.StatoFirma
import org.hibernate.annotations.Type

import javax.persistence.CascadeType
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

@Entity
@Table(name = "gdo_coda_firma")
class CodaFirma extends AbstractDomainMultiEnte {

    @GeneratedValue
    @Id
    @Column(name = "id_coda_firma")
    Long id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_firma")
    Date dataFirma

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_documento")
    Documento documento

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_firmatario")
    Ad4Utente firmatario

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_firmatario_effettivo")
    Ad4Utente firmatarioEffettivo

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean firmato = false

    @Column(nullable = false)
    int sequenza

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_transazione_firma")
    FirmaDigitaleTransazione transazioneFirma

    @Version
    Long version

    static namedQueries = {

        documentiPerTransazione { FirmaDigitaleTransazione transazione, StatoFirma statoFirma = null ->
            projections {
                property("documento")
            }

            if (statoFirma != null) {
                documento {
                    eq("statoFirma", statoFirma)
                }
            }

            eq("transazioneFirma", transazione)
        }

        firmatariPerDocumento { Documento documento ->
            eq("documento", documento)
        }

        prossimoInCoda { Documento documento ->
            inCodaPerFirmare(documento, null, "asc")
            maxResults(1)
        }

        ultimoInCoda { Documento documento ->
            inCodaPerFirmare(documento, null, "desc")
            maxResults(1)
        }

        inCodaPerFirmare { Documento documento, Ad4Utente firmatario = null, String sort = "asc" ->
            firmatariPerDocumento(documento)
            isNull("dataFirma")
            eq("firmato", false)

            if (firmatario != null) {
                eq("firmatario", firmatario)
            } else {
                isNotNull("firmatario")
            }

            order("sequenza", sort)
        }

        cheStaFirmando { Documento documento, Ad4Utente firmatario = null ->
            firmatariPerDocumento(documento)
            isNotNull("dataFirma")
            eq("firmato", false)

            if (firmatario != null) {
                eq("firmatario", firmatario)
            } else {
                isNotNull("firmatario")
            }

            order("sequenza", "asc")
            maxResults(1)
        }

        cheHannoFirmato { Documento documento ->
            firmatariPerDocumento(documento)
            isNotNull("dataFirma")
            eq("firmato", true)
            order("sequenza", "desc")
        }

        ultimoCheHaFirmato { Documento documento ->
            firmatariPerDocumento(documento)
            isNotNull("dataFirma")
            eq("firmato", true)
            order("sequenza", "desc")
            maxResults(1)
        }

        //issue 71114
        firmePresenti { Documento documento ->
            firmatariPerDocumento(documento)
            eq ("firmato", true)
        }
    }
}