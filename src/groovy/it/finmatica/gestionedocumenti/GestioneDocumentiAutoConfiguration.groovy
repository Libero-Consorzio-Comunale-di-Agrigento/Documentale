package it.finmatica.gestionedocumenti


import it.finmatica.firmadigitale.async.FirmaDigitaleProgressStatusHolder
import it.finmatica.gestionedocumenti.commons.FallbackScope
import it.finmatica.gestionedocumenti.documenti.beans.FileDownloader
import it.finmatica.gestionedocumenti.documenti.beans.GestoreTransazioneFirma
import it.finmatica.gestionedocumenti.impostazioni.ImpostazioniMap
import it.finmatica.gestionedocumenti.integrazioni.documentale.IntegrazioneGdmProperties
import it.finmatica.gestionedocumenti.integrazioni.documentale.SpringSecurityAuthenticationProvider
import it.finmatica.gestionedocumenti.integrazioni.firma.FineFirmaCallback
import it.finmatica.gestionedocumenti.integrazioni.firma.GestioneDocumentiFirmaService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiMultiEnteService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiSpringSecurityService
import it.finmatica.gestionedocumenti.multiente.GestioneDocumentiUserDetailService
import it.finmatica.gestionedocumenti.notifiche.NotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import it.finmatica.gestionedocumenti.notifiche.calcolo.annotated.AnnotatedRegolaCalcolo
import it.finmatica.gestionedocumenti.notifiche.calcolo.annotated.AnnotatedRepositoryRegoleCalcolo
import it.finmatica.gestionedocumenti.notifiche.dispatcher.NotificheProxy
import it.finmatica.gestionedocumenti.notifiche.dispatcher.aftercommit.AfterCommitNotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.dispatcher.aftercommit.AfterCommitNotificheDispatcherDelegator
import it.finmatica.gestionedocumenti.notifiche.dispatcher.aftercommit.AsyncNotificheDispatcherDelegator
import it.finmatica.gestionedocumenti.notifiche.dispatcher.email.EmailNotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist.DefaultNotificaJWorklistBuilder
import it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist.JWorklistNotificheDispatcher
import it.finmatica.gestionedocumenti.notifiche.dispatcher.jworklist.NotificaJWorklistBuilder
import it.finmatica.gestionedocumenti.notifiche.iter.RepositoryRegoleCalcoloIter
import it.finmatica.gestionedocumenti.zkutils.SuccessHandler
import it.finmatica.smartdoc.api.DocumentaleService
import it.finmatica.smartdoc.api.login.AuthenticationProvider
import it.finmatica.smartdoc.integrazioni.gdm.DocumentaleGdm
import it.finmatica.so4.So4AutoConfiguration
import it.finmatica.so4.login.So4SpringSecurityService
import it.finmatica.so4.strutturaPubblicazione.So4AmministrazionePubbService
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.web.context.request.RequestScope

import javax.sql.DataSource

// TODO[SPRINGBOOT] i bean qui dichiarati dovrebbero essere "sovrascrivibili" da chi usa questo plugin
@ComponentScan
@Configuration
@AutoConfigurationPackage
@AutoConfigureBefore(So4AutoConfiguration)
class GestioneDocumentiAutoConfiguration {

    @Bean
    RequestScope requestScope() {
        return new RequestScope()
    }

    @Bean
    FallbackScope fallbackScope(RequestScope requestScope) {
        return new FallbackScope(requestScope)
    }

    @Bean
    CustomScopeConfigurer fallbackScopeConfigurer(FallbackScope fallbackScope) {
        CustomScopeConfigurer customScopeConfigurer = new CustomScopeConfigurer()
        customScopeConfigurer.addScope(FallbackScope.DEFAULT_NAME, fallbackScope)
        return customScopeConfigurer
    }

    @Bean
    Holders gestioneDocumentiHolders() {
        return new Holders()
    }

    @Bean
    GestioneDocumentiUserDetailService userDetailService(So4AmministrazionePubbService so4AmministrazionePubbService, SessionFactory sessionFactory) {
        return new GestioneDocumentiUserDetailService(so4AmministrazionePubbService, sessionFactory)
    }

    @Bean
    GestioneDocumentiSpringSecurityService springSecurityService(GestioneDocumentiUserDetailService gestioneDocumentiUserDetailService) {
        return new GestioneDocumentiSpringSecurityService(gestioneDocumentiUserDetailService)
    }

    @Bean
    GestioneDocumentiMultiEnteService multiEnteService(GestioneDocumentiSpringSecurityService springSecurityService) {
        return new GestioneDocumentiMultiEnteService(springSecurityService)
    }

    @Bean
    @ConditionalOnMissingBean(FileDownloader)
    FileDownloader fileDownloader() {
        return new FileDownloader()
    }

    @Bean
    FineFirmaCallback fineFirmaCallback(GestioneDocumentiFirmaService gestioneDocumentiFirmaService, SuccessHandler successHandler, FirmaDigitaleProgressStatusHolder firmaDigitaleProgressStatusHolder) {
        return new FineFirmaCallback(gestioneDocumentiFirmaService, successHandler, firmaDigitaleProgressStatusHolder)
    }

    @Bean
    GestoreTransazioneFirma gestoreTransazioneFirma() {
        return new GestoreTransazioneFirma()
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationProvider)
    AuthenticationProvider authenticationProvider(GestioneDocumentiSpringSecurityService springSecurityService) {
        return new SpringSecurityAuthenticationProvider(springSecurityService)
    }

    @Bean
    @ConditionalOnMissingBean(DocumentaleService)
    DocumentaleService documentaleService(@Qualifier("dataSource_gdm") DataSource dataSource_gdm, GestioneDocumentiSpringSecurityService springSecurityService) {
        AuthenticationProvider authenticationProvider = new SpringSecurityAuthenticationProvider(springSecurityService)
        return new DocumentaleGdm(dataSource_gdm, new IntegrazioneGdmProperties(), authenticationProvider)
    }

    @Bean
    @ConditionalOnMissingBean(ImpostazioniMap)
    ImpostazioniMap impostazioniMap() {
        return new ImpostazioniMap()
    }

    @Bean
    @ConditionalOnMissingBean(NotificaJWorklistBuilder)
    NotificaJWorklistBuilder notificaJWorklistBuilder() {
        return new DefaultNotificaJWorklistBuilder()
    }

    @Bean
    @ConditionalOnMissingBean(RepositoryRegoleCalcolo)
    RepositoryRegoleCalcolo repositoryRegoleCalcolo() {
        return new AnnotatedRepositoryRegoleCalcolo()
    }

    @Bean
    AnnotatedRegolaCalcolo repositoryRegoleCalcoloIter() {
        return new RepositoryRegoleCalcoloIter()
    }

    @Bean
    @Scope(scopeName = FallbackScope.DEFAULT_NAME, proxyMode = ScopedProxyMode.TARGET_CLASS)
    SuccessHandler successHandler() {
        return new SuccessHandler()
    }

    @Bean
    JWorklistNotificheDispatcher jWorklistNotificheDispatcher() {
        return new JWorklistNotificheDispatcher()
    }

    @Bean
    EmailNotificheDispatcher emailNotificheDispatcher() {
        return new EmailNotificheDispatcher()
    }

    @Bean
    NotificheProxy notificheProxy(EmailNotificheDispatcher emailNotificheDispatcher, JWorklistNotificheDispatcher jWorklistNotificheDispatcher) {
        return new NotificheProxy(emailNotificheDispatcher, jWorklistNotificheDispatcher)
    }

    @Bean
    AsyncNotificheDispatcherDelegator asyncNotificheDispatcherDelegator(AfterCommitNotificheDispatcherDelegator afterCommitNotificheDispatcherDelegator) {
        return new AsyncNotificheDispatcherDelegator(afterCommitNotificheDispatcherDelegator)
    }

    @Bean
    AfterCommitNotificheDispatcherDelegator afterCommitNotificheDispatcherDelegator(RepositoryRegoleCalcolo repositoryRegoleCalcolo, NotificheProxy notificheProxy) {
        return new AfterCommitNotificheDispatcherDelegator(repositoryRegoleCalcolo, notificheProxy)
    }

    @Bean
    @Primary
    NotificheDispatcher notificheDispatcher(AfterCommitNotificheDispatcherDelegator afterCommitNotificheDispatcherDelegator
                                            , AsyncNotificheDispatcherDelegator asyncNotificheDispatcherDelegator
                                            , RepositoryRegoleCalcolo repositoryRegoleCalcolo
                                            , So4SpringSecurityService springSecurityService) {
        return new AfterCommitNotificheDispatcher(afterCommitNotificheDispatcherDelegator, asyncNotificheDispatcherDelegator, repositoryRegoleCalcolo, springSecurityService)
    }
}
