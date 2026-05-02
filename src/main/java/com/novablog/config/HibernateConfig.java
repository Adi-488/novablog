package com.novablog.config;

import com.novablog.multitenancy.TenantContext;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Hibernate multi-tenancy configuration.
 *
 * <p>Configures the JPA EntityManagerFactory and TransactionManager
 * for schema-based multi-tenancy using the SCHEMA strategy.</p>
 *
 * <p>The actual connection provider and tenant identifier resolver
 * are registered as HibernatePropertiesCustomizer beans in their
 * respective classes.</p>
 */
@Configuration
public class HibernateConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
