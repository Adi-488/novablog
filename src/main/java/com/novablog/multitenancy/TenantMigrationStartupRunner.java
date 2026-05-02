package com.novablog.multitenancy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs Flyway migrations on all existing tenant schemas at application startup.
 * Required so that when we add new migration files (like Sprint 5/6),
 * existing tenants are updated.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantMigrationStartupRunner implements ApplicationRunner {

    private final DataSource dataSource;
    private final TenantSchemaProvisioner provisioner;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting schema migration for all existing tenants...");
        List<String> tenantSchemas = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT schema_name FROM information_schema.schemata WHERE schema_name LIKE 'tenant_%'")) {
            
            while (rs.next()) {
                tenantSchemas.add(rs.getString("schema_name"));
            }
        } catch (Exception e) {
            log.error("Failed to fetch tenant schemas from database", e);
            return;
        }

        for (String schema : tenantSchemas) {
            try {
                log.info("Running migrations for existing schema: {}", schema);
                // The provisioner's runMigrations method is private, so we'll just run flyway manually
                org.flywaydb.core.Flyway.configure()
                        .dataSource(dataSource)
                        .locations("classpath:db/migration/tenant")
                        .schemas(schema)
                        .baselineOnMigrate(true)
                        .load()
                        .migrate();
                log.info("Migrations completed for schema: {}", schema);
            } catch (Exception e) {
                log.error("Failed to migrate existing schema: {}", schema, e);
            }
        }
        log.info("Finished migrating all {} tenant schemas.", tenantSchemas.size());
    }
}
