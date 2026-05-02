package com.novablog.multitenancy;

/**
 * Thread-local holder for the current tenant identifier.
 *
 * <p>The tenant identifier is the PostgreSQL schema name (e.g., "tenant_acme").
 * It is set by {@link TenantIdentificationFilter} at the start of each request
 * and read by {@link CurrentTenantIdentifierResolverImpl} when Hibernate needs
 * to resolve the active schema.</p>
 *
 * <p><b>IMPORTANT:</b> Always call {@link #clear()} in a finally block to
 * prevent thread-local leaks in pooled thread environments.</p>
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /** Default schema used when no tenant is resolved (e.g., public endpoints). */
    public static final String DEFAULT_TENANT = "public";

    private TenantContext() {
        // Utility class — no instantiation
    }

    /**
     * Sets the current tenant identifier for this thread.
     *
     * @param tenantId the PostgreSQL schema name (e.g., "tenant_acme")
     */
    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Returns the current tenant identifier, or {@link #DEFAULT_TENANT} if none is set.
     */
    public static String getTenantId() {
        String tenantId = CURRENT_TENANT.get();
        return tenantId != null ? tenantId : DEFAULT_TENANT;
    }

    /**
     * Clears the tenant identifier from the current thread.
     * Must be called in a finally block after request processing.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
