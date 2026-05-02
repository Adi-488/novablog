package com.novablog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI configuration for NovaBlog.
 *
 * <p>API documentation is accessible at:
 * <ul>
 *   <li>OpenAPI spec: {@code /api/v1/docs}</li>
 *   <li>Swagger UI: {@code /swagger-ui.html}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI novaBlogOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NovaBlog API")
                        .description("Multi-Tenant SaaS Blogging Platform — REST API Documentation. "
                                + "NovaBlog provides schema-per-tenant isolation, OAuth2 authentication, "
                                + "and a full post lifecycle management system.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Antigravity Engineering")
                                .email("engineering@novablog.dev"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://novablog.dev/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development"),
                        new Server()
                                .url("https://api.novablog.dev")
                                .description("Production")
                ));
    }
}
