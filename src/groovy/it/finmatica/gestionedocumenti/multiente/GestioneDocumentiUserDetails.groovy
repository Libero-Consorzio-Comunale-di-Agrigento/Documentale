package it.finmatica.gestionedocumenti.multiente

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.Amministrazione
import org.springframework.security.core.GrantedAuthority

class GestioneDocumentiUserDetails extends So4UserDetail {

    // idEnte è un "Long" perché in questo modo non verrà abilitato il filtro sull'ente se l'ente non è ancora stato impostato
    private Long idEnte = null

    GestioneDocumentiUserDetails(So4UserDetail userDetail) {
        super(userDetail.getUtente(), userDetail.getAuthorities(), userDetail.getAmministrazioni())
    }

    GestioneDocumentiUserDetails(Ad4Utente utente, Collection<GrantedAuthority> authorities, List<Amministrazione> amministrazioni) {
        super(utente, authorities, amministrazioni)
    }

    Long getIdEnte() {
        return idEnte
    }

    void setIdEnte(long idEnte) {
        this.idEnte = idEnte
        Ente e = Ente.get(ente.id)
        if (e.ottica) {
            setOtticaCorrente(e.ottica)
        }
    }

    Ente getEnte() {
        return Ente.get(this.idEnte)
    }
}
