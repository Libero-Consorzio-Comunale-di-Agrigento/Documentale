package it.finmatica.gestionedocumenti.documenti

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.commons.AbstractDomain

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
@Table(name = "gdo_file_documento_firmatari")
@CompileStatic
class FileDocumentoFirmatario extends AbstractDomain {

    public static String CALCOLATO = 'CALCOLATO'
    public static String VERIFICATO = 'VERIFICATO'

    @GeneratedValue
    @Id
    @Column(name = "id_firmatario")
    Long id

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_firma")
    Date dataFirma

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "data_verifica")
    Date dataVerifica

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_file_documento")
    FileDocumento fileDocumento

    String nominativo

    @Column(nullable = false)
    int sequenza

    String stato

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utente_ad4")
    Ad4Utente utenteAd4

    @Version
    Long version
}