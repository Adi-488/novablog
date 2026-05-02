package com.novablog.post.model;

import com.novablog.common.audit.AuditableEntity;
import com.novablog.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * JPA entity representing a blog post within a tenant's schema.
 *
 * <p>Posts follow a lifecycle state machine:
 * {@code DRAFT → UNDER_REVIEW → PUBLISHED → ARCHIVED}</p>
 *
 * <p>Each post has a unique slug within the tenant, versioning support,
 * SEO metadata, and a many-to-many relationship with tags.</p>
 */
@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 500)
    private String slug;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "seo_title", length = 200)
    private String seoTitle;

    @Column(name = "seo_description", length = 500)
    private String seoDescription;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    /**
     * Adds a tag to this post.
     */
    public void addTag(Tag tag) {
        tags.add(tag);
    }

    /**
     * Removes a tag from this post.
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }
}
