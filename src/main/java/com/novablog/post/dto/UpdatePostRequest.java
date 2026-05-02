package com.novablog.post.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Request DTO for updating an existing blog post.
 * All fields are optional — only provided fields are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    private String body;

    @Size(max = 200, message = "SEO title must not exceed 200 characters")
    private String seoTitle;

    @Size(max = 500, message = "SEO description must not exceed 500 characters")
    private String seoDescription;

    /**
     * Tags to set on the post. Replaces existing tags.
     */
    private Set<String> tags;

    /**
     * Scheduled publish time (only used when transitioning to SCHEDULED status).
     */
    private OffsetDateTime scheduledPublishAt;
}
