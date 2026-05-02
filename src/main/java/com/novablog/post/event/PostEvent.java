package com.novablog.post.event;

import com.novablog.post.model.PostStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Base event for all post lifecycle state changes.
 *
 * <p>Published via Spring's {@code ApplicationEventPublisher} whenever
 * a post transitions between states in the lifecycle.</p>
 */
@Getter
public class PostEvent extends ApplicationEvent {

    private final UUID postId;
    private final UUID tenantId;
    private final UUID actorUserId;
    private final PostStatus fromStatus;
    private final PostStatus toStatus;
    private final EventType eventType;

    public enum EventType {
        CREATED,
        UPDATED,
        STATUS_CHANGED,
        DELETED
    }

    public PostEvent(Object source, UUID postId, UUID tenantId, UUID actorUserId,
                     PostStatus fromStatus, PostStatus toStatus, EventType eventType) {
        super(source);
        this.postId = postId;
        this.tenantId = tenantId;
        this.actorUserId = actorUserId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.eventType = eventType;
    }

    /**
     * Factory method for post creation events.
     */
    public static PostEvent created(Object source, UUID postId, UUID actorUserId) {
        return new PostEvent(source, postId, null, actorUserId,
                null, PostStatus.DRAFT, EventType.CREATED);
    }

    /**
     * Factory method for post status change events.
     */
    public static PostEvent statusChanged(Object source, UUID postId, UUID actorUserId,
                                            PostStatus from, PostStatus to) {
        return new PostEvent(source, postId, null, actorUserId,
                from, to, EventType.STATUS_CHANGED);
    }

    /**
     * Factory method for post update events.
     */
    public static PostEvent updated(Object source, UUID postId, UUID actorUserId) {
        return new PostEvent(source, postId, null, actorUserId,
                null, null, EventType.UPDATED);
    }

    /**
     * Factory method for post deletion events.
     */
    public static PostEvent deleted(Object source, UUID postId, UUID actorUserId) {
        return new PostEvent(source, postId, null, actorUserId,
                null, null, EventType.DELETED);
    }
}
