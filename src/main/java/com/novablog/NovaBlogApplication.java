package com.novablog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NovaBlog — Multi-Tenant SaaS Blogging Platform
 *
 * <p>Entry point for the Spring Boot application. Multi-tenancy is handled via
 * schema-per-tenant isolation with Hibernate's SCHEMA strategy.</p>
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class NovaBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovaBlogApplication.class, args);
    }
}
