package id.ac.ui.cs.advprog.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired JwtAuthChannelInterceptor jwtInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry cfg) {
        cfg.enableSimpleBroker("/topic");              // in-memory broker
        cfg.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry reg) {
        reg.addEndpoint("/ws-chat")                    // handshake URL
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
