package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.annotations.LazyToOne
import org.hibernate.annotations.LazyToOneOption

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
@Table(name = "gdo_notifiche_attivita")
class NotificaAttivitaJWorklist {

    @GeneratedValue
    @Id
    @Column(name = "id_notifica_attivita")
    Long id

    // id di riferimento per l'attività di jworklist
    @Column(name = "id_attivita_jworklist", nullable = false)
    String idAttivitaJWorklist

    // id che identifica il documento / oggetto per cui è stata inviata la notifica
    @Column(name = "id_riferimento", nullable = false)
    String idRiferimento

    @Column(name = "tipo_notifica", nullable = false)
    String tipoNotifica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns([@JoinColumn(name = "unita_progr", referencedColumnName = "progr"),
            @JoinColumn(name = "unita_dal", referencedColumnName = "dal"),
            @JoinColumn(name = "unita_ottica", referencedColumnName = "ottica")])
    So4UnitaPubb unitaSo4

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_ad4")
    Ad4Utente utente

    @Version
    Long version

    static namedQueries = {
        perIdRiferimento { String idRiferimento, String tipoNotifica = null, Ad4Utente utente = null ->

            eq("idRiferimento", idRiferimento)

            if (tipoNotifica != null) {
                eq("tipoNotifica", tipoNotifica)
            }

            if (utente != null) {
                eq("utente", utente)
            }
        }
    }
}