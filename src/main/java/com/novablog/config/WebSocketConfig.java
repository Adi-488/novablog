package com.novablog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time collaborative editing.
 *
 * <p><b>Sprint 4:</b> Enables STOMP over SockJS for post collaboration.
 * Each post gets a dedicated topic: {@code /topic/post/{postId}}.
 * The in-memory broker is used for Phase 1; upgrade to RabbitMQ
 * STOMP relay in Phase 2 for horizontal scaling.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple in-memory broker for topics and queues
        registry.enableSimpleBroker("/topic", "/queue");
        // Application destination prefix for @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");
        // User-specific destination prefix
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*", "https://*.novablog.dev")
                .withSockJS();
    }
}
