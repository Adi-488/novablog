package com.novablog.tenant.repository;

import com.novablog.tenant.model.Tenant;
import com.novablog.tenant.model.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for the {@link Tenant} entity in the public schema.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Finds a tenant by its subdomain.
     */
    Optional<Tenant> findBySubdomain(String subdomain);

    /**
     * Finds a tenant by its schema name.
     */
    Optional<Tenant> findBySchemaName(String schemaName);

    /**
     * Checks if a subdomain is already taken.
     */
    boolean existsBySubdomain(String subdomain);

    /**
     * Finds all tenants with the given status.
     */
    List<Tenant> findByStatus(TenantStatus status);
}
