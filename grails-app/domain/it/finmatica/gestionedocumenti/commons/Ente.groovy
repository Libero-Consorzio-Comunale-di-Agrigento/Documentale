package it.finmatica.gestionedocumenti.commons

import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.struttura.So4Ottica
import org.hibernate.annotations.Type

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
@Table(name = "gdo_enti")
class Ente implements it.finmatica.multiente.Ente {

    @GeneratedValue
    @Id
    @Column(name = "id_ente")
    Long id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amministrazione")
    So4Amministrazione amministrazione

    String aoo

    @Column(nullable = false)
    String descrizione

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ottica")
    So4Ottica ottica

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean interno

    @Column(nullable = false)
    int sequenza

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean valido

    @Version
    Long version

    static namedQueries = {
        perAmministrazioneOtticaAoo { String codiceAmm, String codiceOttica = null, String codiceAoo = null ->
            eq("valido", true)

            eq("amministrazione.codice", codiceAmm)

            if (codiceOttica != null) {
                eq("ottica.codice", codiceOttica)
            }

            if (codiceAoo != null) {
                eq("aoo", codiceAoo)
            }
        }
    }
}