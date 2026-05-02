package com.novablog.collaboration.service;

import com.novablog.collaboration.dto.PresenceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory presence tracking service for collaborative editing sessions.
 *
 * <p>Tracks which users are currently editing each post. Users appear
 * within 2 seconds of opening the editor and disappear within 5 seconds
 * of disconnecting (per US-008 acceptance criteria).</p>
 *
 * <p><b>Phase 1:</b> In-memory ConcurrentHashMap. Phase 2 upgrade to Redis
 * for multi-instance deployments.</p>
 */
@Slf4j
@Service
public class PresenceService {

    // postId → Map<userId, PresenceInfo>
    private final Map<String, Map<String, PresenceInfo>> postSessions = new ConcurrentHashMap<>();

    private static final String[] CURSOR_COLORS = {
            "#3B82F6", "#EF4444", "#10B981", "#F59E0B",
            "#8B5CF6", "#EC4899", "#06B6D4", "#F97316"
    };

    /**
     * Registers a user as present on a post editing session.
     *
     * @param postId    the post being edited
     * @param userId    the user joining
     * @param userName  display name
     * @param avatarUrl avatar URL
     * @return list of all users currently present on the post
     */
    public List<PresenceInfo> join(String postId, String userId, String userName, String avatarUrl) {
        Map<String, PresenceInfo> sessions = postSessions.computeIfAbsent(postId, k -> new ConcurrentHashMap<>());

        // Assign a cursor color based on position
        String color = CURSOR_COLORS[sessions.size() % CURSOR_COLORS.length];

        PresenceInfo info = PresenceInfo.builder()
                .userId(userId)
                .userName(userName)
                .avatarUrl(avatarUrl)
                .color(color)
                .joinedAt(System.currentTimeMillis())
                .build();

        sessions.put(userId, info);
        log.info("User {} joined post {} editing session ({} users)", userName, postId, sessions.size());

        return new ArrayList<>(sessions.values());
    }

    /**
     * Removes a user from a post editing session.
     *
     * @param postId the post being edited
     * @param userId the user leaving
     * @return updated list of present users
     */
    public List<PresenceInfo> leave(String postId, String userId) {
        Map<String, PresenceInfo> sessions = postSessions.get(postId);
        if (sessions != null) {
            PresenceInfo removed = sessions.remove(userId);
            if (removed != null) {
                log.info("User {} left post {} editing session ({} users remaining)",
                        removed.getUserName(), postId, sessions.size());
            }
            // Cleanup empty sessions
            if (sessions.isEmpty()) {
                postSessions.remove(postId);
            }
            return new ArrayList<>(sessions.values());
        }
        return Collections.emptyList();
    }

    /**
     * Returns the list of users currently editing a post.
     */
    public List<PresenceInfo> getPresence(String postId) {
        Map<String, PresenceInfo> sessions = postSessions.get(postId);
        return sessions != null ? new ArrayList<>(sessions.values()) : Collections.emptyList();
    }

    /**
     * Removes a user from ALL post sessions (e.g., on WebSocket disconnect).
     */
    public void removeFromAll(String userId) {
        for (Map.Entry<String, Map<String, PresenceInfo>> entry : postSessions.entrySet()) {
            entry.getValue().remove(userId);
            if (entry.getValue().isEmpty()) {
                postSessions.remove(entry.getKey());
            }
        }
        log.debug("User {} removed from all editing sessions", userId);
    }
}
