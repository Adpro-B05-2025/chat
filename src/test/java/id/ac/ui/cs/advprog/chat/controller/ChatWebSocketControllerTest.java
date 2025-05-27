package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ChatDeleteRequest;
import id.ac.ui.cs.advprog.chat.dto.ChatEditRequest;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatWebSocketControllerTest {

    @Mock
    private IChatMessageService msgSvc;

    @Mock
    private ChatRoomService roomSvc;

    @Mock
    private SimpMessagingTemplate ws;

    @Mock
    private SimpMessageHeaderAccessor headerAccessor;

    @InjectMocks
    private ChatWebSocketController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInitRoom_success() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        String authToken = "token";
        ChatRoom mockRoom = new ChatRoom();
        mockRoom.setId(10L);

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of(
                "USER_ID", currentUserId,
                "AUTH_TOKEN", authToken
        ));
        when(roomSvc.createIfNotExists(currentUserId, targetUserId)).thenReturn(mockRoom);

        controller.initRoom(targetUserId, headerAccessor);

        verify(ws).convertAndSendToUser(eq(currentUserId.toString()), eq("/topic/chat.init"), eq(mockRoom.getId()));
        verify(ws).convertAndSend(eq("/topic/chat.init." + targetUserId), eq(mockRoom.getId()));
    }

    @Test
    void testInitRoom_error() {
        Long currentUserId = 1L;
        Long targetUserId = 2L;
        String authToken = "token";

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of(
                "USER_ID", currentUserId,
                "AUTH_TOKEN", authToken
        ));
        when(roomSvc.createIfNotExists(currentUserId, targetUserId)).thenThrow(new RuntimeException("something went wrong"));

        controller.initRoom(targetUserId, headerAccessor);

        verify(ws).convertAndSend(contains("/topic/chat.init." + targetUserId + ".error"), contains("Failed to initialize room"));
    }

    @Test
    void testSend_success() {
        Long senderId = 5L;
        Long roomId = 9L;
        ChatMessage input = new ChatMessage();
        input.setContent("Hello");
        input.setReceiverId(6L);

        ChatMessage saved = new ChatMessage();
        saved.setId(999L);
        saved.setContent("Hello");

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", senderId));
        when(msgSvc.sendMessage(any())).thenReturn(saved);

        controller.send(roomId, input, headerAccessor);

        assertEquals(senderId, input.getSenderId());
        assertEquals(roomId, input.getRoomId());
        verify(msgSvc).sendMessage(input);
    }

    @Test
    void testSend_error() {
        Long roomId = 9L;
        Long senderId = 5L;
        ChatMessage message = new ChatMessage();

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", senderId));
        when(msgSvc.sendMessage(any())).thenThrow(new RuntimeException("boom"));

        controller.send(roomId, message, headerAccessor);

        verify(ws).convertAndSend(eq("/topic/chat." + roomId + ".error"), contains("Failed to send message"));
    }

    @Test
    void testEdit_success() {
        Long roomId = 3L;
        Long userId = 4L;
        ChatEditRequest req = new ChatEditRequest();
        req.setId(100L);
        req.setNewContent("new");

        ChatMessage updated = new ChatMessage();
        updated.setId(100L);

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", userId));
        when(msgSvc.editMessage(eq(100L), eq("new"))).thenReturn(Optional.of(updated));

        controller.edit(roomId, req, headerAccessor);

        verify(msgSvc).editMessage(100L, "new");
    }

    @Test
    void testEdit_error() {
        Long roomId = 3L;
        Long userId = 4L;
        ChatEditRequest req = new ChatEditRequest();
        req.setId(100L);
        req.setNewContent("new");

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", userId));
        when(msgSvc.editMessage(eq(100L), eq("new"))).thenThrow(new RuntimeException("boom"));

        controller.edit(roomId, req, headerAccessor);

        verify(ws).convertAndSend(eq("/topic/chat." + roomId + ".error"), contains("Failed to edit message"));
    }

    @Test
    void testDelete_success() {
        Long roomId = 8L;
        Long userId = 9L;
        ChatDeleteRequest req = new ChatDeleteRequest();
        req.setId(300L);

        ChatMessage deleted = new ChatMessage();
        deleted.setId(300L);

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", userId));
        when(msgSvc.deleteMessage(eq(300L))).thenReturn(Optional.of(deleted));

        controller.delete(roomId, req, headerAccessor);

        verify(msgSvc).deleteMessage(300L);
    }

    @Test
    void testDelete_error() {
        Long roomId = 8L;
        Long userId = 9L;
        ChatDeleteRequest req = new ChatDeleteRequest();
        req.setId(300L);

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", userId));
        when(msgSvc.deleteMessage(eq(300L))).thenThrow(new RuntimeException("boom"));

        controller.delete(roomId, req, headerAccessor);

        verify(ws).convertAndSend(eq("/topic/chat." + roomId + ".error"), contains("Failed to delete message"));
    }

    @Test
    void testHistory_success() {
        Long roomId = 7L;
        Long userId = 1L;
        ChatMessage m1 = new ChatMessage();
        m1.setId(1L);
        ChatMessage m2 = new ChatMessage();
        m2.setId(2L);

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", userId));
        when(msgSvc.getMessagesByRoom(roomId)).thenReturn(List.of(m1, m2));

        controller.history(roomId, headerAccessor);

        verify(ws).convertAndSend("/topic/chat." + roomId + ".messages", m1);
        verify(ws).convertAndSend("/topic/chat." + roomId + ".messages", m2);
        verify(ws).convertAndSend(eq("/topic/chat." + roomId + ".history.complete"), contains("2 messages"));
    }

    @Test
    void testHistory_error() {
        Long roomId = 7L;
        Long userId = 1L;

        when(headerAccessor.getSessionAttributes()).thenReturn(Map.of("USER_ID", userId));
        when(msgSvc.getMessagesByRoom(roomId)).thenThrow(new RuntimeException("boom"));

        controller.history(roomId, headerAccessor);

        verify(ws).convertAndSend(eq("/topic/chat." + roomId + ".error"), contains("Failed to fetch history"));
    }
}