package com.novablog.notification.dto;

import com.novablog.notification.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for notification API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private UUID id;
    private String type;
    private String title;
    private String message;
    private String link;
    private boolean read;
    private OffsetDateTime createdAt;

    public static NotificationResponse fromEntity(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .link(n.getLink())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
