package com.novablog.notification.service;

import com.novablog.notification.dto.NotificationResponse;
import com.novablog.notification.model.Notification;
import com.novablog.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing in-app notifications.
 *
 * <p><b>Sprint 5:</b> Creates notifications on post lifecycle events
 * and provides read/unread queries for the notification bell UI.</p>
 */
@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates a notification for a specific user.
     */
    @Transactional
    public Notification createNotification(UUID userId, String type, String title,
                                           String message, String link) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created: type={}, user={}, title={}", type, userId, title);
        return notification;
    }

    /**
     * Lists notifications for a user with pagination.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, int page, int size) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(NotificationResponse::fromEntity);
    }

    /**
     * Lists only unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, int page, int size) {
        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(NotificationResponse::fromEntity);
    }

    /**
     * Returns the count of unread notifications for a user.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    /**
     * Marks a single notification as read.
     */
    @Transactional
    public void markAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    /**
     * Marks all notifications for a user as read.
     */
    @Transactional
    public int markAllAsRead(UUID userId) {
        int count = notificationRepository.markAllAsRead(userId);
        log.info("Marked {} notifications as read for user {}", count, userId);
        return count;
    }
}
