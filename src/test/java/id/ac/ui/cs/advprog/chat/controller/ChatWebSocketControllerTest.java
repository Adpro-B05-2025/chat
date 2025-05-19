package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ChatDeleteRequest;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.security.UserPrincipal;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatWebSocketControllerTest {

    @Mock IChatMessageService msgSvc;
    @Mock ChatRoomService roomSvc;
    @InjectMocks ChatWebSocketController controller;

    private Principal pacPrincipal;
    private Principal docPrincipal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pacPrincipal = new UserPrincipal(10L, "pac", null);
        docPrincipal = new UserPrincipal(20L, "doc", null);
    }

    @Test
    void initRoomDelegatesToService() {
        Long doctorId = 20L, pacId = 10L;
        ChatRoom room = new ChatRoom(5L, pacId, doctorId);
        when(roomSvc.createIfNotExists(pacId, doctorId)).thenReturn(room);

        Long id = controller.initRoom(doctorId, pacPrincipal);

        assertEquals(5L, id);
        verify(roomSvc).createIfNotExists(pacId, doctorId);
    }

    @Test
    void initRoomMapping() throws NoSuchMethodException {
        Method m = ChatWebSocketController.class.getMethod("initRoom", Long.class, Principal.class);
        MessageMapping ann = m.getAnnotation(MessageMapping.class);
        assertNotNull(ann);
        assertArrayEquals(new String[]{"/chat.init.{doctorId}"}, ann.value());
    }

    @Test
    void sendDelegatesAndSetsRoomId() {
        ChatMessage msg = new ChatMessage();
        when(msgSvc.sendMessage(msg)).thenReturn(msg);

        ChatMessage out = controller.send(3L, msg, pacPrincipal);

        assertSame(msg, out);
        assertEquals(3L, msg.getRoomId());
        verify(msgSvc).sendMessage(msg);
    }

    @Test
    void sendMapping() throws NoSuchMethodException {
        Method m = ChatWebSocketController.class.getMethod("send", Long.class, ChatMessage.class, Principal.class);
        MessageMapping ann = m.getAnnotation(MessageMapping.class);
        assertNotNull(ann);
        assertArrayEquals(new String[]{"/chat.send.{roomId}"}, ann.value());
    }

    @Test
    void editDelegatesAndReturns() {
        ChatEditRequest req = new ChatEditRequest(7L, "upd");
        ChatMessage updated = new ChatMessage();
        when(msgSvc.editMessage(7L, "upd")).thenReturn(Optional.of(updated));

        ChatMessage out = controller.edit(2L, req);

        assertSame(updated, out);
        assertEquals(2L, req.getRoomId());
        verify(msgSvc).editMessage(7L, "upd");
    }

    @Test
    void editThrowsIfMissing() {
        ChatEditRequest req = new ChatEditRequest(8L, "x");
        when(msgSvc.editMessage(8L, "x")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controlle
