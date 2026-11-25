package it.finmatica.gestionedocumenti.storico

import it.finmatica.gestionedocumenti.commons.AbstractDomain
import it.finmatica.gestionedocumenti.documenti.Documento

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Lob
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_documenti_storico")
class DocumentoStorico extends AbstractDomain {

    public static final String TRASCODIFICATO = "TRASCODIFICATO"
    public static final String ERRORE = "ERRORE"
    public static final String VERSIONE_JSON2 = "JSON2"

    @GeneratedValue
    @Id
    @Column(name = "id_documento_storico")
    Long id

    @Lob
    @Column(name = "dati_modificati")
    String datiModificati

    @Lob
    @Column(name = "dati_storicizzati", nullable = false)
    String datiStoricizzati

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_documento")
    Documento documento

    @Column(nullable = false)
    long revisione

    @Column(name = "stato_trascodifica")
    String statoTrascodifica

    @Lob
    @Column(name = "errore_trascodifica")
    String erroreTrascodifica

    @Column(name = "versione_storico")
    String versioneStorico = VERSIONE_JSON2

    @Version
    Long version
}