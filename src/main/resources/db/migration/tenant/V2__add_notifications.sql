-- ============================================================
-- NovaBlog — Per-Tenant Schema Migration V2
-- Adds notifications table for in-app notification system
-- Sprint 5: Review workflow & notifications
-- ============================================================

CREATE TABLE IF NOT EXISTS notifications (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type            VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    message         TEXT,
    link            VARCHAR(500),
    read            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_notification_type CHECK (type IN (
        'POST_SUBMITTED', 'POST_APPROVED', 'POST_REJECTED',
        'POST_PUBLISHED', 'POST_SCHEDULED', 'ROLE_CHANGED',
        'COMMENT', 'SYSTEM'
    ))
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(user_id, read) WHERE read = FALSE;

COMMENT ON TABLE notifications IS 'In-app notifications for post lifecycle events and system messages';
