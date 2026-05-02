package com.novablog.media.dto;

import com.novablog.media.model.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for media file API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileResponse {
    private UUID id;
    private String originalName;
    private String contentType;
    private long fileSize;
    private String url;
    private String altText;
    private UUID postId;
    private OffsetDateTime createdAt;

    public static MediaFileResponse fromEntity(MediaFile m, String baseUrl) {
        return MediaFileResponse.builder()
                .id(m.getId())
                .originalName(m.getOriginalName())
                .contentType(m.getContentType())
                .fileSize(m.getFileSize())
                .url("/api/v1/media/" + m.getId() + "/download?tenantId=" + com.novablog.multitenancy.TenantContext.getTenantId().replace("tenant_", ""))
                .altText(m.getAltText())
                .postId(m.getPostId())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
