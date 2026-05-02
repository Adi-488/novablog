package com.novablog.tenant.model;

/**
 * Tenant lifecycle status.
 *
 * <p>Transitions:
 * <pre>
 *   ACTIVE → SUSPENDED (admin action)
 *   SUSPENDED → ACTIVE (admin re-activation)
 *   ACTIVE/SUSPENDED → ARCHIVED (deletion request / GDPR)
 * </pre>
 */
public enum TenantStatus {
    /** Tenant is active and operational. */
    ACTIVE,

    /** Tenant is temporarily suspended (e.g., billing, violation). */
    SUSPENDED,

    /** Tenant is archived and data is scheduled for deletion. */
    ARCHIVED
}
