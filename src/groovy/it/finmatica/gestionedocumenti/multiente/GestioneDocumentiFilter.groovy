package it.finmatica.gestionedocumenti.multiente

import groovy.transform.CompileStatic
import it.finmatica.hibernatefilter.resolvablefilter.FilterParamResolver
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

@CompileStatic
class GestioneDocumentiFilter implements FilterParamResolver {

    public static final String FILTER_NAME = "GestioneDocumentiFilter"

    @Override
    Object resolveParam() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication()
        if (authentication == null || !authentication.isAuthenticated()) {
            return null
        }

        GestioneDocumentiUserDetails userDetails = (GestioneDocumentiUserDetails) authentication.getPrincipal()
        return userDetails.getIdEnte()
    }
}
