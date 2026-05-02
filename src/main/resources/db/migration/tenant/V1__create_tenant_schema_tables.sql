-- ============================================================
-- NovaBlog — Per-Tenant Schema Migration V1
-- Creates users, posts, post_versions, tags, and post_tags
-- This script runs inside each tenant's schema
-- ============================================================

-- Users table (populated on OAuth2 first login)
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL,
    display_name    VARCHAR(255),
    avatar_url      VARCHAR(512),
    oauth_provider  VARCHAR(20),
    oauth_id        VARCHAR(255),
    role            VARCHAR(20)  NOT NULL DEFAULT 'WRITER',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email         UNIQUE (email),
    CONSTRAINT chk_users_role         CHECK (role IN ('SUPER_ADMIN', 'ORG_ADMIN', 'EDITOR', 'WRITER', 'READER')),
    CONSTRAINT chk_users_oauth_provider CHECK (oauth_provider IS NULL OR oauth_provider IN ('GOOGLE', 'GITHUB'))
);

-- Posts table
CREATE TABLE IF NOT EXISTS posts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(500) NOT NULL,
    slug            VARCHAR(500) NOT NULL,
    body            TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    author_id       UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seo_title       VARCHAR(200),
    seo_description VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_posts_slug    UNIQUE (slug),
    CONSTRAINT chk_posts_status CHECK (status IN ('DRAFT', 'UNDER_REVIEW', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX IF NOT EXISTS idx_posts_author   ON posts(author_id);
CREATE INDEX IF NOT EXISTS idx_posts_status   ON posts(status);
CREATE INDEX IF NOT EXISTS idx_posts_slug     ON posts(slug);

-- Post version history (for diffing and rollback)
CREATE TABLE IF NOT EXISTS post_versions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id         UUID    NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    title           VARCHAR(500),
    body            TEXT,
    version_number  INTEGER NOT NULL,
    created_by      UUID    NOT NULL REFERENCES users(id),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_post_versions_post ON post_versions(post_id);

-- Tags
CREATE TABLE IF NOT EXISTS tags (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    CONSTRAINT uq_tags_name UNIQUE (name)
);

-- Post ↔ Tag join table
CREATE TABLE IF NOT EXISTS post_tags (
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    tag_id  UUID NOT NULL REFERENCES tags(id)  ON DELETE CASCADE,
    PRIMARY KEY (post_id, tag_id)
);

COMMENT ON TABLE users         IS 'Tenant-scoped users — one row per user per tenant';
COMMENT ON TABLE posts         IS 'Blog posts with lifecycle state machine';
COMMENT ON TABLE post_versions IS 'Immutable version snapshots for post history';
