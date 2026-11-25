package it.finmatica.gestionedocumenti.multiente

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestionedocumenti.commons.Ente
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.impostazioni.Impostazioni
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.So4UserDetailService
import it.finmatica.so4.login.detail.Amministrazione
import it.finmatica.so4.login.detail.Ottica
import it.finmatica.so4.struttura.So4Ottica
import it.finmatica.so4.strutturaPubblicazione.So4AmministrazionePubbService
import org.hibernate.SessionFactory
import org.springframework.security.core.GrantedAuthority
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
class GestioneDocumentiUserDetailService extends So4UserDetailService {
    GestioneDocumentiUserDetailService(So4AmministrazionePubbService so4AmministrazionePubbService, SessionFactory sessionFactory) {
        super(so4AmministrazionePubbService, sessionFactory)
    }

    @Override
    protected GestioneDocumentiUserDetails createUserDetails(Ad4Utente user, Collection<GrantedAuthority> authorities) {
        So4UserDetail userDetail = super.createUserDetails(user, authorities)

        // se l'utente collegato non ha amministrazioni associate, allora è un utente di sistema e devo caricargliele "a mano"
        if (userDetail.amministrazioni.size() == 0) {
            return new GestioneDocumentiUserDetails(userDetail.utente, userDetail.authorities, caricaAmministrazioniGestite())
        }
        return new GestioneDocumentiUserDetails(userDetail)
    }

    private List<Amministrazione> caricaAmministrazioniGestite() {
        // ottengo i codici degli enti gestiti dall'applicativo
        String[] codiciEntiSo4 = Impostazioni.ENTI_SO4.valori

        if (codiciEntiSo4 == null || codiciEntiSo4.size() == 0) {
            ArrayList<String> codiciEnti = new ArrayList<>()
            for (Ente e : Ente.findAllByValidoAndInterno(true, true)) {
                codiciEnti.add(e.amministrazione?.codice)
            }
            codiciEntiSo4 = codiciEnti.toArray()
        }

        List<So4Ottica> ottiche = So4Ottica.createCriteria().list {
            'in'("amministrazione.codice", codiciEntiSo4)
        }

        List<Amministrazione> amministrazioni = []
        for (So4Ottica ottica : ottiche) {
            Amministrazione amministrazioneOttica = amministrazioni.find { it.codice == ottica.amministrazione.codice }

            if (amministrazioneOttica == null) {
                amministrazioneOttica = new Amministrazione(codice: ottica.amministrazione.codice, descrizione: ottica.amministrazione.soggetto.cognome, ottiche: [])
                amministrazioni << amministrazioneOttica
            }

            if (amministrazioneOttica.ottica(ottica.codice) == null) {
                amministrazioneOttica.ottiche << new Ottica(codice: ottica.codice, descrizione: ottica.descrizione, unitaOrganizzative: [])
            }
        }

        return amministrazioni
    }
}
