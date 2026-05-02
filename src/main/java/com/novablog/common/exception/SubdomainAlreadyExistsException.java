package com.novablog.common.exception;

/**
 * Thrown when a subdomain is already registered to another tenant.
 */
public class SubdomainAlreadyExistsException extends RuntimeException {

    private final String subdomain;

    public SubdomainAlreadyExistsException(String subdomain) {
        super("Subdomain '" + subdomain + "' is already registered");
        this.subdomain = subdomain;
    }

    public String getSubdomain() {
        return subdomain;
    }
}
