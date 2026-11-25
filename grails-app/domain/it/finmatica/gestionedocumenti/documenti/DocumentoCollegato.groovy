package it.finmatica.gestionedocumenti.documenti

import it.finmatica.gestionedocumenti.commons.AbstractDomain

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_documenti_collegati")
class DocumentoCollegato extends AbstractDomain {

    @GeneratedValue
    @Id
    @Column(name = "id_documento_collegato")
    Long id

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_collegato", nullable = false)
    Documento collegato

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_documento", nullable = false)
    Documento documento

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_collegamento", nullable = false)
    TipoCollegamento tipoCollegamento

    @Version
    Long version

    static namedQueries = {
        /**
         * Ritorna l'elenco dei collegamenti in cui il documento passato risulta come "collegato"
         */
        collegamentiInversi { Documento documentoCollegato, def tipoColl = null ->

            if (tipoColl instanceof String) {
                tipoCollegamento {
                    eq("codice", tipoColl)
                }
            } else if (tipoColl instanceof TipoCollegamento) {
                eq("tipoCollegamento", tipoColl)
            }

            eq("collegato", documentoCollegato)
        }

        /**
         * Ritorna l'elenco dei collegamenti in cui il documento passato risulta come "documento"
         */
        collegamenti { Documento documento, def tipoColl = null ->

            if (tipoColl instanceof String) {
                tipoCollegamento {
                    eq("codice", tipoColl)
                }
            } else if (tipoColl instanceof TipoCollegamento) {
                eq("tipoCollegamento", tipoColl)
            }

            eq("documento", documento)
        }

        /**
         * Ritorna il n. di collegamenti in cui il documento passato risulta come "documento"
         */
        numeroCollegamenti { Documento documento, def tipoColl = null ->
            projections {
                count("id")
            }

            collegamenti(documento, tipoColl)
        }

        /**
         * Ritorna il n. di collegamenti in cui il documento passato risulta come "collegato"
         */
        numeroCollegamentiInversi { Documento collegato, def tipoColl = null ->
            projections {
                count("id")
            }

            collegamentiInversi(collegato, tipoColl)
        }
    }
}