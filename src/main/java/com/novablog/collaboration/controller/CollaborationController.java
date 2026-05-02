package com.novablog.collaboration.controller;

import com.novablog.collaboration.dto.CollabMessage;
import com.novablog.collaboration.dto.PresenceInfo;
import com.novablog.collaboration.service.PresenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * STOMP controller for real-time collaborative post editing.
 *
 * <p><b>Sprint 4:</b> Handles:</p>
 * <ul>
 *   <li>User join/leave presence (US-008)</li>
 *   <li>Content broadcast to all connected editors (US-009)</li>
 *   <li>Cursor position sync</li>
 *   <li>Auto-save triggers every 30 seconds</li>
 * </ul>
 *
 * <p>Topic pattern: {@code /topic/post/{postId}}</p>
 */
@Slf4j
@Controller
public class CollaborationController {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    // Track sessionId → userId for disconnect cleanup
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    // Track sessionId → postId for disconnect cleanup
    private final Map<String, String> sessionPostMap = new ConcurrentHashMap<>();

    public CollaborationController(PresenceService presenceService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles a user joining a post editing session.
     * Broadcasts updated presence list to all editors on the post.
     */
    @MessageMapping("/post/{postId}/join")
    public void handleJoin(@DestinationVariable String postId,
                           CollabMessage message,
                           SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        sessionUserMap.put(sessionId, message.getUserId());
        sessionPostMap.put(sessionId, postId);

        List<PresenceInfo> presence = presenceService.join(
                postId, message.getUserId(), message.getUserName(), message.getAvatarUrl());

        // Broadcast join event with updated presence list
        CollabMessage joinMsg = CollabMessage.builder()
                .type(CollabMessage.Type.JOIN)
                .postId(postId)
                .userId(message.getUserId())
                .userName(message.getUserName())
                .avatarUrl(message.getAvatarUrl())
                .timestamp(System.currentTimeMillis())
                .build();

        messagingTemplate.convertAndSend("/topic/post/" + postId + "/presence", presence);
        messagingTemplate.convertAndSend("/topic/post/" + postId, joinMsg);
    }

    /**
     * Handles a user leaving a post editing session.
     */
    @MessageMapping("/post/{postId}/leave")
    public void handleLeave(@DestinationVariable String postId,
                            CollabMessage message) {
        List<PresenceInfo> presence = presenceService.leave(postId, message.getUserId());

        CollabMessage leaveMsg = CollabMessage.builder()
                .type(CollabMessage.Type.LEAVE)
                .postId(postId)
                .userId(message.getUserId())
                .userName(message.getUserName())
                .timestamp(System.currentTimeMillis())
                .build();

        messagingTemplate.convertAndSend("/topic/post/" + postId + "/presence", presence);
        messagingTemplate.convertAndSend("/topic/post/" + postId, leaveMsg);
    }

    /**
     * Handles content updates (body or title) and broadcasts to all editors.
     * Changes are delivered within 500ms (US-009 acceptance criteria).
     */
    @MessageMapping("/post/{postId}/edit")
    @SendTo("/topic/post/{postId}")
    public CollabMessage handleEdit(@DestinationVariable String postId,
                                    CollabMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        log.debug("Edit broadcast: post={}, user={}, type={}", postId, message.getUserId(), message.getType());
        return message;
    }

    /**
     * Handles cursor position broadcasts.
     */
    @MessageMapping("/post/{postId}/cursor")
    @SendTo("/topic/post/{postId}/cursors")
    public CollabMessage handleCursor(@DestinationVariable String postId,
                                      CollabMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    /**
     * Cleanup on WebSocket disconnect — remove user from presence.
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        String userId = sessionUserMap.remove(sessionId);
        String postId = sessionPostMap.remove(sessionId);

        if (userId != null && postId != null) {
            List<PresenceInfo> presence = presenceService.leave(postId, userId);

            CollabMessage leaveMsg = CollabMessage.builder()
                    .type(CollabMessage.Type.LEAVE)
                    .postId(postId)
                    .userId(userId)
                    .timestamp(System.currentTimeMillis())
                    .build();

            messagingTemplate.convertAndSend("/topic/post/" + postId + "/presence", presence);
            messagingTemplate.convertAndSend("/topic/post/" + postId, leaveMsg);

            log.info("WebSocket disconnect cleanup: user={}, post={}", userId, postId);
        }
    }
}
