package id.ac.ui.cs.advprog.chat.config;

import id.ac.ui.cs.advprog.chat.security.JwtAuthChannelInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;

import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    private JwtAuthChannelInterceptor jwtInterceptor;
    private WebSocketConfig config;

    @BeforeEach
    void setUp() {
        jwtInterceptor = mock(JwtAuthChannelInterceptor.class);
        config = new WebSocketConfig(jwtInterceptor);
    }

    @Test
    void testConfigureMessageBroker_setsPrefixesAndBroker() {
        MessageBrokerRegistry registry = mock(MessageBrokerRegistry.class);

        config.configureMessageBroker(registry);

        verify(registry).enableSimpleBroker("/topic");
        verify(registry).setApplicationDestinationPrefixes("/app");
    }

    @Test
    void testRegisterStompEndpoints_registersSockJsEndpoint() {
        StompEndpointRegistry registry = mock(StompEndpointRegistry.class);
        StompWebSocketEndpointRegistration sockJsReg = mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws-chat")).thenReturn(sockJsReg);
        when(sockJsReg.setAllowedOriginPatterns("*")).thenReturn(sockJsReg);
        when(sockJsReg.withSockJS()).thenReturn(null); // return doesn't matter

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws-chat");
        verify(sockJsReg).setAllowedOriginPatterns("*");
        verify(sockJsReg).withSockJS();
    }

    @Test
    void testConfigureClientInboundChannel_addsInterceptor() {
        ChannelRegistration registration = mock(ChannelRegistration.class);
        config.configureClientInboundChannel(registration);
        verify(registration).interceptors(jwtInterceptor);
    }
}
