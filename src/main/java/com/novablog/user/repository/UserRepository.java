package com.novablog.user.repository;

import com.novablog.user.model.OAuthProvider;
import com.novablog.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for the tenant-scoped {@link User} entity.
 *
 * <p>Queries execute against the current tenant's schema as determined
 * by {@link com.novablog.multitenancy.TenantContext}.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByOauthProviderAndOauthId(OAuthProvider provider, String oauthId);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByDisplayNameAsc();
}
