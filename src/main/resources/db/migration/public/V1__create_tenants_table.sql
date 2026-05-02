-- ============================================================
-- NovaBlog — Public Schema Migration V1
-- Creates the master tenants registry table
-- ============================================================

CREATE TABLE IF NOT EXISTS tenants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_name        VARCHAR(255) NOT NULL,
    subdomain       VARCHAR(63)  NOT NULL,
    schema_name     VARCHAR(63)  NOT NULL,
    logo_url        VARCHAR(512),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    timezone        VARCHAR(50)  NOT NULL DEFAULT 'UTC',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_tenants_subdomain  UNIQUE (subdomain),
    CONSTRAINT uq_tenants_schema     UNIQUE (schema_name),
    CONSTRAINT chk_tenants_status    CHECK (status IN ('ACTIVE', 'SUSPENDED', 'ARCHIVED'))
);

-- Index for fast subdomain lookup (tenant resolution)
CREATE INDEX IF NOT EXISTS idx_tenants_subdomain ON tenants(subdomain);
CREATE INDEX IF NOT EXISTS idx_tenants_status    ON tenants(status);

COMMENT ON TABLE  tenants IS 'Master registry of all tenants — lives in public schema';
COMMENT ON COLUMN tenants.schema_name IS 'PostgreSQL schema name for this tenant (e.g. tenant_acme)';
