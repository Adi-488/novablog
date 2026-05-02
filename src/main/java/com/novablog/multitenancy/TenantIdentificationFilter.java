package com.novablog.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Servlet filter that resolves the tenant identifier from the incoming request
 * and populates {@link TenantContext}.
 *
 * <p>Resolution strategy (in order of precedence):
 * <ol>
 *   <li><b>X-Tenant-ID header</b> — explicit tenant override (useful for testing)</li>
 *   <li><b>Subdomain extraction</b> — e.g., {@code acme.novablog.dev} → {@code tenant_acme}</li>
 * </ol>
 *
 * <p>Public endpoints (registration, health, docs) bypass tenant resolution.</p>
 *
 * <p>In Sprint 2, JWT-based resolution will be added as the primary strategy.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantIdentificationFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";
    private static final String SCHEMA_PREFIX = "tenant_";

    /** Paths that do NOT require tenant resolution. */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/tenants",
            "/api/v1/auth",
            "/actuator",
            "/swagger-ui",
            "/api/v1/docs",
            "/v3/api-docs",
            "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();

            // Skip tenant resolution for public endpoints
            if (isPublicPath(path)) {
                TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
                filterChain.doFilter(request, response);
                return;
            }

            // Strategy 1: Explicit header (dev/testing)
            String tenantId = request.getHeader(TENANT_HEADER);

            // Strategy 2: Subdomain extraction
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = extractTenantFromSubdomain(request.getServerName());
            }

            // Strategy 3: Query parameter (useful for images loaded directly in browser)
            if (tenantId == null || tenantId.isBlank()) {
                tenantId = request.getParameter("tenantId");
            }

            if (tenantId == null || tenantId.isBlank()) {
                log.warn("Could not resolve tenant for request: {} {}", request.getMethod(), path);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Tenant identifier could not be resolved. Provide X-Tenant-ID header or use a tenant subdomain.");
                return;
            }

            // Normalize to schema name
            String schemaName = tenantId.startsWith(SCHEMA_PREFIX) ? tenantId : SCHEMA_PREFIX + tenantId;
            TenantContext.setTenantId(schemaName);
            log.debug("Tenant resolved: {} for {} {}", schemaName, request.getMethod(), path);

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Extracts the tenant subdomain from the host.
     * Example: "acme.novablog.dev" → "acme", "localhost" → null
     */
    String extractTenantFromSubdomain(String host) {
        if (host == null) return null;

        // Remove port if present
        String hostWithoutPort = host.contains(":") ? host.substring(0, host.indexOf(':')) : host;

        // Skip localhost and IP addresses
        if ("localhost".equalsIgnoreCase(hostWithoutPort) || hostWithoutPort.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return null;
        }

        String[] parts = hostWithoutPort.split("\\.");
        // Expect at least 3 parts: subdomain.domain.tld
        if (parts.length >= 3) {
            return parts[0];
        }

        return null;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
