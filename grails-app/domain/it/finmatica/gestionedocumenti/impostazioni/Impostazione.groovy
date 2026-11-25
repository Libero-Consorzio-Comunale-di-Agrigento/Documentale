package it.finmatica.gestionedocumenti.impostazioni

import it.finmatica.gestionedocumenti.commons.Ente
import org.apache.commons.lang.builder.HashCodeBuilder

import javax.persistence.CascadeType
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
@Table(name = "gdo_impostazioni")
class Impostazione {

    @GeneratedValue
    @Id
    @Column(name = "id_impostazione")
    Long id
    String caratteristiche

    @Column(nullable = false)
    String codice

    @Column(nullable = false)
    String descrizione

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ente")
    Ente ente

    String etichetta

    String predefinito

    @Column(nullable = false)
    String valore

    @Version
    Long version

    boolean equals(other) {
        if (!(other instanceof Impostazione)) {
            return false
        }

        return (other.codice == codice && other.ente.id == ente.id)
    }

    int hashCode() {
        return Objects.hash(ente?.id?:0, codice)
    }

    static namedQueries = {
        getImpostazione { String codiceImpostazione, Ente ente ->
            if (ente == null) {
                isNull("ente")
            } else {
                eq("ente", ente)
            }

            eq("codice", codiceImpostazione)
        }
    }
}