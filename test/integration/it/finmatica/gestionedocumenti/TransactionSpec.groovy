package it.finmatica.gestionedocumenti

import groovy.sql.Sql
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.transaction.CustomChainedTransactionManager
import org.springframework.test.annotation.DirtiesContext
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import spock.lang.Specification

import javax.sql.DataSource

@DirtiesContext
@SpringBootTest
class TransactionSpec extends Specification {

    @Autowired
    DataSource dataSource

    @Autowired
    @Qualifier("dataSource_gdm")
    DataSource dataSource_gdm

    @Autowired
    @Qualifier("unproxiedDataSource")
    DataSource dataSourceUnproxied

    @Autowired
    @Qualifier("unproxiedGdmDataSource")
    DataSource dataSourceUnproxied_gdm

    @Autowired
    PlatformTransactionManager transactionManager

    void "chainedTransactionManager configurato correttamente"() {
        expect:
        transactionManager != null
        transactionManager instanceof CustomChainedTransactionManager
    }

    void cleanup() {
        String descrizioneEnte = "Provincia di Modena"
        String descrizioneTipoSpedizione = "SPEDIZIONE BC ESTERO CON RENDICONTO"
        new Sql(dataSourceUnproxied).executeUpdate("UPDATE GDO_ENTI SET DESCRIZIONE = $descrizioneEnte WHERE ID_ENTE = 1")
        new Sql(dataSourceUnproxied_gdm).executeUpdate("UPDATE SEG_TIPI_SPEDIZIONE SET DESCRIZIONE = $descrizioneTipoSpedizione WHERE TIPO_SPEDIZIONE = 'BERE'")
    }

    void "test after commit dopo tutte le commit"() {
        setup:
        int afterCommitCount = 0
        boolean afterCommit = false
        String descrizioneEnteAfterCommit = "Provincia di Modena"
        String descrizioneTipoSpedizioneAfterCommit = "Descrizione 2"

        when:
        TransactionTemplate template = new TransactionTemplate(transactionManager)
        template.execute({
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                @Override
                void afterCommit() {
                    afterCommitCount++
                    afterCommit = true
                    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, new DefaultTransactionDefinition(Propagation.REQUIRES_NEW.value()))
                    transactionTemplate.execute {
                        descrizioneEnteAfterCommit = new Sql(dataSource).rows("select DESCRIZIONE from gdo_enti WHERE ID_ENTE = 1")[0].DESCRIZIONE
                        descrizioneTipoSpedizioneAfterCommit = new Sql(dataSource_gdm).rows("select DESCRIZIONE from SEG_TIPI_SPEDIZIONE WHERE TIPO_SPEDIZIONE = 'BERE'")[0].DESCRIZIONE
                    }
                }
            })
            new Sql(dataSource).executeUpdate("UPDATE GDO_ENTI SET DESCRIZIONE = 'test' WHERE ID_ENTE = 1")
            new Sql(dataSource_gdm).executeUpdate("UPDATE SEG_TIPI_SPEDIZIONE SET DESCRIZIONE = 'test' WHERE TIPO_SPEDIZIONE = 'BERE'")
        })

        then:
        afterCommitCount == 1
        afterCommit == true
        descrizioneEnteAfterCommit == "test"
        descrizioneTipoSpedizioneAfterCommit == "test"
    }
}
