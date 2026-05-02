package com.novablog.common.exception;

/**
 * Thrown when a tenant cannot be found by the given identifier.
 */
public class TenantNotFoundException extends RuntimeException {

    private final String identifier;

    public TenantNotFoundException(String identifier) {
        super("Tenant not found with identifier: " + identifier);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
