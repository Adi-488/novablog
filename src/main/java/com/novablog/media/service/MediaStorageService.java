package com.novablog.media.service;

import com.novablog.media.dto.MediaFileResponse;
import com.novablog.media.model.MediaFile;
import com.novablog.media.repository.MediaFileRepository;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

/**
 * AWS S3 media storage service.
 *
 * <p>Stores uploaded files in S3 under a tenant-specific prefix.</p>
 */
@Slf4j
@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "application/pdf", "video/mp4"
    );

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;   // 10 MB
    private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024;   // 50 MB

    private final MediaFileRepository mediaFileRepository;
    private final S3Template s3Template;
    private final String bucketName;

    public MediaStorageService(
            MediaFileRepository mediaFileRepository,
            S3Template s3Template,
            @Value("${novablog.media.s3-bucket}") String bucketName) {
        this.mediaFileRepository = mediaFileRepository;
        this.s3Template = s3Template;
        this.bucketName = bucketName;
    }

    @Transactional
    public MediaFileResponse uploadFile(MultipartFile file, String tenantSchema,
                                         UUID uploadedBy, UUID postId, String altText,
                                         String baseUrl) throws IOException {
        validateFile(file);

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID() + "-" + sanitizeFileName(originalName);

        // Store in S3 with key prefix: {tenantSchema}/{storedName}
        String s3Key = tenantSchema + "/" + storedName;

        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucketName, s3Key, inputStream);
        }

        MediaFile mediaFile = MediaFile.builder()
                .originalName(originalName)
                .storedName(storedName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .storagePath(s3Key) // We save the S3 key in the database
                .uploadedBy(uploadedBy)
                .postId(postId)
                .altText(altText)
                .build();

        mediaFile = mediaFileRepository.save(mediaFile);
        log.info("Media uploaded to S3: id={}, key={}, bucket={}",
                mediaFile.getId(), s3Key, bucketName);

        return MediaFileResponse.fromEntity(mediaFile, baseUrl);
    }

    @Transactional(readOnly = true)
    public Page<MediaFileResponse> listFiles(int page, int size, String baseUrl) {
        return mediaFileRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(m -> MediaFileResponse.fromEntity(m, baseUrl));
    }

    @Transactional(readOnly = true)
    public MediaFile getFileById(UUID fileId) {
        return mediaFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Media file not found: " + fileId));
    }

    public byte[] readFileBytes(MediaFile mediaFile) throws IOException {
        String s3Key = mediaFile.getStoragePath();
        
        S3Resource s3Resource = s3Template.download(bucketName, s3Key);
        if (!s3Resource.exists()) {
            throw new IllegalStateException("File not found in S3: " + s3Key);
        }
        
        try (InputStream is = s3Resource.getInputStream()) {
            return is.readAllBytes();
        }
    }

    @Transactional
    public void deleteFile(UUID fileId) throws IOException {
        MediaFile mediaFile = getFileById(fileId);
        String s3Key = mediaFile.getStoragePath();

        s3Template.deleteObject(bucketName, s3Key);

        mediaFileRepository.delete(mediaFile);
        log.info("Media deleted from S3: id={}, key={}", fileId, s3Key);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported file type: " + contentType
                    + ". Allowed: " + ALLOWED_TYPES);
        }

        long maxSize = contentType.startsWith("video/") ? MAX_VIDEO_SIZE : MAX_IMAGE_SIZE;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "File too large: " + (file.getSize() / 1024 / 1024) + "MB. "
                    + "Maximum: " + (maxSize / 1024 / 1024) + "MB");
        }
    }

    private String sanitizeFileName(String name) {
        if (name == null) return "unnamed";
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
