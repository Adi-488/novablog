package com.novablog.tenant.service;

import com.novablog.common.exception.SubdomainAlreadyExistsException;
import com.novablog.common.exception.TenantNotFoundException;
import com.novablog.multitenancy.TenantContext;
import com.novablog.multitenancy.TenantSchemaProvisioner;
import com.novablog.tenant.dto.TenantRegistrationRequest;
import com.novablog.tenant.dto.TenantResponse;
import com.novablog.tenant.model.Tenant;
import com.novablog.tenant.model.TenantStatus;
import com.novablog.tenant.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for tenant management operations.
 *
 * <p>Handles:
 * <ul>
 *   <li>Tenant registration with schema provisioning (US-001)</li>
 *   <li>Subdomain availability checking</li>
 *   <li>Tenant lookup by subdomain</li>
 * </ul>
 */
@Slf4j
@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantSchemaProvisioner schemaProvisioner;
    private final String schemaPrefix;

    public TenantService(
            TenantRepository tenantRepository,
            TenantSchemaProvisioner schemaProvisioner,
            @Value("${novablog.tenant.schema-prefix}") String schemaPrefix) {
        this.tenantRepository = tenantRepository;
        this.schemaProvisioner = schemaProvisioner;
        this.schemaPrefix = schemaPrefix;
    }

    /**
     * Registers a new tenant: validates uniqueness, persists to public.tenants,
     * and provisions the tenant's PostgreSQL schema with Flyway migrations.
     *
     * <p>This operation must complete in < 10 seconds (US-001 AC).</p>
     *
     * @param request the registration request
     * @return the created tenant response
     * @throws SubdomainAlreadyExistsException if subdomain is taken
     */
    @Transactional
    public TenantResponse registerTenant(TenantRegistrationRequest request) {
        log.info("Registering new tenant: orgName='{}', subdomain='{}'",
                request.getOrgName(), request.getSubdomain());

        // Ensure we operate on the public schema for tenant creation
        TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);

        // Check subdomain uniqueness
        if (tenantRepository.existsBySubdomain(request.getSubdomain())) {
            throw new SubdomainAlreadyExistsException(request.getSubdomain());
        }

        String schemaName = buildSchemaName(request.getSubdomain());

        // Create and persist tenant entity
        Tenant tenant = Tenant.builder()
                .orgName(request.getOrgName())
                .subdomain(request.getSubdomain())
                .schemaName(schemaName)
                .logoUrl(request.getLogoUrl())
                .timezone(request.getTimezone() != null ? request.getTimezone() : "UTC")
                .status(TenantStatus.ACTIVE)
                .build();

        tenant = tenantRepository.save(tenant);
        log.info("Tenant entity saved: id={}, schema={}", tenant.getId(), schemaName);

        // Provision the tenant schema (create schema + run Flyway migrations)
        schemaProvisioner.provisionTenantSchema(schemaName);

        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Checks if a subdomain is available for registration.
     *
     * @param subdomain the subdomain to check
     * @return true if available, false if taken
     */
    @Transactional(readOnly = true)
    public boolean isSubdomainAvailable(String subdomain) {
        TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
        return !tenantRepository.existsBySubdomain(subdomain);
    }

    /**
     * Retrieves a tenant by its subdomain.
     *
     * @param subdomain the tenant's subdomain
     * @return the tenant response
     * @throws TenantNotFoundException if no tenant exists with the given subdomain
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantBySubdomain(String subdomain) {
        TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new TenantNotFoundException(subdomain));
        return TenantResponse.fromEntity(tenant);
    }

    /**
     * Lists all tenants with the given status.
     *
     * @param status the status to filter by (null for all)
     * @return list of tenant responses
     */
    @Transactional(readOnly = true)
    public List<TenantResponse> listTenants(TenantStatus status) {
        TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
        List<Tenant> tenants = status != null
                ? tenantRepository.findByStatus(status)
                : tenantRepository.findAll();
        return tenants.stream()
                .map(TenantResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Builds the PostgreSQL schema name from a subdomain.
     * Converts hyphens to underscores for SQL compatibility.
     *
     * @param subdomain the tenant's subdomain (e.g., "acme-corp")
     * @return the schema name (e.g., "tenant_acme_corp")
     */
    String buildSchemaName(String subdomain) {
        return schemaPrefix + subdomain.replace("-", "_");
    }
}
