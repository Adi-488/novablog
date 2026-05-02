package com.novablog.post.event;

import com.novablog.multitenancy.TenantContext;
import com.novablog.notification.service.NotificationService;
import com.novablog.post.model.PostStatus;
import com.novablog.user.model.User;
import com.novablog.user.repository.UserRepository;
import com.novablog.tenant.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listener for post lifecycle events.
 *
 * <p><b>Sprint 5 upgrade:</b> Now creates real in-app notifications
 * for post state changes. Async methods run on a separate thread
 * to avoid blocking the main request thread.</p>
 */
@Slf4j
@Component
public class PostEventListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public PostEventListener(NotificationService notificationService,
                             UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    /**
     * Handles post creation events.
     */
    @EventListener
    public void onPostCreated(PostEvent event) {
        if (event.getEventType() != PostEvent.EventType.CREATED) return;
        log.info("📝 Post created: postId={}, author={}",
                event.getPostId(), event.getActorUserId());
    }

    /**
     * Handles post status change events.
     * Creates notifications for relevant users based on the transition.
     */
    @Async
    @EventListener
    public void onPostStatusChanged(PostEvent event) {
        if (event.getEventType() != PostEvent.EventType.STATUS_CHANGED) return;

        log.info("🔄 Post status changed: postId={}, {} → {}, actor={}",
                event.getPostId(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getActorUserId());

        try {
            // DRAFT → UNDER_REVIEW: Notify all Editors
            if (event.getToStatus() == PostStatus.UNDER_REVIEW) {
                List<User> editors = userRepository.findAll().stream()
                        .filter(u -> u.getRole().isAtLeast(Role.EDITOR))
                        .toList();

                for (User editor : editors) {
                    notificationService.createNotification(
                            editor.getId(),
                            "POST_SUBMITTED",
                            "Post submitted for review",
                            "A new post has been submitted and is waiting for your review.",
                            "/editor/" + event.getPostId()
                    );
                }
                log.info("📧 Notified {} editors about post {} submission",
                        editors.size(), event.getPostId());
            }

            // UNDER_REVIEW → PUBLISHED: Notify the author
            else if (event.getToStatus() == PostStatus.PUBLISHED && event.getActorUserId() != null) {
                notificationService.createNotification(
                        event.getActorUserId(),
                        "POST_PUBLISHED",
                        "Your post has been published! 🚀",
                        "Your post is now live and visible to readers.",
                        "/post/" + event.getPostId()
                );
                log.info("🚀 Notified author about post {} publication", event.getPostId());
            }

            // UNDER_REVIEW → DRAFT: Post was rejected
            else if (event.getFromStatus() == PostStatus.UNDER_REVIEW
                     && event.getToStatus() == PostStatus.DRAFT
                     && event.getActorUserId() != null) {
                notificationService.createNotification(
                        event.getActorUserId(),
                        "POST_REJECTED",
                        "Post needs changes",
                        "Your post has been sent back for revisions by an editor.",
                        "/editor/" + event.getPostId()
                );
            }

            // UNDER_REVIEW → SCHEDULED
            else if (event.getToStatus() == PostStatus.SCHEDULED && event.getActorUserId() != null) {
                notificationService.createNotification(
                        event.getActorUserId(),
                        "POST_SCHEDULED",
                        "Post scheduled for publishing",
                        "Your post has been approved and scheduled for automatic publishing.",
                        "/editor/" + event.getPostId()
                );
            }
        } catch (Exception e) {
            // Don't let notification failures break the main flow
            log.error("Failed to create notifications for post event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles post update events.
     */
    @EventListener
    public void onPostUpdated(PostEvent event) {
        if (event.getEventType() != PostEvent.EventType.UPDATED) return;
        log.debug("✏️ Post updated: postId={}, editor={}",
                event.getPostId(), event.getActorUserId());
    }

    /**
     * Handles post deletion events.
     */
    @EventListener
    public void onPostDeleted(PostEvent event) {
        if (event.getEventType() != PostEvent.EventType.DELETED) return;
        log.info("🗑️ Post deleted: postId={}, actor={}",
                event.getPostId(), event.getActorUserId());
    }
}
