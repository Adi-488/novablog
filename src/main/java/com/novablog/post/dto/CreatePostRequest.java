package com.novablog.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Request DTO for creating a new blog post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    /**
     * Post body content (can be HTML or markdown).
     */
    private String body;

    /**
     * Optional custom slug. If not provided, one will be generated from the title.
     */
    @Size(max = 500, message = "Slug must not exceed 500 characters")
    private String slug;

    @Size(max = 200, message = "SEO title must not exceed 200 characters")
    private String seoTitle;

    @Size(max = 500, message = "SEO description must not exceed 500 characters")
    private String seoDescription;

    /**
     * Optional tag names. Tags are created if they don't exist.
     */
    private Set<String> tags;
}
