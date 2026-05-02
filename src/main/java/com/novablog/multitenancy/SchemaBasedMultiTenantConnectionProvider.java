package com.novablog.multitenancy;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Hibernate multi-tenant connection provider using PostgreSQL schema switching.
 *
 * <p>For each tenant request, this provider acquires a connection from the shared
 * pool and issues {@code SET search_path TO <schema>} to route queries to
 * the correct tenant schema.</p>
 *
 * <p>On release, the search path is reset to {@code public} to prevent
 * cross-tenant data leakage.</p>
 */
@Slf4j
@Component
public class SchemaBasedMultiTenantConnectionProvider
        implements MultiTenantConnectionProvider<String>, HibernatePropertiesCustomizer {

    private final DataSource dataSource;

    public SchemaBasedMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        log.debug("Getting connection for tenant schema: {}", tenantIdentifier);
        Connection connection = getAnyConnection();
        try {
            connection.createStatement()
                    .execute("SET search_path TO " + sanitizeSchemaName(tenantIdentifier) + ", public");
        } catch (SQLException e) {
            log.error("Failed to set search_path for tenant: {}", tenantIdentifier, e);
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            // Reset search_path to public to prevent cross-tenant leakage
            connection.createStatement().execute("SET search_path TO public");
        } catch (SQLException e) {
            log.warn("Failed to reset search_path after releasing connection for tenant: {}",
                    tenantIdentifier, e);
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        throw new UnsupportedOperationException("Cannot unwrap " + unwrapType);
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, this);
    }

    /**
     * Sanitizes schema names to prevent SQL injection.
     * Only allows alphanumeric characters and underscores.
     */
    private String sanitizeSchemaName(String schemaName) {
        if (schemaName == null || !schemaName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("Invalid schema name: " + schemaName);
        }
        return schemaName;
    }
}
