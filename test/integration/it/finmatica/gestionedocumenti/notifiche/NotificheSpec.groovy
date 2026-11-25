package it.finmatica.gestionedocumenti.notifiche

import it.finmatica.gestionedocumenti.notifiche.calcolo.NotificaSoggetto
import it.finmatica.gestionedocumenti.notifiche.calcolo.RegolaCalcoloNotifica
import it.finmatica.gestionedocumenti.notifiche.calcolo.RepositoryRegoleCalcolo
import it.finmatica.gestionedocumenti.notifiche.calcolo.TipoNotifica
import it.finmatica.gestionedocumenti.testutils.TestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

/**
 * Created by esasdelli on 06/04/2017.
 */
@SpringBootTest
@Import(TestConfiguration)
class NotificheSpec extends Specification {

    @Autowired
    RepositoryRegoleCalcolo repositoryRegoleCalcolo

    @Autowired
    PlatformTransactionManager transactionManager

    @Autowired
    NotificheService notificheService

    @Autowired
    NotificheDispatcher notificheDispatcher

    void "invio notifica dopo la commit"() {
        setup:
        TransactionTemplate requiresNewTransactionTemplate = new TransactionTemplate(transactionManager, new DefaultTransactionDefinition(Propagation.REQUIRES_NEW.value()))

        when:
        requiresNewTransactionTemplate.execute { TransactionStatus status ->
            TestUtils.setUpEnte()
            TestUtils.login()
            notificheDispatcher.invia(new Notifica(id: 5), [doc: "test"], [new NotificaSoggetto(email: "e.sasdelli@ads.it")], "oggetto", "testo", "messaggioTODO", false)
        }
        then:
        true
    }

    @TestConfiguration
    static class NotificheConfig {

        @Bean
        RepositoryRegoleCalcolo repositoryRegoleCalcolo() {
            return new RepositoryRegoleCalcolo() {
                @Override
                String getTipoDocumento(Object documento) {
                    return "TIPO_DOCUMENTO"
                }

                @Override
                String getIdRiferimento(Object documento) {
                    return "ID_RIF_1"
                }

                @Override
                Object getDocumento(String idRiferimento) {
                    return [doc: "test"]
                }

                @Override
                List<TipoNotifica> getListaTipiNotifica() {
                    return null
                }

                @Override
                List<RegolaCalcoloNotifica> getRegoleCalcoloTag() {
                    return null
                }

                @Override
                List<RegolaCalcoloNotifica> getRegoleCalcoloDestinatari() {
                    return null
                }

                @Override
                List<RegolaCalcoloNotifica> getRegoleCalcoloAllegati() {
                    return null
                }

                @Override
                String calcolaTag(String codiceRegolaCalcolo, Object documento) {
                    return null
                }

                @Override
                List<NotificaSoggetto> calcolaDestinatari(String codiceRegolaCalcolo, Object documento, NotificaDestinatario notificaDestinatario) {
                    return null
                }

                @Override
                List<Allegato> calcolaAllegati(String codiceRegolaCalcolo, Object documento) {
                    return null
                }
            }
        }
    }
}
