package com.novablog.user.service;

import com.novablog.multitenancy.TenantContext;
import com.novablog.security.oauth2.OAuth2UserInfo;
import com.novablog.tenant.model.Role;
import com.novablog.user.dto.UserResponse;
import com.novablog.user.model.OAuthProvider;
import com.novablog.user.model.User;
import com.novablog.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for tenant-scoped user management.
 *
 * <p>Handles user creation on first OAuth login, profile updates,
 * role assignment, and user listing within a tenant.</p>
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates or updates a user based on OAuth2 login info.
     *
     * <p>If a user with the same OAuth provider + ID already exists,
     * their profile is updated. Otherwise, a new user is created
     * with the default WRITER role.</p>
     *
     * <p>If this is the first user in the tenant, they are assigned
     * ORG_ADMIN role automatically.</p>
     *
     * @param userInfo    normalized OAuth2 user info
     * @param tenantSchema the tenant schema to operate in
     * @return the created or updated user
     */
    @Transactional
    public User findOrCreateUser(OAuth2UserInfo userInfo, String tenantSchema) {
        OAuthProvider provider = OAuthProvider.valueOf(userInfo.getProvider());

        // Try to find existing user by OAuth provider + ID
        return userRepository.findByOauthProviderAndOauthId(provider, userInfo.getOauthId())
                .map(existingUser -> {
                    // Update profile fields that may have changed
                    existingUser.setDisplayName(userInfo.getName());
                    existingUser.setAvatarUrl(userInfo.getAvatarUrl());
                    existingUser.setEmail(userInfo.getEmail());
                    log.info("Existing user logged in: {} ({})", existingUser.getEmail(), tenantSchema);
                    return userRepository.save(existingUser);
                })
                .orElseGet(() -> {
                    // Check if this is the first user in the tenant → make them ORG_ADMIN
                    boolean isFirstUser = userRepository.count() == 0;
                    Role assignedRole = isFirstUser ? Role.ORG_ADMIN : Role.WRITER;

                    User newUser = User.builder()
                            .email(userInfo.getEmail())
                            .displayName(userInfo.getName())
                            .avatarUrl(userInfo.getAvatarUrl())
                            .oauthProvider(provider)
                            .oauthId(userInfo.getOauthId())
                            .role(assignedRole)
                            .build();

                    newUser = userRepository.save(newUser);
                    log.info("New user created: {} as {} in tenant {}",
                            newUser.getEmail(), assignedRole, tenantSchema);
                    return newUser;
                });
    }

    /**
     * Finds a user by their ID within the current tenant context.
     */
    @Transactional(readOnly = true)
    public User findById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));
    }

    /**
     * Gets the current user's profile.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = findById(userId);
        return UserResponse.fromEntity(user);
    }

    /**
     * Lists all users in the current tenant.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> listTenantUsers() {
        return userRepository.findAllByOrderByDisplayNameAsc().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Changes a user's role within the current tenant.
     *
     * <p>Only ORG_ADMIN or higher can change roles. A user cannot
     * change their own role or assign a role higher than their own.</p>
     *
     * @param targetUserId the user whose role is being changed
     * @param newRole      the new role to assign
     * @param actorUserId  the user performing the action
     * @param actorRole    the actor's current role
     * @return updated user response
     */
    @Transactional
    public UserResponse changeUserRole(UUID targetUserId, Role newRole,
                                        UUID actorUserId, Role actorRole) {
        // Validate actor has sufficient privileges
        if (!actorRole.isAtLeast(Role.ORG_ADMIN)) {
            throw new SecurityException("Only ORG_ADMIN or higher can change roles");
        }

        // Cannot assign a role higher than your own
        if (newRole.isAbove(actorRole)) {
            throw new SecurityException(
                    "Cannot assign role " + newRole + " — it is higher than your role " + actorRole);
        }

        // Cannot change your own role
        if (targetUserId.equals(actorUserId)) {
            throw new SecurityException("Cannot change your own role");
        }

        User targetUser = findById(targetUserId);

        // Cannot change the role of someone with equal or higher privilege
        if (targetUser.getRole().isAtLeast(actorRole)) {
            throw new SecurityException(
                    "Cannot change role of user with equal or higher privilege");
        }

        log.info("Role change: user {} from {} to {} (by {})",
                targetUserId, targetUser.getRole(), newRole, actorUserId);

        targetUser.setRole(newRole);
        targetUser = userRepository.save(targetUser);
        return UserResponse.fromEntity(targetUser);
    }
}
