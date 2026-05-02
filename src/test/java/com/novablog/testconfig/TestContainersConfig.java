package com.novablog.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Testcontainers configuration for integration tests.
 *
 * <p>Provides a PostgreSQL 15 container that is automatically wired
 * into the Spring datasource via {@link ServiceConnection}.</p>
 *
 * <p>Usage: Import this configuration in integration test classes:
 * <pre>
 * {@code @Import(TestContainersConfig.class)}
 * </pre>
 */
@TestConfiguration
public class TestContainersConfig {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:15-alpine")
                .withDatabaseName("novablog_test")
                .withUsername("test")
                .withPassword("test");
    }
}
