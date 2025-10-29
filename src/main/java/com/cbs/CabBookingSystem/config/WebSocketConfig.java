package com.cbs.CabBookingSystem.config;// src/main/java/com/cbs/CabBookingSystem/config/WebSocketConfig.java
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Enables broker for push to /topic/
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Exposes the SockJS endpoint (Angular client uses http://.../rides/ws)
        registry.addEndpoint("/rides/ws")
        .setAllowedOrigins("http://localhost:4200")
        .withSockJS(); // Crucial for cross-browser compatibility
    }
}