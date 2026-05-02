package com.novablog.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO containing JWT access and refresh tokens after authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String userId;
    private String email;
    private String role;
    private String tenantSubdomain;

    public static AuthResponse of(String accessToken, String refreshToken,
                                   long expiresInMs, String userId,
                                   String email, String role, String tenantSubdomain) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresInMs / 1000) // Convert to seconds for client
                .userId(userId)
                .email(email)
                .role(role)
                .tenantSubdomain(tenantSubdomain)
                .build();
    }
}
