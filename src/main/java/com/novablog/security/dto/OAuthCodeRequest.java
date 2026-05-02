package com.novablog.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for the OAuth2 code exchange endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuthCodeRequest {

    @NotBlank(message = "Authorization code is required")
    private String code;

    @NotBlank(message = "Tenant subdomain is required")
    private String tenant;

    /**
     * Optional redirect URI used during the OAuth2 flow.
     * Must match the redirect URI registered with the OAuth2 provider.
     */
    private String redirectUri;
}
