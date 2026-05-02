package com.novablog.post.controller;

import com.novablog.post.dto.*;
import com.novablog.post.model.PostStatus;
import com.novablog.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for blog post management within a tenant.
 *
 * <p>All endpoints require JWT authentication. The tenant context
 * is automatically set from the JWT's tenant_schema claim.</p>
 *
 * <p>Role requirements:
 * <ul>
 *   <li>WRITER+: Create and update own posts</li>
 *   <li>EDITOR+: Update any post, change status, list all posts</li>
 *   <li>ORG_ADMIN+: Delete posts</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Post Management", description = "Blog post CRUD, versioning, and lifecycle management")
@SecurityRequirement(name = "Bearer Authentication")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    /**
     * Creates a new draft post.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create a new post",
            description = "Creates a new blog post in DRAFT status. Requires WRITER+ role.")
    @ApiResponse(responseCode = "201", description = "Post created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Insufficient privileges")
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            Authentication authentication) {

        UUID authorId = (UUID) authentication.getPrincipal();
        PostResponse response = postService.createPost(request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    /**
     * Gets a post by its ID.
     */
    @GetMapping("/{postId}")
    @PreAuthorize("hasAnyRole('READER', 'WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get post by ID")
    @ApiResponse(responseCode = "200", description = "Post found")
    @ApiResponse(responseCode = "404", description = "Post not found")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID postId) {
        PostResponse response = postService.getPostById(postId);
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a post by its slug (used for SEO-friendly URLs).
     */
    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasAnyRole('READER', 'WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get post by slug",
            description = "Retrieves a post using its URL-friendly slug")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        PostResponse response = postService.getPostBySlug(slug);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists posts with pagination and optional status filter.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('READER', 'WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List posts",
            description = "Returns a paginated list of posts. Filter by status with ?status=PUBLISHED")
    public ResponseEntity<Page<PostResponse>> listPosts(
            @RequestParam(required = false) PostStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<PostResponse> posts = postService.listPosts(status, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Lists the current user's posts.
     */
    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "List my posts",
            description = "Returns the authenticated user's posts")
    public ResponseEntity<Page<PostResponse>> listMyPosts(
            @RequestParam(required = false) PostStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            Authentication authentication) {
        UUID authorId = (UUID) authentication.getPrincipal();
        Page<PostResponse> posts = postService.listPostsByAuthor(authorId, status, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Searches published posts by title or body content.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('READER', 'WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Search posts",
            description = "Full-text search across published posts")
    public ResponseEntity<Page<PostResponse>> searchPosts(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> results = postService.searchPosts(q, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Gets posts by tag name.
     */
    @GetMapping("/tag/{tagName}")
    @PreAuthorize("hasAnyRole('READER', 'WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get posts by tag",
            description = "Returns published posts with the specified tag")
    public ResponseEntity<Page<PostResponse>> getPostsByTag(
            @PathVariable String tagName,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.getPostsByTag(tagName, pageable);
        return ResponseEntity.ok(posts);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates a post's content.
     */
    @PutMapping("/{postId}")
    @PreAuthorize("hasAnyRole('WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Update post",
            description = "Updates a DRAFT or UNDER_REVIEW post. Creates a version snapshot.")
    @ApiResponse(responseCode = "200", description = "Post updated")
    @ApiResponse(responseCode = "400", description = "Cannot edit post in current status")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request,
            Authentication authentication) {
        UUID editorId = (UUID) authentication.getPrincipal();
        PostResponse response = postService.updatePost(postId, request, editorId);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // STATUS TRANSITIONS
    // ----------------------------------------------------------------

    /**
     * Changes a post's lifecycle status.
     *
     * <p>Request body: {@code { "status": "UNDER_REVIEW", "scheduledAt": "..." }}</p>
     */
    @PatchMapping("/{postId}/status")
    @PreAuthorize("hasAnyRole('WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Change post status",
            description = "Transitions a post to a new lifecycle status. "
                    + "Valid transitions: DRAFT→UNDER_REVIEW, UNDER_REVIEW→PUBLISHED, etc.")
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "Invalid status transition")
    public ResponseEntity<PostResponse> changeStatus(
            @PathVariable UUID postId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String statusStr = body.get("status");
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        PostStatus newStatus;
        try {
            newStatus = PostStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr
                    + ". Valid values: DRAFT, UNDER_REVIEW, SCHEDULED, PUBLISHED, ARCHIVED");
        }

        OffsetDateTime scheduledAt = null;
        String scheduledAtStr = body.get("scheduledAt");
        if (scheduledAtStr != null && !scheduledAtStr.isBlank()) {
            scheduledAt = OffsetDateTime.parse(scheduledAtStr);
        }

        UUID actorId = (UUID) authentication.getPrincipal();
        PostResponse response = postService.changePostStatus(postId, newStatus, actorId, scheduledAt);
        return ResponseEntity.ok(response);
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    /**
     * Deletes a post (archives first, hard-deletes if already archived).
     */
    @DeleteMapping("/{postId}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Delete post",
            description = "Archives the post (soft-delete). If already archived, permanently deletes.")
    @ApiResponse(responseCode = "204", description = "Post deleted")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID postId,
            Authentication authentication) {
        UUID actorId = (UUID) authentication.getPrincipal();
        postService.deletePost(postId, actorId);
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------
    // VERSION HISTORY
    // ----------------------------------------------------------------

    /**
     * Returns the version history of a post.
     */
    @GetMapping("/{postId}/versions")
    @PreAuthorize("hasAnyRole('WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get post version history",
            description = "Returns all version snapshots of a post, newest first")
    public ResponseEntity<List<PostVersionResponse>> getPostVersions(@PathVariable UUID postId) {
        List<PostVersionResponse> versions = postService.getPostVersions(postId);
        return ResponseEntity.ok(versions);
    }

    /**
     * Gets a specific version of a post.
     */
    @GetMapping("/{postId}/versions/{versionNumber}")
    @PreAuthorize("hasAnyRole('WRITER', 'EDITOR', 'ORG_ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get specific post version",
            description = "Returns a specific version snapshot of a post")
    public ResponseEntity<PostVersionResponse> getPostVersion(
            @PathVariable UUID postId,
            @PathVariable int versionNumber) {
        PostVersionResponse version = postService.getPostVersion(postId, versionNumber);
        return ResponseEntity.ok(version);
    }
}
