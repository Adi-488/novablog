package com.novablog.tenant.controller;

import com.novablog.tenant.dto.SubdomainAvailabilityResponse;
import com.novablog.tenant.dto.TenantRegistrationRequest;
import com.novablog.tenant.dto.TenantResponse;
import com.novablog.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for tenant management operations.
 *
 * <p>All endpoints in this controller are public (no tenant resolution required)
 * because they operate on the public schema's tenants table.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenant Management", description = "Tenant registration and subdomain management")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * Registers a new organization tenant.
     *
     * <p>Creates the tenant record in the public schema, then provisions
     * a dedicated PostgreSQL schema with all required tables.</p>
     *
     * @param request the registration request with org name, subdomain, etc.
     * @return 201 Created with the tenant details
     */
    @PostMapping("/register")
    @Operation(
            summary = "Register a new tenant",
            description = "Creates a new organization workspace with an isolated PostgreSQL schema. "
                    + "Schema provisioning completes in under 10 seconds."
    )
    @ApiResponse(responseCode = "201", description = "Tenant created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or validation error")
    @ApiResponse(responseCode = "409", description = "Subdomain already exists")
    public ResponseEntity<TenantResponse> registerTenant(
            @Valid @RequestBody TenantRegistrationRequest request) {
        log.info("Tenant registration request received: subdomain='{}'", request.getSubdomain());
        TenantResponse response = tenantService.registerTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Checks if a subdomain is available for registration.
     *
     * @param subdomain the subdomain to check
     * @return 200 OK with availability status
     */
    @GetMapping("/check")
    @Operation(
            summary = "Check subdomain availability",
            description = "Returns whether a subdomain is available for tenant registration"
    )
    @ApiResponse(responseCode = "200", description = "Availability status returned")
    public ResponseEntity<SubdomainAvailabilityResponse> checkSubdomain(
            @RequestParam String subdomain) {
        boolean available = tenantService.isSubdomainAvailable(subdomain);
        return ResponseEntity.ok(new SubdomainAvailabilityResponse(subdomain, available));
    }

    /**
     * Retrieves a tenant by its subdomain.
     *
     * @param subdomain the tenant's subdomain
     * @return 200 OK with tenant details
     */
    @GetMapping("/{subdomain}")
    @Operation(
            summary = "Get tenant by subdomain",
            description = "Retrieves tenant details by subdomain identifier"
    )
    @ApiResponse(responseCode = "200", description = "Tenant found")
    @ApiResponse(responseCode = "404", description = "Tenant not found")
    public ResponseEntity<TenantResponse> getTenantBySubdomain(
            @PathVariable String subdomain) {
        TenantResponse response = tenantService.getTenantBySubdomain(subdomain);
        return ResponseEntity.ok(response);
    }
}
