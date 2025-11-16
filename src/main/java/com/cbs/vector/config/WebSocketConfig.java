package com.cbs.vector.config;// src/main/java/com/cbs/CabBookingSystem/config/WebSocketConfig.java
import com.cbs.vector.service.UserDetailsServiceImpl;
import com.cbs.vector.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

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

                // Check for the type of STOMP command/frame
                switch (accessor.getCommand()) {
                    case CONNECT:
                        System.out.println(">> STOMP CONNECT: A client is attempting to connect.");
                        // You would put your JWT authentication logic here
                        break;
                    case SUBSCRIBE:
                        String destination = accessor.getDestination();
                        String sessionId = accessor.getSessionId();
                        // If you set the User principal, you could use accessor.getUser().getName()
                        System.out.println(">> STOMP SUBSCRIBE: Session **" + sessionId + "** subscribing to **" + destination + "**");
                        break;
                    case DISCONNECT:
                        // This event is often triggered when the WebSocket connection closes
                        System.out.println(">> STOMP DISCONNECT: Session **" + accessor.getSessionId() + "** disconnecting.");
                        break;
                    default:
                        // Other commands like SEND, MESSAGE, UNSUBSCRIBE, HEARTBEAT, etc.
                        break;
                }
                // Your authentication logic (uncommented part of your original code) goes here
                // ... (authentication logic)

                return message;
            }
        });
    }

}