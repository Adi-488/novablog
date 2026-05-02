package com.novablog.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * STOMP message DTO for real-time collaborative editing.
 *
 * <p>Carries content changes between connected editors on the same post.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollabMessage {

    public enum Type {
        /** Full content update (body text) */
        CONTENT_UPDATE,
        /** Title change */
        TITLE_UPDATE,
        /** Cursor position broadcast */
        CURSOR,
        /** User joined the editing session */
        JOIN,
        /** User left the editing session */
        LEAVE,
        /** Auto-save acknowledgement */
        AUTOSAVE_ACK,
        /** Conflict notification */
        CONFLICT
    }

    private Type type;
    private String postId;
    private String userId;
    private String userName;
    private String avatarUrl;

    // Content fields
    private String content;
    private String title;

    // Cursor fields
    private Integer cursorPosition;
    private Integer selectionStart;
    private Integer selectionEnd;

    // Metadata
    private long timestamp;
}
