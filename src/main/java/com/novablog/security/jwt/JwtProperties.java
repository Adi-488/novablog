package com.novablog.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT token management.
 *
 * <p>Properties are bound from {@code novablog.jwt.*} in application.yml.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "novablog.jwt")
public class JwtProperties {

    /**
     * Base64-encoded HMAC-SHA256 secret key for signing JWT tokens.
     * MUST be at least 32 bytes (256 bits) when decoded.
     */
    private String secret;

    /**
     * Access token expiration in milliseconds. Default: 15 minutes.
     */
    private long accessTokenExpirationMs = 900_000; // 15 min

    /**
     * Refresh token expiration in milliseconds. Default: 7 days.
     */
    private long refreshTokenExpirationMs = 604_800_000; // 7 days

    /**
     * JWT issuer claim value.
     */
    private String issuer = "novablog";
}
