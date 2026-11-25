package it.finmatica.gestionedocumenti.multiente

import it.finmatica.so4.login.So4SpringSecurityService
import org.hibernate.SessionFactory
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.transaction.annotation.Transactional

class GestioneDocumentiSpringSecurityService extends So4SpringSecurityService {
    GestioneDocumentiSpringSecurityService(GestioneDocumentiUserDetailService userDetailsService) {
        super(userDetailsService)
    }

    @Override
    GestioneDocumentiUserDetails getPrincipal() {
        return (GestioneDocumentiUserDetails) super.getPrincipal()
    }
}
