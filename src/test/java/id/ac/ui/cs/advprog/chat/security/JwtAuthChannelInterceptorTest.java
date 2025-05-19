// src/test/java/id/ac/ui/cs/advprog/chat/security/JwtAuthChannelInterceptorTest.java
package id.ac.ui.cs.advprog.chat.security;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class JwtAuthChannelInterceptorTest {

    @Mock
    private ChatRoomService roomSvc;

    @InjectMocks
    private JwtAuthChannelInterceptor interceptor;

    private MessageChannel dummyChannel;

    @BeforeEach
    void setUp() {
        dummyChannel = mock(MessageChannel.class);
    }

    private Message<byte[]> buildSendMessage(Long userId, String destination) {
        // Create a UserPrincipal with given userId
        UserPrincipal user = new UserPrincipal(userId, "user"+userId, Collections.emptyList());

        // Build STOMP SEND message
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination(destination);
        accessor.setUser(user);
        accessor.setSessionId("sess-" + userId);
        accessor.setLeaveMutable(true);

        byte[] payload = new byte[0];
        return MessageBuilder.createMessage(payload, accessor.getMessageHeaders());
    }

    @Test
    void allowsPacilianToSend() {
        Long roomId = 42L;
        Long pacilianId = 100L;
        Long doctorId = 200L;

        // Mock existing room
        when(roomSvc.find(roomId))
                .thenReturn(Optional.of(new ChatRoom(roomId, pacilianId, doctorId)));

        // Build message from pacilian
        Message<byte[]> msg = buildSendMessage(pacilianId, "/app/chat.send." + roomId);

        // Should return the same message without exception
        Message<?> result = interceptor.preSend(msg, dummyChannel);
        assertSame(msg, result);

        verify(roomSvc).find(roomId);
    }

    @Test
    void allowsDoctorToSend() {
        Long roomId = 7L;
        Long pacilianId = 10L;
        Long doctorId = 20L;

        when(roomSvc.find(roomId))
                .thenReturn(Optional.of(new ChatRoom(roomId, pacilianId, doctorId)));

        Message<byte[]> msg = buildSendMessage(doctorId, "/app/chat.send." + roomId);

        Message<?> result = interceptor.preSend(msg, dummyChannel);
        assertSame(msg, result);

        verify(roomSvc).find(roomId);
    }

    @Test
    void deniesNonMemberToSend() {
        Long roomId = 5L;
        Long pacilianId = 1L;
        Long doctorId = 2L;

        when(roomSvc.find(roomId))
                .thenReturn(Optional.of(new ChatRoom(roomId, pacilianId, doctorId)));

        // userId 999L is neither pacilian nor doctor
        Message<byte[]> msg = buildSendMessage(999L, "/app/chat.send." + roomId);

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(msg, dummyChannel)
        );
        assertEquals("You are not a member of this chat room", ex.getMessage());

        verify(roomSvc).find(roomId);
    }

    @Test
    void deniesWhenRoomNotFound() {
        Long roomId = 123L;

        when(roomSvc.find(roomId))
                .thenReturn(Optional.empty());

        Message<byte[]> msg = buildSendMessage(roomId, "/app/chat.send." + roomId);

        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(msg, dummyChannel)
        );
        assertEquals("Chat room not found", ex.getMessage());

        verify(roomSvc).find(roomId);
    }

    @Test
    void ignoresOtherDestinations() {
        // Build SEND to a different endpoint
        Message<byte[]> msg = buildSendMessage(1L, "/app/other.endpoint");

        // Since it doesn't match /app/chat.send., it should pass through
        Message<?> result = interceptor.preSend(msg, dummyChannel);
        assertSame(msg, result);

        // roomSvc.find should never be called
        verifyNoInteractions(roomSvc);
    }

    @Test
    void ignoresNonSendCommands() {
        // Create a CONNECT message
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setDestination("/app/chat.send.1");
        accessor.setUser(new UserPrincipal(1L, "u", Collections.emptyList()));
        accessor.setLeaveMutable(true);

        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        Message<?> result = interceptor.preSend(msg, dummyChannel);
        assertSame(msg, result);

        verifyNoInteractions(roomSvc);
    }
}
