package com.novablog;

import com.novablog.testconfig.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test: verifies the Spring Boot application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
class NovaBlogApplicationTests {

    @Test
    void contextLoads() {
        // Verifies all beans are wired correctly and Flyway migrations run
    }
}
