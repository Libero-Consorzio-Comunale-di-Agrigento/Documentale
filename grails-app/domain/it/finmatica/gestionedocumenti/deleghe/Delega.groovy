package it.finmatica.gestionedocumenti.deleghe

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente

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

@Entity
@Table(name = "SO4_V_DELEGHE")
@CompileStatic
class Delega {

    @GeneratedValue
    @Id
    @Column(name = "id_delega")
    Long id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    Date al

    @Column(name = "OTTICA", nullable = false)
    String codiceOttica

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    Date dal

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_aggiornamento", nullable = false)
    Date dataAggiornamento

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegante_utente")
    Ad4Utente deleganteUtente

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegato_utente")
    Ad4Utente delegatoUtente

    @Column(name = "istanza_applicativo", nullable = false)
    String istanzaApplicativo

    @Column(name = "modulo_applicativo", nullable = false)
    String moduloApplicativo

    @Column(name = "PROGR_UNITA_ORGANIZZATIVA", nullable = false)
    Long progressivoUnita

    @Column(name = "codice_competenza_delega", nullable = false)
    String tipologia

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_aggiornamento")
    Ad4Utente utenteAggiornamento
}