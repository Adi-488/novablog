package com.novablog.user.model;

import com.novablog.common.audit.AuditableEntity;
import com.novablog.tenant.model.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity representing a user within a tenant's schema.
 *
 * <p>Each tenant has its own {@code users} table in its isolated PostgreSQL schema.
 * Users are created on first OAuth2 login and are assigned the default WRITER role.</p>
 *
 * <p>A user can exist in multiple tenant schemas with different roles in each.</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 20)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_id")
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.WRITER;
}
