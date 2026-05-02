package com.novablog.media.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for media file metadata within a tenant's schema.
 *
 * <p><b>Sprint 6:</b> Stores metadata about uploaded files. Actual
 * file bytes are stored on the filesystem (or S3 in production).</p>
 */
@Entity
@Table(name = "media_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_name", nullable = false, length = 500)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 500)
    private String storedName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    @Column(name = "post_id")
    private UUID postId;

    @Column(name = "alt_text", length = 500)
    private String altText;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
