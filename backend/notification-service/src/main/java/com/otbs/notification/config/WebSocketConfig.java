package com.otbs.notification.config;

import com.otbs.notification.security.AuthHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for subscription topics
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for application destinations
        registry.setApplicationDestinationPrefixes("/app");

        // /topic/... for broadcast messages
        // /queue/... for user-specific messages
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-notification")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new AuthHandshakeInterceptor())
                .withSockJS();
    }

}
