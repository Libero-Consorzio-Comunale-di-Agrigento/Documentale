package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.gestionedocumenti.commons.AbstractDomainMultiEnte
import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OrderColumn
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Version

@Entity
@Table(name = "gdo_notifiche")
class Notifica extends AbstractDomainMultiEnte {

    public static final String MODALITA_EMAIL = "EMAIL"
    public static final String MODALITA_JWORKLIST = "JWORKLIST"

    @GeneratedValue
    @Id
    @Column(name = "id_notifica")
    Long id

    // elenco degli allegati della notifica
    String allegati

    @OrderColumn(name = "sequenza", nullable = false, updatable = false)
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "notifica", orphanRemoval = true)
    List<NotificaDestinatario> destinatari

    // come devo inviare la notifica?
    // possibilità di invio della notifica:
    // - email
    // - jworklist
    @Column(name = "modalita_invio", nullable = false)
    String modalitaInvio

    // elenco degli oggetti (separati da "," ) per cui questa notifica è disponibile (ad es LETTERA,PROTOCOLLO)
    String oggetti

    // testo dell'oggetto da inviare (può contenere i TAG da sostituire)
    @Column(nullable = false)
    String oggetto

    // testo da inviare (può contenere i TAG da sostituire)
    String testo

    @Column(name = "tipo_notifica", nullable = false)
    String tipoNotifica

    // tipologia della SmartDesktop utile per scegliere i bottoni in multiselezione
    @Column(name = "tipo_notifica_scrivania")
    String tipoNotificaScrivania

    // descrive la notifica
    @Column(nullable = false)
    String titolo

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valido_al")
    Date validoAl

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "valido_dal", nullable = false)
    Date validoDal

    @Version
    Long version

    Notifica addToDestinatari(NotificaDestinatario value) {
        if (this.destinatari == null) {
            this.destinatari = new ArrayList<NotificaDestinatario>()
        }

        NotificaDestinatario notificaDestinatario = null
        int index = 0
        for(NotificaDestinatario n : this.destinatari){
            if(value.id != null && n.id == value.id){
                notificaDestinatario = value
                break
            }
            index = index + 1
        }

        if(notificaDestinatario == null) {
            this.destinatari.add(value)
        } else {
            this.destinatari.set(index, value)
        }

        value.notifica = this
        return this
    }

    Notifica removeFromDestinatari(NotificaDestinatario value) {
        if (this.destinatari == null) {
            this.destinatari = new ArrayList<NotificaDestinatario>()
        }
        this.destinatari.remove((Object) value);
        value.notifica = null
        return this
    }

    @PrePersist
    void onPrePersist() {
        validoDal = validoDal ?: new Date()
        validoAl = valido ? null : (validoAl ?: new Date())
    }

    @PreUpdate
    void onPreUpdate() {
        validoAl = valido ? null : (validoAl ?: new Date())
    }

    List<String> getListaAllegati() {
        return allegati?.split(",") ?: []
    }

    List<String> getListaOggetti() {
        return oggetti?.split(",") ?: []
    }

    static namedQueries = {
        perTipo { String tipoNotifica, String oggetto = null ->
            eq ("tipoNotifica", tipoNotifica)
            eq ("valido", 		true)
            if (oggetto != null) {
                // che bruttura... ma è per evitare brutture ancora peggiori...
                sqlRestriction "','||oggetti||',' like '%,${oggetto},%'"
            }
        }
    }
}