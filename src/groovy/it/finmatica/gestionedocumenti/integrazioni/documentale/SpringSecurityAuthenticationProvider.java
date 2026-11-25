package it.finmatica.gestionedocumenti.integrazioni.documentale;

import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService;
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiUserDetails;
import it.finmatica.smartdoc.api.login.AuthenticationProvider;
import it.finmatica.smartdoc.api.login.Utente;

class SpringSecurityAuthenticationProvider implements AuthenticationProvider {

    final private GestioneDocumentiSpringSecurityService springSecurityService;

    SpringSecurityAuthenticationProvider(GestioneDocumentiSpringSecurityService springSecurityService) {
        this.springSecurityService = springSecurityService;
    }

    @Override
    public Utente getUtente() {
        // accedo all'utente direttamente dal principal così non scatta una nuova transazione.
        // utile soprattutto per lo storico envers (una transaction fa fallire la PostUpdateEventListener)
        GestioneDocumentiUserDetails userDetails = springSecurityService.getPrincipal();
        return new Utente(userDetails.getId());
    }

    @Override
    public void login(Utente utente) {
    }
}
