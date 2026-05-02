package com.novablog.common.exception;

import com.novablog.multitenancy.TenantSchemaProvisioner.TenantProvisioningException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for all REST controllers.
 * Provides consistent error response format across the API.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles JSR-380 validation errors (e.g., @Valid failures).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = buildErrorBody(
                HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles subdomain already exists (409 Conflict).
     */
    @ExceptionHandler(SubdomainAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleSubdomainConflict(
            SubdomainAlreadyExistsException ex) {
        Map<String, Object> body = buildErrorBody(
                HttpStatus.CONFLICT, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /**
     * Handles tenant not found (404).
     */
    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTenantNotFound(
            TenantNotFoundException ex) {
        Map<String, Object> body = buildErrorBody(
                HttpStatus.NOT_FOUND, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Handles tenant provisioning failures (500).
     */
    @ExceptionHandler(TenantProvisioningException.class)
    public ResponseEntity<Map<String, Object>> handleProvisioningError(
            TenantProvisioningException ex) {
        log.error("Tenant provisioning failed", ex);
        Map<String, Object> body = buildErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to provision tenant workspace. Please try again.",
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Handles illegal argument exceptions (400).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        Map<String, Object> body = buildErrorBody(
                HttpStatus.BAD_REQUEST, ex.getMessage(), null);
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles OAuth2 authentication failures (401).
     */
    @ExceptionHandler(com.novablog.security.oauth2.OAuth2AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleOAuth2AuthError(
            com.novablog.security.oauth2.OAuth2AuthenticationException ex) {
        log.warn("OAuth2 authentication failed: {}", ex.getMessage());
        Map<String, Object> body = buildErrorBody(
                HttpStatus.UNAUTHORIZED, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    /**
     * Handles security/authorization violations (403).
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(
            SecurityException ex) {
        Map<String, Object> body = buildErrorBody(
                HttpStatus.FORBIDDEN, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Handles Spring Security access denied (403).
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        Map<String, Object> body = buildErrorBody(
                HttpStatus.FORBIDDEN, "Insufficient privileges", null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Catch-all for unexpected exceptions (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, Object> body = buildErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Builds a consistent error response body.
     */
    private Map<String, Object> buildErrorBody(
            HttpStatus status, String message, Map<String, String> errors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            body.put("fieldErrors", errors);
        }
        return body;
    }
}
