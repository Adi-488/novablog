package com.novablog.notification.controller;

import com.novablog.notification.dto.NotificationResponse;
import com.novablog.notification.service.NotificationService;
import com.novablog.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for in-app notifications.
 *
 * <p><b>Sprint 5:</b> Endpoints for listing, counting, and marking
 * notifications as read. Used by the frontend notification bell.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    public NotificationController(NotificationService notificationService,
                                  JwtTokenProvider jwtTokenProvider) {
        this.notificationService = notificationService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Lists notifications for the current user.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> listNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            HttpServletRequest request) {

        UUID userId = extractUserId(request);
        Page<NotificationResponse> notifications = unreadOnly
                ? notificationService.getUnreadNotifications(userId, page, size)
                : notificationService.getUserNotifications(userId, page, size);

        return ResponseEntity.ok(notifications);
    }

    /**
     * Returns the unread notification count for the notification badge.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        UUID userId = extractUserId(request);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * Marks a single notification as read.
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks all notifications for the current user as read.
     */
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(HttpServletRequest request) {
        UUID userId = extractUserId(request);
        int count = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("markedRead", count));
    }

    private UUID extractUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return jwtTokenProvider.getUserIdFromToken(token);
    }
}
