package com.novablog.post.dto;

import com.novablog.post.model.PostVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for a post version snapshot.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostVersionResponse {

    private UUID id;
    private UUID postId;
    private String title;
    private String body;
    private int versionNumber;
    private UUID createdBy;
    private OffsetDateTime createdAt;

    public static PostVersionResponse fromEntity(PostVersion version) {
        return PostVersionResponse.builder()
                .id(version.getId())
                .postId(version.getPostId())
                .title(version.getTitle())
                .body(version.getBody())
                .versionNumber(version.getVersionNumber())
                .createdBy(version.getCreatedBy())
                .createdAt(version.getCreatedAt())
                .build();
    }
}
