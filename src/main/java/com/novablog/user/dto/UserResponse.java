package com.novablog.user.dto;

import com.novablog.tenant.model.Role;
import com.novablog.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for user profile information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String oauthProvider;
    private Role role;
    private OffsetDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .oauthProvider(user.getOauthProvider() != null
                        ? user.getOauthProvider().name() : null)
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
