package com.novablog.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TenantIdentificationFilter}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantIdentificationFilter")
class TenantIdentificationFilterTest {

    private TenantIdentificationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new TenantIdentificationFilter();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("should resolve tenant from X-Tenant-ID header")
    void shouldResolveTenantFromHeader() throws Exception {
        when(request.getHeader("X-Tenant-ID")).thenReturn("acme");
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getServerName()).thenReturn("localhost");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should resolve tenant from subdomain")
    void shouldResolveTenantFromSubdomain() throws Exception {
        when(request.getHeader("X-Tenant-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getServerName()).thenReturn("acme.novablog.dev");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/v1/tenants/register",
            "/api/v1/tenants/check",
            "/actuator/health",
            "/swagger-ui/index.html",
            "/api/v1/docs",
            "/error"
    })
    @DisplayName("should skip tenant resolution for public paths")
    void shouldSkipResolutionForPublicPaths(String path) throws Exception {
        when(request.getRequestURI()).thenReturn(path);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should return 400 when tenant cannot be resolved")
    void shouldReturn400WhenTenantNotResolved() throws Exception {
        when(request.getHeader("X-Tenant-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getMethod()).thenReturn("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(eq(400), anyString());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("should prefix schema name with 'tenant_' if not already prefixed")
    void shouldPrefixSchemaName() throws Exception {
        when(request.getHeader("X-Tenant-ID")).thenReturn("acme");
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getServerName()).thenReturn("localhost");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should not double-prefix 'tenant_' if header already has it")
    void shouldNotDoublePrefixSchemaName() throws Exception {
        when(request.getHeader("X-Tenant-ID")).thenReturn("tenant_acme");
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getServerName()).thenReturn("localhost");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("should always clear TenantContext after request (even on exception)")
    void shouldAlwaysClearTenantContext() throws Exception {
        when(request.getHeader("X-Tenant-ID")).thenReturn("acme");
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getServerName()).thenReturn("localhost");
        doThrow(new RuntimeException("Test exception"))
                .when(filterChain).doFilter(request, response);

        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException ignored) {
        }

        assertThat(TenantContext.getTenantId()).isEqualTo(TenantContext.DEFAULT_TENANT);
    }

    // ---- extractTenantFromSubdomain tests ----

    @Test
    @DisplayName("should extract 'acme' from 'acme.novablog.dev'")
    void shouldExtractSubdomain() {
        assertThat(filter.extractTenantFromSubdomain("acme.novablog.dev"))
                .isEqualTo("acme");
    }

    @Test
    @DisplayName("should return null for 'localhost'")
    void shouldReturnNullForLocalhost() {
        assertThat(filter.extractTenantFromSubdomain("localhost")).isNull();
    }

    @Test
    @DisplayName("should return null for IP addresses")
    void shouldReturnNullForIpAddresses() {
        assertThat(filter.extractTenantFromSubdomain("192.168.1.100")).isNull();
    }

    @Test
    @DisplayName("should return null for two-part hostnames")
    void shouldReturnNullForTwoPartHostnames() {
        assertThat(filter.extractTenantFromSubdomain("novablog.dev")).isNull();
    }

    @Test
    @DisplayName("should handle host with port")
    void shouldHandleHostWithPort() {
        assertThat(filter.extractTenantFromSubdomain("acme.novablog.dev:8080"))
                .isEqualTo("acme");
    }

    @Test
    @DisplayName("should return null for null host")
    void shouldReturnNullForNullHost() {
        assertThat(filter.extractTenantFromSubdomain(null)).isNull();
    }
}
