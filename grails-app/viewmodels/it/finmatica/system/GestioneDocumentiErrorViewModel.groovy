package it.finmatica.system

import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

import groovy.util.logging.Slf4j
import it.finmatica.gestionedocumenti.exception.GestioneDocumentiRuntimeException
import it.finmatica.gestionedocumenti.zkutils.SuccessHandler
import it.finmatica.gestioneiter.exceptions.IterRuntimeException
import org.hibernate.StaleObjectStateException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.orm.hibernate5.HibernateJdbcException
import org.zkoss.bind.BindContext
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.UiException
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

@Slf4j
@VariableResolver(DelegatingVariableResolver)
class GestioneDocumentiErrorViewModel {

    @WireVariable
    private SuccessHandler successHandler

    // component
    def self

    // eccezione
    def title
    def stacktrace

    // mostrare dettagli
    def dettagli = false

    // auto closable
    def autoClosable = false
    def autoTime = 2500

    @Init
    init(@ContextParam(ContextType.COMPONENT) Window w) {
        self = w
        successHandler.clearMessages()
        Throwable exception = Executions.getCurrent().getAttribute("javax.servlet.error.exception")
        log.error(exception.message, exception)

        def cause = (exception instanceof UiException ? exception.cause : null)
        if (cause != null) {
            checkException(cause)
        } else {
            checkException(exception)
        }
    }

    private void checkException(Throwable e) {
        if (e instanceof GestioneDocumentiRuntimeException || e instanceof IterRuntimeException) {
            title = e.message
            autoClosable = true
        } else if (e instanceof DataIntegrityViolationException) {
            title = "Oggetto non modificabile o eliminabile: esistono dipendenze."
        } else if (e instanceof StaleObjectStateException) {
            title = "Oggetto modificato da un altro utente."
        } else if (e instanceof HibernateJdbcException) {
            title = e.getSQLException().message
        } else if (e.cause?.cause != null) {
            title = e.cause?.cause?.message
        } else if (e.cause != null) {
            title = "Errore generico:\n\n " + e.cause.message?.toString()?.split('\n')[0]
        } else {
            String[] strings = e.message?.toString()?.split('\n')
            if (strings?.size() > 0) {
                title = "Errore generico:\n\n " + strings[0]
            } else {
                title = "Errore generico:\n\n " + e.stackTrace[0]?.toString()
            }
        }

        stacktrace = e.message + "\n" + e.getStackTrace().toString().replace(')', ')\n')
    }

    @Command
    onClose(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        dettagli = false
        Events.postEvent("onClose", self, null)
    }

    @Command
    checkClose(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        if (autoClosable)
            Events.postEvent("onClose", self, null)
    }
}
