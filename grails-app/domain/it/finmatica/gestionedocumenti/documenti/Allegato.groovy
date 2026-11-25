package it.finmatica.gestionedocumenti.documenti


import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "gdo_allegati")
class Allegato extends Documento {

    public final static String CODICE_TIPO_COLLEGAMENTO = "ALLEGATO"

    String commento

    String descrizione

    @Column(name = "num_pagine")
    Integer numPagine

    String origine

    Integer quantita

    @Column(nullable = false)
    int sequenza

    @Type(type = "yes_no")
    @Column(name = "stampa_unica", nullable = false)
    boolean stampaUnica = false

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_allegato")
    TipoAllegato tipoAllegato

    String ubicazione

    DocumentoCollegato getCollegamentoDocumentoPrincipale() {
        return DocumentoCollegato.findByCollegatoAndTipoCollegamentoAndValido(this, TipoCollegamento.findByCodice(CODICE_TIPO_COLLEGAMENTO), true)
    }

    Documento getDocumentoPrincipale() {
        return getCollegamentoDocumentoPrincipale()?.documento
    }

    String getApplicativo() {
        return documentoPrincipale?.applicativo
    }

    String getEstremi() {
        return documentoPrincipale?.estremi
    }
}