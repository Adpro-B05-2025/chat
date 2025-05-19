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
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class JwtAuthChannelInterceptorTest {

    @Mock private ChatRoomService roomSvc;
    @InjectMocks private JwtAuthChannelInterceptor interceptor;
    private MessageChannel dummyChannel;

    @BeforeEach
    void setUp() {
        dummyChannel = mock(MessageChannel.class);
    }

    private Message<byte[]> buildStompSend(String destination, Long userId) {
        StompHeaderAccessor sh = StompHeaderAccessor.create(StompCommand.SEND);
        sh.setDestination(destination);
        sh.setUser(new UserPrincipal(userId, "user", Collections.emptyList()));
        sh.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], sh.getMessageHeaders());
    }

    @Test
    void allowsEditWhenMember() {
        Long roomId=1L, pac=10L, doc=20L;
        when(roomSvc.find(roomId)).thenReturn(Optional.of(new ChatRoom(roomId,pac,doc)));
        Message<?> mPac = buildStompSend("/app/chat.edit." + roomId, pac);
        assertSame(mPac, interceptor.preSend(mPac, dummyChannel));
        Message<?> mDoc = buildStompSend("/app/chat.edit." + roomId, doc);
        assertSame(mDoc, interceptor.preSend(mDoc, dummyChannel));
    }

    @Test
    void deniesEditWhenNotMember() {
        Long roomId=2L, pac=100L, doc=200L;
        when(roomSvc.find(roomId)).thenReturn(Optional.of(new ChatRoom(roomId,pac,doc)));
        Message<?> bad = buildStompSend("/app/chat.edit." + roomId, 999L);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                ()->interceptor.preSend(bad, dummyChannel));
        assertEquals("You are not a member of this chat room", ex.getMessage());
    }

    @Test
    void allowsDeleteWhenMember() {
        Long roomId=3L, pac=11L, doc=22L;
        when(roomSvc.find(roomId)).thenReturn(Optional.of(new ChatRoom(roomId,pac,doc)));
        Message<?> m = buildStompSend("/app/chat.delete." + roomId, pac);
        assertSame(m, interceptor.preSend(m, dummyChannel));
    }

    @Test
    void deniesDeleteWhenNotMember() {
        Long roomId=4L, pac=12L, doc=24L;
        when(roomSvc.find(roomId)).thenReturn(Optional.of(new ChatRoom(roomId,pac,doc)));
        Message<?> bad = buildStompSend("/app/chat.delete." + roomId, 777L);
        assertThrows(AccessDeniedException.class,
                ()->interceptor.preSend(bad, dummyChannel));
    }
}
