package com.novablog.user.controller;

import com.novablog.tenant.model.Role;
import com.novablog.user.dto.UserResponse;
import com.novablog.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for user management within a tenant.
 *
 * <p>All endpoints require JWT authentication. The tenant context
 * is automatically set from the JWT's tenant_schema claim.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "Tenant-scoped user profiles and role management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the authenticated user's profile.
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile",
            description = "Returns the authenticated user's profile in the current tenant")
    @ApiResponse(responseCode = "200", description = "User profile returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UserResponse response = userService.getCurrentUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all users in the current tenant.
     * Requires EDITOR role or higher.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List tenant members",
            description = "Returns all users in the current tenant. Requires EDITOR+ role.")
    @ApiResponse(responseCode = "200", description = "User list returned")
    @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userService.listTenantUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Changes a user's role within the current tenant.
     * Requires ORG_ADMIN role or higher.
     *
     * @param userId the target user's ID
     * @param body   request body containing "role" field
     */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAnyRole('ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Change user role",
            description = "Assigns a new role to a user in the current tenant. "
                    + "Requires ORG_ADMIN+ role. Cannot assign role higher than your own.")
    @ApiResponse(responseCode = "200", description = "Role updated")
    @ApiResponse(responseCode = "400", description = "Invalid role")
    @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String roleString = body.get("role");
        if (roleString == null || roleString.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }

        Role newRole;
        try {
            newRole = Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleString
                    + ". Valid roles: READER, WRITER, EDITOR, ORG_ADMIN");
        }

        UUID actorUserId = (UUID) authentication.getPrincipal();
        // Extract role from authorities (format: ROLE_ORG_ADMIN → ORG_ADMIN)
        String actorRoleStr = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .map(a -> a.substring(5))
                .orElseThrow(() -> new SecurityException("No role found in token"));

        Role actorRole = Role.valueOf(actorRoleStr);

        UserResponse response = userService.changeUserRole(userId, newRole, actorUserId, actorRole);
        return ResponseEntity.ok(response);
    }
}
