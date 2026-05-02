package com.novablog.post.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable snapshot of a post at a specific point in time.
 *
 * <p>A new version is created every time a post's title or body is updated.
 * This enables full edit history, diffing, and rollback capabilities.</p>
 */
@Entity
@Table(name = "post_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }
}
