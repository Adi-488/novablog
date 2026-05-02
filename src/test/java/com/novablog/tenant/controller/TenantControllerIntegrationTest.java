package com.novablog.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novablog.tenant.dto.TenantRegistrationRequest;
import com.novablog.testconfig.TestContainersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link TenantController}.
 *
 * <p>Uses Testcontainers PostgreSQL to validate the full registration flow:
 * subdomain check → register → verify schema creation.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("TenantController Integration Tests")
class TenantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    @DisplayName("POST /register — should register a new tenant successfully")
    void shouldRegisterTenantSuccessfully() throws Exception {
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .orgName("Integration Test Corp")
                .subdomain("inttest")
                .logoUrl("https://example.com/logo.png")
                .timezone("America/New_York")
                .build();

        mockMvc.perform(post("/api/v1/tenants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orgName").value("Integration Test Corp"))
                .andExpect(jsonPath("$.subdomain").value("inttest"))
                .andExpect(jsonPath("$.schemaName").value("tenant_inttest"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.workspaceUrl").value("https://inttest.novablog.dev"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("POST /register — should return 409 for duplicate subdomain")
    void shouldReturn409ForDuplicateSubdomain() throws Exception {
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .orgName("Duplicate Corp")
                .subdomain("inttest")
                .build();

        mockMvc.perform(post("/api/v1/tenants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("inttest")));
    }

    @Test
    @Order(3)
    @DisplayName("POST /register — should return 400 for invalid request body")
    void shouldReturn400ForInvalidRequest() throws Exception {
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .orgName("")  // Blank org name
                .subdomain("ab")  // Too short
                .build();

        mockMvc.perform(post("/api/v1/tenants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isNotEmpty());
    }

    @Test
    @Order(4)
    @DisplayName("GET /check — should return available=true for new subdomain")
    void shouldReturnAvailableForNewSubdomain() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/check")
                        .param("subdomain", "brandnew"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subdomain").value("brandnew"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @Order(5)
    @DisplayName("GET /check — should return available=false for taken subdomain")
    void shouldReturnUnavailableForTakenSubdomain() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/check")
                        .param("subdomain", "inttest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subdomain").value("inttest"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @Order(6)
    @DisplayName("GET /{subdomain} — should return tenant details")
    void shouldReturnTenantBySubdomain() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/inttest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orgName").value("Integration Test Corp"))
                .andExpect(jsonPath("$.subdomain").value("inttest"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /{subdomain} — should return 404 for nonexistent tenant")
    void shouldReturn404ForNonexistentTenant() throws Exception {
        mockMvc.perform(get("/api/v1/tenants/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("nonexistent")));
    }

    @Test
    @Order(8)
    @DisplayName("POST /register — should register tenant with hyphens in subdomain")
    void shouldRegisterTenantWithHyphens() throws Exception {
        TenantRegistrationRequest request = TenantRegistrationRequest.builder()
                .orgName("Hyphen Corp")
                .subdomain("my-cool-blog")
                .build();

        mockMvc.perform(post("/api/v1/tenants/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.schemaName").value("tenant_my_cool_blog"));
    }
}
