package com.novablog.security.jwt;

import com.novablog.multitenancy.TenantContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT authentication filter that processes Bearer tokens from the Authorization header.
 *
 * <p>This filter:
 * <ol>
 *   <li>Extracts the JWT from the {@code Authorization: Bearer <token>} header</li>
 *   <li>Validates the token signature and expiration</li>
 *   <li>Extracts user claims (userId, email, role, tenant)</li>
 *   <li>Sets the {@link SecurityContextHolder} with the authenticated user</li>
 *   <li>Sets the {@link TenantContext} from the JWT's tenant_schema claim</li>
 * </ol>
 *
 * <p>This filter is added BEFORE Spring Security's UsernamePasswordAuthenticationFilter.</p>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = extractJwtFromRequest(request);

        if (StringUtils.hasText(jwt)) {
            try {
                Claims claims = jwtTokenProvider.parseToken(jwt);

                // Only process access tokens (not refresh tokens)
                String tokenType = claims.get("type", String.class);
                if (!"access".equals(tokenType)) {
                    log.debug("Ignoring non-access token type: {}", tokenType);
                    filterChain.doFilter(request, response);
                    return;
                }

                UUID userId = UUID.fromString(claims.getSubject());
                String email = claims.get("email", String.class);
                String role = claims.get("role", String.class);
                String tenantSchema = claims.get("tenant_schema", String.class);

                // Set tenant context from JWT
                if (StringUtils.hasText(tenantSchema)) {
                    TenantContext.setTenantId(tenantSchema);
                }

                // Build Spring Security authentication
                // Role is prefixed with ROLE_ for Spring Security's hasRole() checks
                List<SimpleGrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,   // principal = userId
                                email,    // credentials = email (for convenience)
                                authorities
                        );
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT authenticated: userId={}, role={}, tenant={}",
                        userId, role, tenantSchema);

            } catch (JwtException | IllegalArgumentException e) {
                log.debug("JWT validation failed: {}", e.getMessage());
                // Don't set authentication — request proceeds as anonymous
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token string, or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
