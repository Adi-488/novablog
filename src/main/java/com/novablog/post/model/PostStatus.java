package com.novablog.post.model;

/**
 * Post lifecycle states.
 *
 * <p>State machine:
 * <pre>
 *   DRAFT → UNDER_REVIEW → PUBLISHED → ARCHIVED
 *                  ↓           ↑
 *                  └→ SCHEDULED ┘
 * </pre>
 *
 * <p>Valid transitions:
 * <ul>
 *   <li>DRAFT → UNDER_REVIEW (Writer submits for review)</li>
 *   <li>UNDER_REVIEW → DRAFT (Editor rejects / requests changes)</li>
 *   <li>UNDER_REVIEW → SCHEDULED (Editor approves with future publish date)</li>
 *   <li>UNDER_REVIEW → PUBLISHED (Editor approves for immediate publish)</li>
 *   <li>SCHEDULED → PUBLISHED (System auto-publishes at scheduled time)</li>
 *   <li>PUBLISHED → ARCHIVED (Admin/Editor archives old content)</li>
 *   <li>ARCHIVED → DRAFT (Restore from archive)</li>
 * </ul>
 */
public enum PostStatus {
    DRAFT,
    UNDER_REVIEW,
    SCHEDULED,
    PUBLISHED,
    ARCHIVED;

    /**
     * Checks if transitioning from this status to the target is allowed.
     */
    public boolean canTransitionTo(PostStatus target) {
        return switch (this) {
            case DRAFT -> target == UNDER_REVIEW;
            case UNDER_REVIEW -> target == DRAFT || target == SCHEDULED || target == PUBLISHED;
            case SCHEDULED -> target == PUBLISHED;
            case PUBLISHED -> target == ARCHIVED;
            case ARCHIVED -> target == DRAFT;
        };
    }
}
