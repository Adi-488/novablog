package com.novablog.media.repository;

import com.novablog.media.model.MediaFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for tenant-scoped media file queries.
 */
@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    Page<MediaFile> findByUploadedByOrderByCreatedAtDesc(UUID uploadedBy, Pageable pageable);

    Page<MediaFile> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<MediaFile> findByPostId(UUID postId);
}
