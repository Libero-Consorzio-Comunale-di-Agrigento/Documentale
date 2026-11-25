package it.finmatica.gestionedocumenti.registri

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_tipi_registro")
@CompileStatic
class TipoRegistro extends AbstractDomainMultiEnte {

    @Id
    @Column(name = "tipo_registro")
    String codice

    // indica se il registro annuale va chiuso automaticamente al rinnovo rinnovoAutomatico del nuovo anno.
    @Type(type = "yes_no")
    @Column(name = "chiusura_automatica", nullable = false)
    boolean chiusuraAutomatica

    String commento

    // codice da usare per integrazione (es contabilità)
    @Column(name = "registro_esterno")
    String registroEsterno

    // indica se deve rinnovarsi automaticamente ogni anno
    @Type(type = "yes_no")
    @Column(name = "rinnovo_automatico", nullable = false)
    boolean rinnovoAutomatico

    @Type(type = "yes_no")
    @Column(nullable = false)
    boolean emergenza

    @Type(type = "yes_no")
    @Column(name = "registro_automatico")
    boolean registroAutomatico

    @Column(name = "id_societa")
    Long idSocieta

    @Type(type = "yes_no")
    @Column(name = "numerazione_illimitata", nullable = false)
    boolean numerazioneIllimitata

    @Type(type = "yes_no")
    @Column(name = "protocollo_generale")
    boolean protocolloGenerale

    @Version
    Long version

    String getId() {
        return codice
    }

    void setId(String codice) {
        this.codice = codice
    }
}