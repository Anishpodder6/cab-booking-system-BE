package com.cbs.CabBookingSystem.config;// src/main/java/com/cbs/CabBookingSystem/config/WebSocketConfig.java
import com.cbs.CabBookingSystem.service.UserDetailsServiceImpl;
import com.cbs.CabBookingSystem.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

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

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                List<String> tokenList = accessor.getNativeHeader("token");
                System.out.println("Got Socket Token: " + tokenList.get(0));
                if(tokenList.isEmpty()) {
                    String token = tokenList.get(0);
                    String username = jwtUtil.extractUsername(token);

                    if (jwtUtil.validateToken(token, username)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        accessor.setUser(auth);
                    }
                }

                return message;
            }
        });
    }
}