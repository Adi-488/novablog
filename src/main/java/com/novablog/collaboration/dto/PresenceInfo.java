package com.novablog.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a user's presence in a post editing session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceInfo {
    private String userId;
    private String userName;
    private String avatarUrl;
    private String color;
    private long joinedAt;
}
