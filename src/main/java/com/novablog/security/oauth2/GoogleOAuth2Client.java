package com.novablog.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * OAuth2 client for Google authentication.
 *
 * <p>Handles the authorization code exchange and user profile retrieval
 * from Google's OAuth2 API.</p>
 */
@Slf4j
@Component
public class GoogleOAuth2Client {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:}")
    private String defaultRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Exchanges an authorization code for user information.
     *
     * @param code        the authorization code from Google
     * @param redirectUri the redirect URI used in the auth request
     * @return normalized user info
     */
    @SuppressWarnings("unchecked")
    public OAuth2UserInfo exchangeCodeForUserInfo(String code, String redirectUri) {
        // Mock fallback for local development
        if ("mock_google_code".equals(code)) {
            log.warn("Using MOCK Google user profile for local development!");
            return OAuth2UserInfo.builder()
                    .oauthId("mock-google-id-123")
                    .email("admin@mock.com")
                    .name("Mock Admin")
                    .avatarUrl("https://ui-avatars.com/api/?name=Mock+Admin")
                    .provider("GOOGLE")
                    .build();
        }

        log.debug("Exchanging Google authorization code for tokens");

        String effectiveRedirectUri = redirectUri != null ? redirectUri : defaultRedirectUri;

        // Step 1: Exchange code for access token
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", effectiveRedirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                new HttpEntity<>(params, headers),
                Map.class
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");
        if (accessToken == null) {
            throw new OAuth2AuthenticationException("Failed to obtain access token from Google");
        }

        // Step 2: Fetch user profile
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                USER_INFO_URL,
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                Map.class
        );

        Map<String, Object> userInfo = userResponse.getBody();
        log.debug("Google user info retrieved for: {}", userInfo.get("email"));

        return OAuth2UserInfo.builder()
                .oauthId(String.valueOf(userInfo.get("id")))
                .email((String) userInfo.get("email"))
                .name((String) userInfo.get("name"))
                .avatarUrl((String) userInfo.get("picture"))
                .provider("GOOGLE")
                .build();
    }

    /**
     * Returns the Google OAuth2 authorization URL for the given redirect URI.
     */
    public String getAuthorizationUrl(String redirectUri, String state) {
        String effectiveRedirectUri = redirectUri != null ? redirectUri : defaultRedirectUri;

        // Mock fallback for local development
        if (clientId == null || clientId.isBlank() || clientId.contains("mock")) {
            log.warn("Using MOCK Google OAuth2 authorization! Real credentials not configured.");
            return effectiveRedirectUri + "?code=mock_google_code&state=" + state;
        }

        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + effectiveRedirectUri
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline"
                + "&state=" + state;
    }
}
