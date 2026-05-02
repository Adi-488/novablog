package com.novablog.post.repository;

import com.novablog.post.model.Post;
import com.novablog.post.model.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for tenant-scoped {@link Post} entities.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByAuthorId(UUID authorId, Pageable pageable);

    Page<Post> findByAuthorIdAndStatus(UUID authorId, PostStatus status, Pageable pageable);

    /**
     * Finds all posts with SCHEDULED status that are due for publishing.
     */
    @Query("SELECT p FROM Post p WHERE p.status = 'SCHEDULED' AND p.publishedAt <= :now")
    List<Post> findScheduledPostsDueForPublishing(@Param("now") OffsetDateTime now);

    /**
     * Searches posts by title or body containing the query string (case-insensitive).
     */
    @Query("SELECT p FROM Post p WHERE p.status = :status AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.body) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Post> searchByTitleOrBody(@Param("query") String query,
                                   @Param("status") PostStatus status,
                                   Pageable pageable);

    /**
     * Finds published posts that have a specific tag.
     */
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tagName AND p.status = :status")
    Page<Post> findByTagNameAndStatus(@Param("tagName") String tagName,
                                      @Param("status") PostStatus status,
                                      Pageable pageable);

    long countByStatus(PostStatus status);
}
