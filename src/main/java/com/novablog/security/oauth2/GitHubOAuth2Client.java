package com.novablog.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * OAuth2 client for GitHub authentication.
 *
 * <p>Handles the authorization code exchange and user profile retrieval
 * from GitHub's OAuth2 API. GitHub may require a separate API call
 * to get the user's email if it's private.</p>
 */
@Slf4j
@Component
public class GitHubOAuth2Client {

    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_API_URL = "https://api.github.com/user";
    private static final String EMAILS_API_URL = "https://api.github.com/user/emails";

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret:}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.github.redirect-uri:}")
    private String defaultRedirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Exchanges an authorization code for user information.
     *
     * @param code        the authorization code from GitHub
     * @param redirectUri the redirect URI used in the auth request
     * @return normalized user info
     */
    @SuppressWarnings("unchecked")
    public OAuth2UserInfo exchangeCodeForUserInfo(String code, String redirectUri) {
        // Mock fallback for local development
        if ("mock_github_code".equals(code)) {
            log.warn("Using MOCK GitHub user profile for local development!");
            return OAuth2UserInfo.builder()
                    .oauthId("mock-github-id-456")
                    .email("github-admin@mock.com")
                    .name("Mock GitHub Admin")
                    .avatarUrl("https://ui-avatars.com/api/?name=Mock+GitHub")
                    .provider("GITHUB")
                    .build();
        }

        log.debug("Exchanging GitHub authorization code for tokens");

        String effectiveRedirectUri = redirectUri != null ? redirectUri : defaultRedirectUri;

        // Step 1: Exchange code for access token
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", effectiveRedirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                new HttpEntity<>(params, headers),
                Map.class
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");
        if (accessToken == null) {
            String error = (String) tokenResponse.getBody().get("error_description");
            throw new OAuth2AuthenticationException(
                    "Failed to obtain access token from GitHub: " + error);
        }

        // Step 2: Fetch user profile
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        userHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                USER_API_URL,
                HttpMethod.GET,
                new HttpEntity<>(userHeaders),
                Map.class
        );

        Map<String, Object> userInfo = userResponse.getBody();
        String email = (String) userInfo.get("email");

        // GitHub may not include email in the profile if it's private
        if (email == null || email.isBlank()) {
            email = fetchPrimaryEmail(accessToken);
        }

        log.debug("GitHub user info retrieved for: {}", email);

        return OAuth2UserInfo.builder()
                .oauthId(String.valueOf(userInfo.get("id")))
                .email(email)
                .name((String) userInfo.get("name"))
                .avatarUrl((String) userInfo.get("avatar_url"))
                .provider("GITHUB")
                .build();
    }

    /**
     * Fetches the user's primary verified email from GitHub's emails API.
     */
    @SuppressWarnings("unchecked")
    private String fetchPrimaryEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<List> response = restTemplate.exchange(
                EMAILS_API_URL,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        List<Map<String, Object>> emails = response.getBody();
        if (emails != null) {
            for (Map<String, Object> emailEntry : emails) {
                Boolean primary = (Boolean) emailEntry.get("primary");
                Boolean verified = (Boolean) emailEntry.get("verified");
                if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                    return (String) emailEntry.get("email");
                }
            }
        }

        throw new OAuth2AuthenticationException(
                "Could not retrieve a verified primary email from GitHub");
    }

    /**
     * Returns the GitHub OAuth2 authorization URL.
     */
    public String getAuthorizationUrl(String redirectUri, String state) {
        String effectiveRedirectUri = redirectUri != null ? redirectUri : defaultRedirectUri;

        // Mock fallback for local development
        if (clientId == null || clientId.isBlank() || clientId.contains("mock")) {
            log.warn("Using MOCK GitHub OAuth2 authorization! Real credentials not configured.");
            return effectiveRedirectUri + "?code=mock_github_code&state=" + state;
        }

        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + effectiveRedirectUri
                + "&scope=user:email"
                + "&state=" + state;
    }
}
