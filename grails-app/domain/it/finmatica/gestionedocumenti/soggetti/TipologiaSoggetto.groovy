package it.finmatica.gestionedocumenti.soggetti

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_tipologie_soggetto")
@CompileStatic
class TipologiaSoggetto extends AbstractDomainMultiEnte {

    @GeneratedValue
    @Id
    @Column(name = "id_tipologia_soggetto")
    Long id

    String commento

    String descrizione

    @Column(name = "layout_soggetti", nullable = false)
    String layoutSoggetti

    @OrderColumn(name = "sequenza", nullable = false, updatable = false)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "tipologiaSoggetto", orphanRemoval = true)
    List<TipologiaSoggettoRegola> regole

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_oggetto")
    WkfTipoOggetto tipoOggetto

    @Version
    Long version

    TipologiaSoggetto addToRegole(TipologiaSoggettoRegola value) {
        if (this.regole == null) {
            this.regole = new ArrayList<TipologiaSoggettoRegola>()
        }

        TipologiaSoggettoRegola regola = null
        int index = 0
        for (TipologiaSoggettoRegola r : this.regole) {
            if (value.id != null && r.id == value.id) {
                regola = value
                break
            }
            index = index + 1
        }
        if (regola == null) {
            this.regole.add(value)
        } else {
            this.regole.set(index, value)
        }
        value.tipologiaSoggetto = this
        return this
    }

    TipologiaSoggetto removeFromRegole(TipologiaSoggettoRegola value) {
        if (this.regole == null) {
            this.regole = new ArrayList<TipologiaSoggettoRegola>()
        }
        this.regole.remove((Object) value);
        value.tipologiaSoggetto = null
        return this
    }
}