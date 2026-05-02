package com.novablog.multitenancy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TenantContext}.
 */
@DisplayName("TenantContext")
class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should return DEFAULT_TENANT when no tenant is set")
    void shouldReturnDefaultWhenNoTenantSet() {
        assertThat(TenantContext.getTenantId())
                .isEqualTo(TenantContext.DEFAULT_TENANT);
    }

    @Test
    @DisplayName("should return the set tenant identifier")
    void shouldReturnSetTenantId() {
        TenantContext.setTenantId("tenant_acme");

        assertThat(TenantContext.getTenantId()).isEqualTo("tenant_acme");
    }

    @Test
    @DisplayName("should return DEFAULT_TENANT after clear()")
    void shouldReturnDefaultAfterClear() {
        TenantContext.setTenantId("tenant_acme");
        TenantContext.clear();

        assertThat(TenantContext.getTenantId())
                .isEqualTo(TenantContext.DEFAULT_TENANT);
    }

    @Test
    @DisplayName("should overwrite previous tenant identifier")
    void shouldOverwritePreviousTenant() {
        TenantContext.setTenantId("tenant_acme");
        TenantContext.setTenantId("tenant_globex");

        assertThat(TenantContext.getTenantId()).isEqualTo("tenant_globex");
    }

    @Test
    @DisplayName("should be thread-isolated")
    void shouldBeThreadIsolated() throws InterruptedException {
        TenantContext.setTenantId("tenant_main_thread");

        Thread otherThread = new Thread(() -> {
            assertThat(TenantContext.getTenantId())
                    .isEqualTo(TenantContext.DEFAULT_TENANT);
            TenantContext.setTenantId("tenant_other_thread");
            assertThat(TenantContext.getTenantId()).isEqualTo("tenant_other_thread");
            TenantContext.clear();
        });

        otherThread.start();
        otherThread.join();

        // Main thread's value should be unaffected
        assertThat(TenantContext.getTenantId()).isEqualTo("tenant_main_thread");
    }

    @Test
    @DisplayName("DEFAULT_TENANT should be 'public'")
    void defaultTenantShouldBePublic() {
        assertThat(TenantContext.DEFAULT_TENANT).isEqualTo("public");
    }
}
