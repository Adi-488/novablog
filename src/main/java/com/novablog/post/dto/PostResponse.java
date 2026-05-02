package com.novablog.post.dto;

import com.novablog.post.model.Post;
import com.novablog.post.model.PostStatus;
import com.novablog.post.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for blog post data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private UUID id;
    private String title;
    private String slug;
    private String body;
    private PostStatus status;
    private UUID authorId;
    private String authorName;
    private String seoTitle;
    private String seoDescription;
    private Set<String> tags;
    private OffsetDateTime publishedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static PostResponse fromEntity(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .body(post.getBody())
                .status(post.getStatus())
                .authorId(post.getAuthor().getId())
                .authorName(post.getAuthor().getDisplayName())
                .seoTitle(post.getSeoTitle())
                .seoDescription(post.getSeoDescription())
                .tags(post.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()))
                .publishedAt(post.getPublishedAt())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
