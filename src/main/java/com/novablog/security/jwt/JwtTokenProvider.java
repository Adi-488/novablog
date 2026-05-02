package com.novablog.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * JWT token provider for generating and validating access and refresh tokens.
 *
 * <p>Token structure:
 * <ul>
 *   <li><b>Access token</b> (15 min): sub=userId, email, role, tenant_schema, tenant_subdomain</li>
 *   <li><b>Refresh token</b> (7 days): sub=userId, tenant_schema, type=refresh</li>
 * </ul>
 *
 * <p>Uses HMAC-SHA256 signing algorithm with a configurable secret key.</p>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        try {
            // Try Base64 decoding first
            keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        } catch (IllegalArgumentException e) {
            // Fallback to raw string bytes if they didn't provide a valid base64 string
            keyBytes = jwtProperties.getSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        // HMAC-SHA256 requires at least 256 bits (32 bytes). Pad if necessary to prevent startup crashes.
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT signing key initialized (algorithm: HmacSHA256)");
    }

    /**
     * Generates an access token for the given user.
     *
     * @param userId          the user's UUID
     * @param email           the user's email
     * @param role            the user's role in the tenant
     * @param tenantSchema    the tenant's PostgreSQL schema name
     * @param tenantSubdomain the tenant's subdomain
     * @return signed JWT access token
     */
    public String generateAccessToken(UUID userId, String email, String role,
                                       String tenantSchema, String tenantSubdomain) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .claim("tenant_schema", tenantSchema)
                .claim("tenant_subdomain", tenantSubdomain)
                .claim("type", "access")
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generates a refresh token for the given user.
     *
     * @param userId       the user's UUID
     * @param tenantSchema the tenant's PostgreSQL schema name
     * @return signed JWT refresh token
     */
    public String generateRefreshToken(UUID userId, String tenantSchema) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
                .subject(userId.toString())
                .claim("tenant_schema", tenantSchema)
                .claim("type", "refresh")
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates a JWT token, returning its claims.
     *
     * @param token the JWT token string
     * @return parsed claims
     * @throws JwtException if the token is invalid, expired, or tampered with
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtProperties.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Validates a token without throwing exceptions.
     *
     * @param token the JWT token string
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the user ID (subject) from a token.
     */
    public UUID getUserIdFromToken(String token) {
        return UUID.fromString(parseToken(token).getSubject());
    }

    /**
     * Extracts the tenant schema from a token.
     */
    public String getTenantSchemaFromToken(String token) {
        return parseToken(token).get("tenant_schema", String.class);
    }

    /**
     * Extracts the role from a token.
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    /**
     * Extracts the token type (access/refresh) from a token.
     */
    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }

    /**
     * Returns access token expiration in milliseconds.
     */
    public long getAccessTokenExpirationMs() {
        return jwtProperties.getAccessTokenExpirationMs();
    }
}
