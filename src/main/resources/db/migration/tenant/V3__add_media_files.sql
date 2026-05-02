-- ============================================================
-- NovaBlog — Per-Tenant Schema Migration V3
-- Adds media_files table for per-tenant media management
-- Sprint 6: Media Management
-- ============================================================

CREATE TABLE IF NOT EXISTS media_files (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    original_name   VARCHAR(500) NOT NULL,
    stored_name     VARCHAR(500) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    file_size       BIGINT       NOT NULL,
    storage_path    VARCHAR(1000) NOT NULL,
    uploaded_by     UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id         UUID         REFERENCES posts(id) ON DELETE SET NULL,
    alt_text        VARCHAR(500),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_media_content_type CHECK (content_type IN (
        'image/jpeg', 'image/png', 'image/gif', 'image/webp',
        'application/pdf', 'video/mp4'
    )),
    CONSTRAINT chk_media_file_size CHECK (file_size > 0 AND file_size <= 52428800)
);

CREATE INDEX IF NOT EXISTS idx_media_uploaded_by ON media_files(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_media_post ON media_files(post_id);

COMMENT ON TABLE media_files IS 'Per-tenant media file metadata — actual files stored on filesystem/S3';
