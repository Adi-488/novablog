package com.novablog.post.service;

import com.novablog.post.dto.*;
import com.novablog.post.event.PostEvent;
import com.novablog.post.model.*;
import com.novablog.post.repository.*;
import com.novablog.user.model.User;
import com.novablog.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for post management within a tenant schema.
 *
 * <p>Handles:
 * <ul>
 *   <li>Post CRUD operations with slug generation</li>
 *   <li>Lifecycle state transitions with validation (US-005, US-006)</li>
 *   <li>Version snapshotting on every content update</li>
 *   <li>Tag management (create-on-demand)</li>
 *   <li>Spring Application Events on state changes</li>
 * </ul>
 */
@Slf4j
@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostVersionRepository versionRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PostService(PostRepository postRepository,
                       PostVersionRepository versionRepository,
                       TagRepository tagRepository,
                       UserRepository userRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.versionRepository = versionRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // ----------------------------------------------------------------
    // CREATE
    // ----------------------------------------------------------------

    /**
     * Creates a new draft post.
     *
     * @param request  the creation request
     * @param authorId the authenticated user's ID
     * @return the created post response
     */
    @Transactional
    public PostResponse createPost(CreatePostRequest request, UUID authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found: " + authorId));

        // Generate slug from title if not provided
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? slugify(request.getSlug())
                : slugify(request.getTitle());

        // Ensure slug uniqueness within the tenant
        slug = ensureUniqueSlug(slug);

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(slug)
                .body(request.getBody())
                .status(PostStatus.DRAFT)
                .author(author)
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .build();

        // Resolve tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = resolveTags(request.getTags());
            post.setTags(tags);
        }

        post = postRepository.save(post);
        log.info("Post created: id={}, slug={}, author={}", post.getId(), slug, authorId);

        // Create initial version snapshot
        createVersionSnapshot(post, authorId);

        // Publish event
        eventPublisher.publishEvent(PostEvent.created(this, post.getId(), authorId));

        return PostResponse.fromEntity(post);
    }

    // ----------------------------------------------------------------
    // READ
    // ----------------------------------------------------------------

    /**
     * Gets a post by its ID.
     */
    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID postId) {
        Post post = findPostOrThrow(postId);
        return PostResponse.fromEntity(post);
    }

    /**
     * Gets a post by its slug (used for public URLs).
     */
    @Transactional(readOnly = true)
    public PostResponse getPostBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + slug));
        return PostResponse.fromEntity(post);
    }

    /**
     * Lists posts with pagination and optional status filter.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> listPosts(PostStatus status, Pageable pageable) {
        Page<Post> posts = status != null
                ? postRepository.findByStatus(status, pageable)
                : postRepository.findAll(pageable);
        return posts.map(PostResponse::fromEntity);
    }

    /**
     * Lists posts by a specific author.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> listPostsByAuthor(UUID authorId, PostStatus status,
                                                 Pageable pageable) {
        Page<Post> posts = status != null
                ? postRepository.findByAuthorIdAndStatus(authorId, status, pageable)
                : postRepository.findByAuthorId(authorId, pageable);
        return posts.map(PostResponse::fromEntity);
    }

    /**
     * Searches published posts by title or body content.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        return postRepository.searchByTitleOrBody(query, PostStatus.PUBLISHED, pageable)
                .map(PostResponse::fromEntity);
    }

    /**
     * Finds published posts with a specific tag.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByTag(String tagName, Pageable pageable) {
        return postRepository.findByTagNameAndStatus(tagName, PostStatus.PUBLISHED, pageable)
                .map(PostResponse::fromEntity);
    }

    // ----------------------------------------------------------------
    // UPDATE
    // ----------------------------------------------------------------

    /**
     * Updates a post's content. Creates a version snapshot of the previous state.
     *
     * <p>Only the author can update a DRAFT post. Editors can update any post
     * that is in DRAFT or UNDER_REVIEW status.</p>
     *
     * @param postId   the post to update
     * @param request  the update request (partial — only provided fields are changed)
     * @param editorId the authenticated user performing the update
     * @return the updated post response
     */
    @Transactional
    public PostResponse updatePost(UUID postId, UpdatePostRequest request, UUID editorId) {
        Post post = findPostOrThrow(postId);

        // Only allow edits on DRAFT or UNDER_REVIEW posts
        if (post.getStatus() != PostStatus.DRAFT && post.getStatus() != PostStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Cannot edit post in " + post.getStatus() + " status. "
                    + "Only DRAFT and UNDER_REVIEW posts can be edited.");
        }

        // Track if content actually changed
        boolean contentChanged = false;

        if (request.getTitle() != null && !request.getTitle().equals(post.getTitle())) {
            post.setTitle(request.getTitle());
            contentChanged = true;
        }
        if (request.getBody() != null && !request.getBody().equals(post.getBody())) {
            post.setBody(request.getBody());
            contentChanged = true;
        }
        if (request.getSeoTitle() != null) {
            post.setSeoTitle(request.getSeoTitle());
        }
        if (request.getSeoDescription() != null) {
            post.setSeoDescription(request.getSeoDescription());
        }
        if (request.getTags() != null) {
            post.setTags(resolveTags(request.getTags()));
        }

        post = postRepository.save(post);

        // Create version snapshot only if content (title/body) changed
        if (contentChanged) {
            createVersionSnapshot(post, editorId);
        }

        eventPublisher.publishEvent(PostEvent.updated(this, post.getId(), editorId));
        log.info("Post updated: id={}, by={}", postId, editorId);

        return PostResponse.fromEntity(post);
    }

    // ----------------------------------------------------------------
    // STATUS TRANSITIONS
    // ----------------------------------------------------------------

    /**
     * Transitions a post to a new status with validation.
     *
     * @param postId    the post ID
     * @param newStatus the target status
     * @param actorId   the user performing the transition
     * @param scheduledAt optional scheduled publish time (for SCHEDULED status)
     * @return the updated post
     */
    @Transactional
    public PostResponse changePostStatus(UUID postId, PostStatus newStatus,
                                          UUID actorId, OffsetDateTime scheduledAt) {
        Post post = findPostOrThrow(postId);
        PostStatus oldStatus = post.getStatus();

        // Validate transition
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition: " + oldStatus + " → " + newStatus);
        }

        post.setStatus(newStatus);

        // Handle publish-specific logic
        if (newStatus == PostStatus.PUBLISHED) {
            post.setPublishedAt(OffsetDateTime.now());
        } else if (newStatus == PostStatus.SCHEDULED) {
            if (scheduledAt == null) {
                throw new IllegalArgumentException(
                        "Scheduled publish time is required for SCHEDULED status");
            }
            if (scheduledAt.isBefore(OffsetDateTime.now())) {
                throw new IllegalArgumentException(
                        "Scheduled publish time must be in the future");
            }
            post.setPublishedAt(scheduledAt);
        }

        post = postRepository.save(post);

        // Publish state change event
        eventPublisher.publishEvent(
                PostEvent.statusChanged(this, post.getId(), actorId, oldStatus, newStatus));

        log.info("Post status changed: id={}, {} → {}, by={}",
                postId, oldStatus, newStatus, actorId);

        return PostResponse.fromEntity(post);
    }

    // ----------------------------------------------------------------
    // DELETE
    // ----------------------------------------------------------------

    /**
     * Soft-deletes a post by archiving it, or hard-deletes if already archived.
     */
    @Transactional
    public void deletePost(UUID postId, UUID actorId) {
        Post post = findPostOrThrow(postId);

        if (post.getStatus() == PostStatus.ARCHIVED) {
            // Hard delete if already archived
            postRepository.delete(post);
            log.info("Post hard-deleted: id={}, by={}", postId, actorId);
        } else {
            // Soft delete → archive
            PostStatus oldStatus = post.getStatus();
            post.setStatus(PostStatus.ARCHIVED);
            postRepository.save(post);
            eventPublisher.publishEvent(
                    PostEvent.statusChanged(this, postId, actorId, oldStatus, PostStatus.ARCHIVED));
            log.info("Post archived (soft-delete): id={}, by={}", postId, actorId);
        }

        eventPublisher.publishEvent(PostEvent.deleted(this, postId, actorId));
    }

    // ----------------------------------------------------------------
    // VERSION HISTORY
    // ----------------------------------------------------------------

    /**
     * Returns the version history for a post (newest first).
     */
    @Transactional(readOnly = true)
    public List<PostVersionResponse> getPostVersions(UUID postId) {
        findPostOrThrow(postId); // Verify post exists
        return versionRepository.findByPostIdOrderByVersionNumberDesc(postId).stream()
                .map(PostVersionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Gets a specific version snapshot of a post.
     */
    @Transactional(readOnly = true)
    public PostVersionResponse getPostVersion(UUID postId, int versionNumber) {
        return versionRepository.findByPostIdAndVersionNumber(postId, versionNumber)
                .map(PostVersionResponse::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Version " + versionNumber + " not found for post " + postId));
    }

    // ----------------------------------------------------------------
    // SCHEDULED PUBLISHING
    // ----------------------------------------------------------------

    /**
     * Publishes all posts that are past their scheduled publish time.
     * Called by the {@code ScheduledPostPublisher} cron job.
     *
     * @return number of posts published
     */
    @Transactional
    public int publishScheduledPosts() {
        List<Post> duePosts = postRepository.findScheduledPostsDueForPublishing(
                OffsetDateTime.now());

        for (Post post : duePosts) {
            PostStatus oldStatus = post.getStatus();
            post.setStatus(PostStatus.PUBLISHED);
            post.setPublishedAt(OffsetDateTime.now());
            postRepository.save(post);

            eventPublisher.publishEvent(
                    PostEvent.statusChanged(this, post.getId(), null, oldStatus, PostStatus.PUBLISHED));

            log.info("Scheduled post auto-published: id={}, slug={}",
                    post.getId(), post.getSlug());
        }

        if (!duePosts.isEmpty()) {
            log.info("Published {} scheduled post(s)", duePosts.size());
        }

        return duePosts.size();
    }

    // ----------------------------------------------------------------
    // HELPER METHODS
    // ----------------------------------------------------------------

    private Post findPostOrThrow(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found: " + postId));
    }

    /**
     * Creates an immutable version snapshot of the current post state.
     */
    private void createVersionSnapshot(Post post, UUID createdBy) {
        int nextVersion = versionRepository.findMaxVersionNumber(post.getId()) + 1;

        PostVersion version = PostVersion.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .versionNumber(nextVersion)
                .createdBy(createdBy)
                .build();

        versionRepository.save(version);
        log.debug("Version snapshot created: postId={}, v{}", post.getId(), nextVersion);
    }

    /**
     * Resolves tag names into Tag entities, creating new tags on demand.
     */
    private Set<Tag> resolveTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            String normalized = name.trim().toLowerCase();
            if (normalized.isEmpty()) continue;

            Tag tag = tagRepository.findByName(normalized)
                    .orElseGet(() -> {
                        Tag newTag = Tag.builder().name(normalized).build();
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }

    /**
     * Converts a string to a URL-friendly slug.
     */
    String slugify(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")  // Remove special chars
                .replaceAll("\\s+", "-")           // Replace spaces with hyphens
                .replaceAll("-+", "-")             // Collapse multiple hyphens
                .replaceAll("^-|-$", "");          // Trim leading/trailing hyphens
    }

    /**
     * Ensures slug uniqueness by appending a suffix if needed.
     */
    private String ensureUniqueSlug(String slug) {
        if (!postRepository.existsBySlug(slug)) {
            return slug;
        }

        int suffix = 2;
        String candidate;
        do {
            candidate = slug + "-" + suffix;
            suffix++;
        } while (postRepository.existsBySlug(candidate));

        return candidate;
    }
}
