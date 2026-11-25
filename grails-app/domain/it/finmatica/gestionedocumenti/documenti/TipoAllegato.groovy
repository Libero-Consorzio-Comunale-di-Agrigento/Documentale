package it.finmatica.gestionedocumenti.documenti

import groovy.transform.CompileStatic
import it.finmatica.gestionedocumenti.documenti.TipoDocumento
import org.hibernate.annotations.Type

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "gdo_tipi_allegato")
@CompileStatic
class TipoAllegato extends TipoDocumento {

    // questi sono i codici relativi al tipo-documento per gli allegati.
    public static final String CODICE_TIPO_ALLEGATO = "ALLEGATO"
    public static final String CODICE_TIPO_STAMPA_UNICA = "STAMPA_UNICA"

    public static final String ACRONIMO_DEFAULT = "0000"

    @Type(type = "yes_no")
    @Column(name = "stampa_unica", nullable = false)
    boolean stampaUnica

    TipoAllegato addToModelliAssociati(TipoDocumentoModello value) {
        if (this.modelliAssociati == null) {
            this.modelliAssociati = new HashSet<TipoDocumentoModello>()
        }
        this.modelliAssociati.add(value);
        value.tipoDocumento = this
        return this
    }

    TipoAllegato removeFromModelliAssociati(TipoDocumentoModello value) {
        if (this.modelliAssociati == null) {
            this.modelliAssociati = new HashSet<TipoDocumentoModello>()
        }
        this.modelliAssociati.remove((Object) value);
        value.tipoDocumento = null
        return this
    }
}