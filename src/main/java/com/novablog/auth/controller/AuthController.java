package com.novablog.auth.controller;

import com.novablog.security.dto.AuthResponse;
import com.novablog.security.dto.OAuthCodeRequest;
import com.novablog.security.dto.RefreshTokenRequest;
import com.novablog.security.oauth2.OAuth2AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 *
 * <p>Handles OAuth2 social login (Google, GitHub) and JWT token management.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "OAuth2 social login and JWT token management")
public class AuthController {

    private final OAuth2AuthenticationService authService;

    public AuthController(OAuth2AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Redirects the user to the OAuth2 provider's authorization page.
     *
     * @param provider the OAuth2 provider ("google" or "github")
     * @param tenant   the tenant subdomain
     * @param redirectUri optional custom redirect URI
     * @return 302 redirect to OAuth2 provider
     */
    @GetMapping("/login/{provider}")
    @Operation(
            summary = "Initiate OAuth2 login",
            description = "Redirects to the OAuth2 provider's consent screen. "
                    + "After consent, the provider redirects back with an authorization code."
    )
    @ApiResponse(responseCode = "302", description = "Redirect to OAuth2 provider")
    @ApiResponse(responseCode = "400", description = "Invalid provider or tenant")
    public ResponseEntity<Void> initiateLogin(
            @PathVariable String provider,
            @RequestParam String tenant,
            @RequestParam(required = false) String redirectUri) {

        String authUrl = authService.getAuthorizationUrl(provider, tenant, redirectUri);
        return ResponseEntity.status(302)
                .header("Location", authUrl)
                .build();
    }

    /**
     * Exchanges an OAuth2 authorization code for JWT access + refresh tokens.
     *
     * <p>This is called by the frontend after the OAuth2 provider redirects back
     * with an authorization code.</p>
     *
     * @param provider the OAuth2 provider ("google" or "github")
     * @param request  the code exchange request
     * @return JWT token pair + user info
     */
    @PostMapping("/callback/{provider}")
    @Operation(
            summary = "Exchange OAuth2 code for JWT tokens",
            description = "Exchanges the authorization code from the OAuth2 provider "
                    + "for NovaBlog JWT access and refresh tokens. Creates the user "
                    + "in the tenant's schema on first login."
    )
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "400", description = "Invalid code or tenant")
    @ApiResponse(responseCode = "401", description = "OAuth2 authentication failed")
    public ResponseEntity<AuthResponse> exchangeCode(
            @PathVariable String provider,
            @Valid @RequestBody OAuthCodeRequest request) {

        AuthResponse response = authService.authenticateWithOAuth2(
                provider,
                request.getCode(),
                request.getTenant(),
                request.getRedirectUri()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @return new JWT token pair
     */
    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token. "
                    + "The refresh token is also rotated for security."
    )
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}
