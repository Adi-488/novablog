package com.novablog.tenant.model;

/**
 * Tenant-scoped role hierarchy for NovaBlog.
 *
 * <p>Hierarchy (highest to lowest privilege):
 * <pre>
 *   SUPER_ADMIN > ORG_ADMIN > EDITOR > WRITER > READER
 * </pre>
 *
 * <p>A user can hold different roles in different tenants.
 * For example, a user may be EDITOR in Tenant A but WRITER in Tenant B.</p>
 *
 * <p>Role permissions:
 * <ul>
 *   <li><b>SUPER_ADMIN</b> — Platform-wide admin; can manage all tenants</li>
 *   <li><b>ORG_ADMIN</b> — Tenant admin; manages members, settings, and content</li>
 *   <li><b>EDITOR</b> — Reviews, approves, and publishes posts</li>
 *   <li><b>WRITER</b> — Creates and edits draft posts</li>
 *   <li><b>READER</b> — Read-only access to published content</li>
 * </ul>
 */
public enum Role {
    SUPER_ADMIN(5),
    ORG_ADMIN(4),
    EDITOR(3),
    WRITER(2),
    READER(1);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    /**
     * Returns the numeric privilege level (higher = more privileged).
     */
    public int getLevel() {
        return level;
    }

    /**
     * Checks if this role has equal or higher privilege than the given role.
     */
    public boolean isAtLeast(Role other) {
        return this.level >= other.level;
    }

    /**
     * Checks if this role has strictly higher privilege than the given role.
     */
    public boolean isAbove(Role other) {
        return this.level > other.level;
    }
}
