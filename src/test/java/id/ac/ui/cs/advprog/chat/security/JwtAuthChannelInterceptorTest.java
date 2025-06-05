package id.ac.ui.cs.advprog.chat.security;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import java.util.Optional;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthChannelInterceptorTest {

    @Mock private ChatRoomService roomService;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks private JwtAuthChannelInterceptor interceptor;

    private MessageChannel channel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        channel = mock(MessageChannel.class);
    }

    private Message<byte[]> buildMessage(StompCommand cmd, String dest, Principal principal, Map<String, Object> sessionAttrs) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(cmd);
        accessor.setDestination(dest);
        accessor.setSessionId("session-1");
        accessor.setUser(principal);
        accessor.setSessionAttributes(sessionAttrs);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    @Test
    void testSend_roomAction_allowsMember() {
        Long uid = 1L, rid = 99L;
        Principal principal = new JwtAuthChannelInterceptor.SimplePrincipal("test", uid);
        ChatRoom room = new ChatRoom(rid, uid, 2L);
        when(roomService.find(rid)).thenReturn(Optional.of(room));

        Message<byte[]> msg = buildMessage(StompCommand.SEND, "/app/chat.edit." + rid, principal, new HashMap<>());
        assertSame(msg, interceptor.preSend(msg, channel));
    }

    @Test
    void testSend_roomAction_notMember_throwsAccessDenied() {
        Long uid = 30L, rid = 77L;
        Principal principal = new JwtAuthChannelInterceptor.SimplePrincipal("outsider", uid);
        ChatRoom room = new ChatRoom(rid, 1L, 2L);
        when(roomService.find(rid)).thenReturn(Optional.of(room));

        Message<byte[]> msg = buildMessage(StompCommand.SEND, "/app/chat.edit." + rid, principal, new HashMap<>());
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(msg, channel));
    }

//    @Test
//    void testSend_connect_parsesJwt_andStoresPrincipal() {
//        Map<String, Object> attrs = new HashMap<>();
//        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
//        accessor.setSessionAttributes(attrs);
//        accessor.setLeaveMutable(true);
//        accessor.setNativeHeader("Authorization", "Bearer fakeToken");
//        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
//
//        when(jwtUtils.validateJwtToken("fakeToken")).thenReturn(true);
//        when(jwtUtils.getUserIdFromJwtToken("fakeToken")).thenReturn("42");
//        when(jwtUtils.getUsernameFromToken("fakeToken")).thenReturn("meimei");
//
//        Message<?> result = interceptor.preSend(msg, channel);
//        StompHeaderAccessor modifiedAccessor = StompHeaderAccessor.wrap(result);
//
//        assertNotNull(modifiedAccessor.getUser());
//        assertTrue(modifiedAccessor.getUser() instanceof JwtAuthChannelInterceptor.SimplePrincipal);
//        assertEquals("meimei", modifiedAccessor.getUser().getName());
//    }

    @Test
    void testSend_chatInit_valid() {
        Long uid = 88L, targetId = 99L;
        Principal principal = new JwtAuthChannelInterceptor.SimplePrincipal("gege", uid);
        Message<byte[]> msg = buildMessage(StompCommand.SEND, "/app/chat.init." + targetId, principal, new HashMap<>());

        assertSame(msg, interceptor.preSend(msg, channel));
    }

    @Test
    void testSend_chatInit_withSelf_throwsAccessDenied() {
        Long uid = 77L;
        Principal principal = new JwtAuthChannelInterceptor.SimplePrincipal("self", uid);
        Message<byte[]> msg = buildMessage(StompCommand.SEND, "/app/chat.init." + uid, principal, new HashMap<>());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(msg, channel));
    }

    @Test
    void testSend_invalidDestination_throwsAccessDenied() {
        Long uid = 100L;
        Principal principal = new JwtAuthChannelInterceptor.SimplePrincipal("bad", uid);
        Message<byte[]> msg = buildMessage(StompCommand.SEND, "/app/chat.send.xyz", principal, new HashMap<>());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(msg, channel));
    }

    @Test
    void testConnect_noAuthorizationHeader_doesNotSetUser() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());
        Message<byte[]> msg = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(msg, channel);
        StompHeaderAccessor modifiedAccessor = StompHeaderAccessor.wrap(result);

        assertNull(modifiedAccessor.getUser(), "User seharusnya tidak diset jika tidak ada Authorization");
    }
}
