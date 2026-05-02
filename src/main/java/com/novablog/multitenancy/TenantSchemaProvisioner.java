package com.novablog.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Provisions new tenant schemas by creating the PostgreSQL schema
 * and running Flyway migrations against it.
 *
 * <p>This component is called during tenant registration (US-001).
 * Target: schema provisioning must complete in < 10 seconds.</p>
 */
@Slf4j
@Component
public class TenantSchemaProvisioner {

    private final DataSource dataSource;
    private final String migrationLocation;

    public TenantSchemaProvisioner(
            DataSource dataSource,
            @Value("${novablog.tenant.migration-location}") String migrationLocation) {
        this.dataSource = dataSource;
        this.migrationLocation = migrationLocation;
    }

    /**
     * Creates a new PostgreSQL schema for the given tenant and runs
     * all tenant-specific Flyway migrations against it.
     *
     * @param schemaName the schema name (e.g., "tenant_acme")
     * @throws TenantProvisioningException if schema creation or migration fails
     */
    public void provisionTenantSchema(String schemaName) {
        log.info("Provisioning tenant schema: {}", schemaName);
        long startTime = System.currentTimeMillis();

        validateSchemaName(schemaName);
        createSchema(schemaName);
        runMigrations(schemaName);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Tenant schema '{}' provisioned in {}ms", schemaName, elapsed);

        if (elapsed > 10_000) {
            log.warn("Schema provisioning for '{}' exceeded 10s target ({}ms)", schemaName, elapsed);
        }
    }

    /**
     * Creates the PostgreSQL schema if it does not already exist.
     */
    private void createSchema(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            log.debug("Schema '{}' created successfully", schemaName);
        } catch (SQLException e) {
            log.error("Failed to create schema: {}", schemaName, e);
            throw new TenantProvisioningException(
                    "Failed to create schema '" + schemaName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Runs Flyway migrations in the tenant schema.
     */
    private void runMigrations(String schemaName) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations(migrationLocation)
                    .schemas(schemaName)
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
            log.debug("Flyway migrations completed for schema: {}", schemaName);
        } catch (Exception e) {
            log.error("Flyway migration failed for schema: {}", schemaName, e);
            // Attempt to clean up the schema on failure
            dropSchema(schemaName);
            throw new TenantProvisioningException(
                    "Migration failed for schema '" + schemaName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Drops a schema (used for rollback on migration failure).
     */
    private void dropSchema(String schemaName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
            log.warn("Rolled back: dropped schema '{}' after migration failure", schemaName);
        } catch (SQLException e) {
            log.error("Failed to drop schema '{}' during rollback", schemaName, e);
        }
    }

    /**
     * Validates that the schema name is safe to use in SQL.
     */
    private void validateSchemaName(String schemaName) {
        if (schemaName == null || !schemaName.matches("^[a-z][a-z0-9_]{1,62}$")) {
            throw new IllegalArgumentException(
                    "Invalid schema name '" + schemaName + "'. Must be lowercase, start with a letter, "
                    + "contain only letters/numbers/underscores, and be 2-63 characters.");
        }
    }

    /**
     * Runtime exception for tenant provisioning failures.
     */
    public static class TenantProvisioningException extends RuntimeException {
        public TenantProvisioningException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
