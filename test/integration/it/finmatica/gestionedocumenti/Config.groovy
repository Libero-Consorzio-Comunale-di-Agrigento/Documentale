package it.finmatica.gestionedocumenti

import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.transaction.CustomChainedTransactionManager
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager

import javax.persistence.EntityManagerFactory
import javax.sql.DataSource
import java.sql.SQLException

@SpringBootApplication
class Config {
    @Primary
    @Bean("dataSource")
    DataSource dataSourceSingleConnection(@Qualifier("unproxiedDataSource") DataSource dataSource)
            throws SQLException {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean("ad4DataSource")
    DataSource ad4DataSourceSingleConnection(@Qualifier("unproxiedAd4DataSource") DataSource dataSource)
            throws SQLException {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean("dataSource_gdm")
    DataSource dataSource_gdm(@Qualifier("unproxiedGdmDataSource") DataSource dataSource)
            throws SQLException {
        return new TransactionAwareDataSourceProxy(dataSource);
    }

    @Bean("unproxiedDataSource")
    @ConfigurationProperties("spring.datasource")
    DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("unproxiedAd4DataSource")
    @ConfigurationProperties("spring.datasources.ad4")
    DataSource unproxiedAd4DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean("unproxiedGdmDataSource")
    @ConfigurationProperties("spring.datasources.gdm")
    DataSource unproxiedGdmDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    PlatformTransactionManager transactionManager(JpaTransactionManager jpaTransactionManager, @Qualifier("gdmTransactionManager") PlatformTransactionManager gdmTransactionManager) {
        return new CustomChainedTransactionManager(gdmTransactionManager, jpaTransactionManager)
    }

    @Bean("gdmTransactionManager")
    PlatformTransactionManager gdmTransactionManager(@Qualifier("unproxiedGdmDataSource") DataSource dataSourceGdm) {
        return new DataSourceTransactionManager(dataSourceGdm)
    }

    @Bean
    JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory)
    }

    @Bean
    SessionFactory sessionFactory(EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.unwrap(SessionFactory)
    }
}
