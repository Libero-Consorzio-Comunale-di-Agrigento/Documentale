package it.finmatica.gestionedocumenti.soggetti

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.gestionedocumenti.RegolaCalcolo
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte

import javax.persistence.AttributeOverride
import javax.persistence.AttributeOverrides
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_tipologie_soggetto_regole")
@CompileStatic
class TipologiaSoggettoRegola extends AbstractDomainMultiEnte {

    @GeneratedValue
    @Id
    @Column(name = "id_tipologia_soggetto_regola")
    Long id

    @Embedded
    @AttributeOverrides([@AttributeOverride(name = "nomeBean", column = @Column(name = "regola_default_nome_bean")),
            @AttributeOverride(name = "nomeMetodo", column = @Column(name = "regola_default_nome_metodo"))])
    RegolaCalcolo regolaDefault

    @Embedded
    @AttributeOverrides([@AttributeOverride(name = "nomeBean", column = @Column(name = "regola_lista_nome_bean")),
            @AttributeOverride(name = "nomeMetodo", column = @Column(name = "regola_lista_nome_metodo"))])
    RegolaCalcolo regolaLista

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruolo")
    Ad4Ruolo ruolo

    @Column(nullable = false)
    int sequenza

    @Column(name = "tipo_soggetto", nullable = false)
    String tipoSoggetto

    @Column(name = "tipo_soggetto_partenza")
    String tipoSoggettoPartenza

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipologia_soggetto")
    TipologiaSoggetto tipologiaSoggetto

    @Version
    Long version
}