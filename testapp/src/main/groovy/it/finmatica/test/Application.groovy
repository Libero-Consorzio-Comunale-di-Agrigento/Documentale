package it.finmatica.test

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import javax.sql.DataSource

@SpringBootApplication
class Application {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args)
    }

    @Bean(name = "dataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    DataSource dataSource() {
        return DataSourceBuilder.create().build()
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasources.ad4")
    DataSource ad4DataSource() {
        return DataSourceBuilder.create().build()
    }

    @Bean(name = "dataSourceGdmInternal")
    @ConfigurationProperties(prefix = "spring.datasources.gdm")
    @Qualifier('gdmDataSourceInternal')
    DataSource dataSourceGdmInternal() {
        return DataSourceBuilder.create().build()
    }

    @Bean(name = "dataSourceGdm")
    @Qualifier('gdmDataSource')
    DataSource dataSourceGdm() {
        return new TransactionAwareDataSourceProxy(dataSourceGdmInternal())
    }
}
