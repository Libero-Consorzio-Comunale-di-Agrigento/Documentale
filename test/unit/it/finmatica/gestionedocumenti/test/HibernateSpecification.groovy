package it.finmatica.gestionedocumenti.test

import it.finmatica.dto.DtoAutoConfiguration
import it.finmatica.gorm.boot.FinmaticaGormProperties
import it.finmatica.gorm.criteria.FinmaticaGormDecorator
import it.finmatica.hibernatefilter.HibernateFilterAutoConfiguration
import org.h2.Driver
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.hibernate.dialect.H2Dialect
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import org.springframework.jdbc.datasource.SingleConnectionDataSource
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Ignore
class HibernateSpecification extends Specification {

    @Shared
    EntityManagerFactory entityManagerFactory
    @Shared
    SessionFactory sessionFactory
    Transaction transaction

    void setupSpec() {
        entityManagerFactory = entityManagerFactory(dataSource(), hibernateProperties())
        sessionFactory = entityManagerFactory.unwrap(SessionFactory)

        new FinmaticaGormDecorator(sessionFactory, new FinmaticaGormProperties(flushOnSave: false)).decorate()
        ApplicationReadyEvent applicationReadyEvent = Mock(ApplicationReadyEvent)
        ConfigurableApplicationContext applicationContext = Mock(ConfigurableApplicationContext)
        applicationContext.getBean(SessionFactory) >> sessionFactory
        applicationReadyEvent.getApplicationContext() >> applicationContext
        new DtoAutoConfiguration().inizializzaDto(applicationReadyEvent)
    }

    void setup() {
        transaction = sessionFactory.getCurrentSession().beginTransaction()
    }

    void cleanup() {
        transaction.rollback()
    }

    DataSource dataSource() {
        return new SingleConnectionDataSource(new SimpleDriverDataSource(new Driver(), "jdbc:h2:mem:test", "sa", "sa").getConnection(), true)
    }

    public Properties hibernateProperties() {
        final Properties properties = new Properties();

        properties.put("hibernate.dialect", H2Dialect.name);
        properties.put("hibernate.connection.driver_class", Driver.name);
        properties.put("hibernate.current_session_context_class", "thread");
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("show_sql", "true");
        properties.put("hibernate.show_sql", "true");

        return properties;
    }

    public EntityManagerFactory entityManagerFactory(DataSource dataSource, Properties hibernateProperties) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource)
        // è importante l'ordine dei package: nel package di so4 viene definito un filtro hibernate che viene usato
        // negli altri progetti (che brutta cosa dipendere dall'ordine..)
        em.setPackagesToScan("it.finmatica.ad4", "it.finmatica.so4", "it.finmatica")
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter())
        em.setJpaPropertyMap(hibernateProperties);
        em.afterPropertiesSet()
        return em.getObject();
    }
}
