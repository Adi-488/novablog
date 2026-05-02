package com.novablog.config;

import com.novablog.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for NovaBlog.
 *
 * <p><b>Sprint 2:</b> Integrates JWT-based authentication with OAuth2 social login.
 * Stateless sessions, CSRF disabled, CORS enabled for frontend clients.
 * Role-based access is enforced via {@code @PreAuthorize} annotations on controllers.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize, @Secured, @RolesAllowed
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless API
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session — JWT handles auth
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Disable Spring's built-in OAuth2 login (we handle it manually)
                .oauth2Login(oauth2 -> oauth2.disable())

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — no authentication required
                        .requestMatchers(
                                "/api/v1/tenants/register",
                                "/api/v1/tenants/check",
                                "/api/v1/tenants/{subdomain}",
                                "/api/v1/auth/**",
                                "/api/v1/media/*/download",
                                "/ws/**",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/docs/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // OPTIONS requests (CORS preflight) are always permitted
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter before Spring's default auth filter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration allowing frontend clients to access the API.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://*.novablog.dev"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
