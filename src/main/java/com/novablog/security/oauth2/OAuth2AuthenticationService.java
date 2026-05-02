package com.novablog.security.oauth2;

import com.novablog.multitenancy.TenantContext;
import com.novablog.security.dto.AuthResponse;
import com.novablog.security.jwt.JwtTokenProvider;
import com.novablog.tenant.model.Tenant;
import com.novablog.tenant.repository.TenantRepository;
import com.novablog.user.model.User;
import com.novablog.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service that orchestrates the OAuth2 authentication flow.
 *
 * <p>Flow:
 * <ol>
 *   <li>Receive authorization code + tenant subdomain from frontend</li>
 *   <li>Exchange code with OAuth provider for user profile</li>
 *   <li>Resolve tenant from subdomain → get schema name</li>
 *   <li>Create or update user in tenant schema</li>
 *   <li>Generate JWT access + refresh tokens</li>
 * </ol>
 */
@Slf4j
@Service
public class OAuth2AuthenticationService {

    private final GoogleOAuth2Client googleClient;
    private final GitHubOAuth2Client gitHubClient;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TenantRepository tenantRepository;

    public OAuth2AuthenticationService(
            GoogleOAuth2Client googleClient,
            GitHubOAuth2Client gitHubClient,
            UserService userService,
            JwtTokenProvider jwtTokenProvider,
            TenantRepository tenantRepository) {
        this.googleClient = googleClient;
        this.gitHubClient = gitHubClient;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Authenticates a user via OAuth2 code exchange and returns JWT tokens.
     *
     * @param provider    the OAuth2 provider ("google" or "github")
     * @param code        the authorization code from the OAuth2 provider
     * @param subdomain   the tenant subdomain
     * @param redirectUri the redirect URI used during the OAuth2 flow
     * @return JWT access + refresh token pair
     */
    public AuthResponse authenticateWithOAuth2(String provider, String code,
                                                String subdomain, String redirectUri) {
        log.info("OAuth2 authentication: provider={}, tenant={}", provider, subdomain);

        // Step 1: Resolve tenant
        TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
        Tenant tenant = tenantRepository.findBySubdomain(subdomain)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        "Tenant not found: " + subdomain));

        // Step 2: Exchange code for user info
        OAuth2UserInfo userInfo = exchangeCode(provider, code, redirectUri);

        // Step 3: Create or update user in tenant schema
        TenantContext.setTenantId(tenant.getSchemaName());
        User user = userService.findOrCreateUser(userInfo, tenant.getSchemaName());

        // Step 4: Generate JWT tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                tenant.getSchemaName(),
                tenant.getSubdomain()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                tenant.getSchemaName()
        );

        log.info("OAuth2 authentication successful: user={}, tenant={}",
                user.getEmail(), subdomain);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationMs(),
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                tenant.getSubdomain()
        );
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * @param refreshToken the refresh token
     * @return new JWT access + refresh token pair
     */
    public AuthResponse refreshAccessToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new OAuth2AuthenticationException("Invalid or expired refresh token");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new OAuth2AuthenticationException("Token is not a refresh token");
        }

        // Extract claims from refresh token
        var userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String tenantSchema = jwtTokenProvider.getTenantSchemaFromToken(refreshToken);

        // Load the user from the tenant schema to get current role
        TenantContext.setTenantId(tenantSchema);
        User user = userService.findById(userId);

        // Resolve tenant subdomain from schema
        TenantContext.setTenantId(TenantContext.DEFAULT_TENANT);
        Tenant tenant = tenantRepository.findBySchemaName(tenantSchema)
                .orElseThrow(() -> new OAuth2AuthenticationException(
                        "Tenant not found for schema: " + tenantSchema));

        // Generate new token pair
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                tenant.getSchemaName(),
                tenant.getSubdomain()
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                user.getId(),
                tenant.getSchemaName()
        );

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getAccessTokenExpirationMs(),
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name(),
                tenant.getSubdomain()
        );
    }

    /**
     * Returns the OAuth2 authorization URL for the given provider.
     */
    public String getAuthorizationUrl(String provider, String subdomain, String redirectUri) {
        String state = subdomain; // Include tenant in state for callback
        return switch (provider.toLowerCase()) {
            case "google" -> googleClient.getAuthorizationUrl(redirectUri, state);
            case "github" -> gitHubClient.getAuthorizationUrl(redirectUri, state);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        };
    }

    private OAuth2UserInfo exchangeCode(String provider, String code, String redirectUri) {
        return switch (provider.toLowerCase()) {
            case "google" -> googleClient.exchangeCodeForUserInfo(code, redirectUri);
            case "github" -> gitHubClient.exchangeCodeForUserInfo(code, redirectUri);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        };
    }
}
