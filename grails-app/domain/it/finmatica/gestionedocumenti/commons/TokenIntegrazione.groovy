package it.finmatica.gestionedocumenti.commons

import groovy.transform.CompileStatic
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.security.SpringSecurityService
import it.finmatica.gestionedocumenti.Holders
import it.finmatica.so4.login.So4SpringSecurityService
import org.springframework.beans.factory.annotation.Autowired

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(name = "gdo_token_integrazioni")
@CompileStatic
class TokenIntegrazione extends AbstractDomain {

    public static final transient String TIPO_PROTOCOLLO = "PROTOCOLLO"
    public static final transient String TIPO_LOCK_DOCUMENTO = "LOCK_DOCUMENTO"
    public static final transient String TIPO_ALBO = "ALBO"

    public static final transient String STATO_IN_CORSO = "IN_CORSO"
    public static final transient String STATO_SUCCESSO = "SUCCESSO"
    public static final transient String STATO_ERRORE = "ERRORE"

    @GeneratedValue
    @Id
    @Column(name = "id_token")
    Long id
    @Version
    Long version
    @Column(name = "id_riferimento", nullable = false)
    String idRiferimento

    String dati

    @Column(nullable = false)
    String stato

    @Column(nullable = false)
    String tipo

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeInsert () {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeUpdate () {
        utenteUpd = springSecurityService.currentUser
    }


    boolean isStatoSuccesso() {
        return (stato == STATO_SUCCESSO)
    }

    boolean isStatoErrore() {
        return (stato == STATO_ERRORE)
    }

    boolean isStatoInCorso() {
        return (stato == STATO_IN_CORSO)
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService") as SpringSecurityService
    }
}