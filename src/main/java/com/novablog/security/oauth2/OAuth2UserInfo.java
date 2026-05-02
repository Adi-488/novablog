package com.novablog.security.oauth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Normalized user information extracted from an OAuth2 provider response.
 *
 * <p>Both Google and GitHub return different JSON structures.
 * This class normalizes them into a common format.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {

    private String oauthId;
    private String email;
    private String name;
    private String avatarUrl;
    private String provider; // "GOOGLE" or "GITHUB"
}
