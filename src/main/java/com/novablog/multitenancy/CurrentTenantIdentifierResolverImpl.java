package com.novablog.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Hibernate tenant identifier resolver that reads from {@link TenantContext}.
 *
 * <p>This is called by Hibernate on every database operation to determine which
 * schema to route the query to. It reads the ThreadLocal value set by
 * {@link TenantIdentificationFilter}.</p>
 */
@Slf4j
@Component
public class CurrentTenantIdentifierResolverImpl
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        log.trace("Resolved current tenant: {}", tenantId);
        return tenantId;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
