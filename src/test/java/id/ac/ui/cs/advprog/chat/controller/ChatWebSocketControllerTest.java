package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ChatDeleteRequest;
import id.ac.ui.cs.advprog.chat.dto.ChatEditRequest;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.security.UserPrincipal;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatWebSocketControllerTest {

    @Mock
    private IChatMessageService msgSvc;

    @Mock
    private ChatRoomService roomSvc;

    @InjectMocks
    private ChatWebSocketController controller;

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
        ChatEditRequest req = new ChatEditRequest();
        req.setId(7L);
        req.setNewContent("upd");
        ChatMessage updated = new ChatMessage();
        when(msgSvc.editMessage(7L, "upd")).thenReturn(Optional.of(updated));

        ChatMessage out = controller.edit(2L, req);

        assertSame(updated, out);
        assertEquals(2L, req.getRoomId());
        verify(msgSvc).editMessage(7L, "upd");
    }

    @Test
    void editThrowsIfMissing() {
        ChatEditRequest req = new ChatEditRequest();
        req.setId(8L);
        req.setNewContent("x");
        when(msgSvc.editMessage(8L, "x")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.edit(1L, req));
        assertTrue(ex.getMessage().contains("Message not found with id: 8"));
    }

    @Test
    void editMapping() throws NoSuchMethodException {
        Method m = ChatWebSocketController.class.getMethod("edit", Long.class, ChatEditRequest.class);
        MessageMapping ann = m.getAnnotation(MessageMapping.class);
        assertNotNull(ann);
        assertArrayEquals(new String[]{"/chat.edit.{roomId}"}, ann.value());
    }

    @Test
    void deleteDelegatesAndReturns() {
        ChatDeleteRequest req = new ChatDeleteRequest();
        req.setId(9L);
        ChatMessage deleted = new ChatMessage();
        when(msgSvc.deleteMessage(9L)).thenReturn(Optional.of(deleted));

        ChatMessage out = controller.delete(4L, req);

        assertSame(deleted, out);
        assertEquals(4L, req.getRoomId());
        verify(msgSvc).deleteMessage(9L);
    }

    @Test
    void deleteThrowsIfMissing() {
        ChatDeleteRequest req = new ChatDeleteRequest();
        req.setId(10L);
        when(msgSvc.deleteMessage(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.delete(5L, req));
        assertTrue(ex.getMessage().contains("Message not found with id: 10"));
    }

    @Test
    void deleteMapping() throws NoSuchMethodException {
        Method m = ChatWebSocketController.class.getMethod("delete", Long.class, ChatDeleteRequest.class);
        MessageMapping ann = m.getAnnotation(MessageMapping.class);
        assertNotNull(ann);
        assertArrayEquals(new String[]{"/chat.delete.{roomId}"}, ann.value());
    }

    @Test
    void historyDelegatesAndReturns() {
        ChatMessage m1 = new ChatMessage();
        ChatMessage m2 = new ChatMessage();
        when(msgSvc.getMessagesByRoom(99L)).thenReturn(List.of(m1, m2));

        List<ChatMessage> out = controller.history(99L, pacPrincipal);

        assertEquals(2, out.size());
        assertSame(m1, out.get(0));
        assertSame(m2, out.get(1));
        verify(msgSvc).getMessagesByRoom(99L);
    }

    @Test
    void historyMapping() throws NoSuchMethodException {
        Method m = ChatWebSocketController.class.getMethod("history", Long.class, Principal.class);
        MessageMapping ann = m.getAnnotation(MessageMapping.class);
        assertNotNull(ann);
        assertArrayEquals(new String[]{"/chat.history.{roomId}"}, ann.value());
    }
}