package com.novablog.post.repository;

import com.novablog.post.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant-scoped tags.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByName(String name);

    boolean existsByName(String name);

    List<Tag> findAllByOrderByNameAsc();
}
