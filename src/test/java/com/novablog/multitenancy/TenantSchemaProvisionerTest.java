package com.novablog.multitenancy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import com.novablog.testconfig.TestContainersConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link TenantSchemaProvisioner}.
 * Uses Testcontainers PostgreSQL to verify actual schema creation and migration.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@DisplayName("TenantSchemaProvisioner Integration Tests")
class TenantSchemaProvisionerTest {

    @Autowired
    private TenantSchemaProvisioner provisioner;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("should create schema and run migrations successfully")
    void shouldProvisionSchemaSuccessfully() throws SQLException {
        String schemaName = "tenant_provtest";

        provisioner.provisionTenantSchema(schemaName);

        // Verify the schema exists
        assertThat(schemaExists(schemaName)).isTrue();

        // Verify tables were created inside the tenant schema
        assertThat(tableExists(schemaName, "users")).isTrue();
        assertThat(tableExists(schemaName, "posts")).isTrue();
        assertThat(tableExists(schemaName, "post_versions")).isTrue();
        assertThat(tableExists(schemaName, "tags")).isTrue();
        assertThat(tableExists(schemaName, "post_tags")).isTrue();
    }

    @Test
    @DisplayName("should complete provisioning in under 10 seconds")
    void shouldProvisionWithinTimeLimit() {
        String schemaName = "tenant_timetest";
        long start = System.currentTimeMillis();

        provisioner.provisionTenantSchema(schemaName);

        long elapsed = System.currentTimeMillis() - start;
        assertThat(elapsed)
                .as("Schema provisioning should complete in under 10 seconds")
                .isLessThan(10_000);
    }

    @Test
    @DisplayName("should reject invalid schema names")
    void shouldRejectInvalidSchemaNames() {
        assertThatThrownBy(() -> provisioner.provisionTenantSchema("UPPERCASE"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> provisioner.provisionTenantSchema("drop;--"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> provisioner.provisionTenantSchema(""))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> provisioner.provisionTenantSchema(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should be idempotent — provisioning same schema twice should not fail")
    void shouldBeIdempotent() {
        String schemaName = "tenant_idempotent";

        provisioner.provisionTenantSchema(schemaName);
        // Second call should not throw
        provisioner.provisionTenantSchema(schemaName);

        assertThat(schemaExists(schemaName)).isTrue();
    }

    // ---- Helper methods ----

    private boolean schemaExists(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT schema_name FROM information_schema.schemata WHERE schema_name = '"
                     + schemaName + "'")) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean tableExists(String schemaName, String tableName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT table_name FROM information_schema.tables WHERE table_schema = '"
                     + schemaName + "' AND table_name = '" + tableName + "'")) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
