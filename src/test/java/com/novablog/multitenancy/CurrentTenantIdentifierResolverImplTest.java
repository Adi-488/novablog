package com.novablog.multitenancy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CurrentTenantIdentifierResolverImpl}.
 */
@DisplayName("CurrentTenantIdentifierResolver")
class CurrentTenantIdentifierResolverImplTest {

    private final CurrentTenantIdentifierResolverImpl resolver =
            new CurrentTenantIdentifierResolverImpl();

    @Test
    @DisplayName("should resolve to DEFAULT_TENANT when no tenant is set")
    void shouldResolveToDefault() {
        TenantContext.clear();

        String tenantId = resolver.resolveCurrentTenantIdentifier();

        assertThat(tenantId).isEqualTo(TenantContext.DEFAULT_TENANT);
    }

    @Test
    @DisplayName("should resolve to the set tenant identifier")
    void shouldResolveToSetTenant() {
        TenantContext.setTenantId("tenant_acme");

        try {
            String tenantId = resolver.resolveCurrentTenantIdentifier();
            assertThat(tenantId).isEqualTo("tenant_acme");
        } finally {
            TenantContext.clear();
        }
    }

    @Test
    @DisplayName("should validate existing current sessions")
    void shouldValidateExistingSessions() {
        assertThat(resolver.validateExistingCurrentSessions()).isTrue();
    }
}
