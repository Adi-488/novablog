package com.novablog.media.controller;

import com.novablog.media.dto.MediaFileResponse;
import com.novablog.media.model.MediaFile;
import com.novablog.media.service.MediaStorageService;
import com.novablog.multitenancy.TenantContext;
import com.novablog.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * REST controller for media file management.
 *
 * <p><b>Sprint 6:</b> Handles file upload, listing, download, and deletion.
 * All operations are tenant-scoped via the X-Tenant-ID header.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaStorageService storageService;
    private final JwtTokenProvider jwtTokenProvider;

    public MediaController(MediaStorageService storageService,
                           JwtTokenProvider jwtTokenProvider) {
        this.storageService = storageService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Upload a media file.
     *
     * <p>Supports drag-and-drop and file picker. Validates file type
     * and size per US-010 acceptance criteria.</p>
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaFileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "postId", required = false) UUID postId,
            @RequestParam(value = "altText", required = false) String altText,
            HttpServletRequest request) throws IOException {

        UUID userId = extractUserId(request);
        String tenantSchema = TenantContext.getTenantId();
        String baseUrl = getBaseUrl(request);

        MediaFileResponse response = storageService.uploadFile(
                file, tenantSchema, userId, postId, altText, baseUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * List all media files for the current tenant.
     */
    @GetMapping
    public ResponseEntity<Page<MediaFileResponse>> listFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        String baseUrl = getBaseUrl(request);
        Page<MediaFileResponse> files = storageService.listFiles(page, size, baseUrl);
        return ResponseEntity.ok(files);
    }

    /**
     * Download a media file by its ID.
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable UUID fileId) throws IOException {
        MediaFile mediaFile = storageService.getFileById(fileId);
        byte[] bytes = storageService.readFileBytes(mediaFile);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + mediaFile.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                .contentLength(mediaFile.getFileSize())
                .body(bytes);
    }

    /**
     * Delete a media file.
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) throws IOException {
        storageService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }

    private String getBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();
    }
}
