package com.backend.smarthire.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        // 1. Enable a simple built-in memory broker.
        // Clients (frontend) will subscribe to paths starting with "/topic" to listen for live updates.
        config.enableSimpleBroker("/topic");

        // 2. Paths starting with "/app" are intended for messages sent FROM the client TO the server (if needed)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // This is the initial HTTP connection URL where the frontend establishes the handshake
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allows your React frontend to connect across different ports
                .withSockJS(); // Fallback mechanism if a user's browser doesn't support raw WebSockets
    }

}
