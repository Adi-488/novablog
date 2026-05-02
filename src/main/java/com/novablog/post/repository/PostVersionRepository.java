package com.novablog.post.repository;

import com.novablog.post.model.PostVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for post version history (immutable snapshots).
 */
@Repository
public interface PostVersionRepository extends JpaRepository<PostVersion, UUID> {

    List<PostVersion> findByPostIdOrderByVersionNumberDesc(UUID postId);

    Optional<PostVersion> findByPostIdAndVersionNumber(UUID postId, int versionNumber);

    @Query("SELECT COALESCE(MAX(pv.versionNumber), 0) FROM PostVersion pv WHERE pv.postId = :postId")
    int findMaxVersionNumber(@Param("postId") UUID postId);
}
