package com.novablog.tenant.model;

import com.novablog.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity representing a tenant (organization) in the public schema.
 *
 * <p>Each tenant has a unique subdomain and a corresponding PostgreSQL schema
 * (e.g., subdomain "acme" → schema "tenant_acme").</p>
 *
 * <p>This entity lives in the {@code public} schema and serves as the master
 * registry for all tenants in the platform.</p>
 */
@Entity
@Table(name = "tenants", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "org_name", nullable = false)
    private String orgName;

    @Column(name = "subdomain", nullable = false, unique = true, length = 63)
    private String subdomain;

    @Column(name = "schema_name", nullable = false, unique = true, length = 63)
    private String schemaName;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "UTC";
}
