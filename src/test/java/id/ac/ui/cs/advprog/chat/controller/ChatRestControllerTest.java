package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.ContactResponse;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.service.AuthProfileService;
import id.ac.ui.cs.advprog.chat.service.ChatRoomService;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatRestControllerTest {

    @Mock
    private ChatRoomService roomService;

    @Mock
    private IChatMessageService messageService;

    @Mock
    private AuthProfileService authProfileService;

    @InjectMocks
    private ChatRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetContacts_returnsSortedContacts() {
        Long userId = 1L;
        Long contactId = 2L;
        Long roomId = 10L;
        LocalDateTime now = LocalDateTime.now();

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setPacilianId(userId);
        room.setDoctorId(contactId);

        ChatMessage msg = new ChatMessage();
        msg.setContent("Hello");
        msg.setStatus("sent");
        msg.setTimestamp(now);

        when(roomService.findRoomsByUserId(userId)).thenReturn(List.of(room));
        when(authProfileService.getUserName(contactId)).thenReturn("ContactName");
        when(messageService.getMessagesByRoom(roomId)).thenReturn(List.of(msg));

        ResponseEntity<List<ContactResponse>> response = controller.getContacts(userId);

        assertEquals(200, response.getStatusCodeValue());
        List<ContactResponse> contacts = response.getBody();
        assertNotNull(contacts);
        assertEquals(1, contacts.size());
        assertEquals(contactId, contacts.get(0).getContactId());
        assertEquals("ContactName", contacts.get(0).getContactName());
        assertEquals("Hello", contacts.get(0).getLastMessage());
        assertEquals(now, contacts.get(0).getLastMessageTime());
    }

    @Test
    void testGetContacts_handlesDeletedMessage() {
        Long userId = 1L;
        Long contactId = 2L;
        Long roomId = 11L;

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setPacilianId(contactId);
        room.setDoctorId(userId);

        ChatMessage msg = new ChatMessage();
        msg.setContent("should be ignored");
        msg.setStatus("deleted");
        msg.setTimestamp(LocalDateTime.now());

        when(roomService.findRoomsByUserId(userId)).thenReturn(List.of(room));
        when(authProfileService.getUserName(contactId)).thenReturn("DocName");
        when(messageService.getMessagesByRoom(roomId)).thenReturn(List.of(msg));

        ResponseEntity<List<ContactResponse>> response = controller.getContacts(userId);
        assertEquals("Message deleted", response.getBody().get(0).getLastMessage());
    }

    @Test
    void testGetContacts_handlesEmptyMessages() {
        Long userId = 1L;
        Long contactId = 2L;
        Long roomId = 12L;

        ChatRoom room = new ChatRoom();
        room.setId(roomId);
        room.setPacilianId(userId);
        room.setDoctorId(contactId);

        when(roomService.findRoomsByUserId(userId)).thenReturn(List.of(room));
        when(authProfileService.getUserName(contactId)).thenReturn("NoMsgUser");
        when(messageService.getMessagesByRoom(roomId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ContactResponse>> response = controller.getContacts(userId);
        assertEquals("", response.getBody().get(0).getLastMessage());
        assertNull(response.getBody().get(0).getLastMessageTime());
    }

    @Test
    void testGetContacts_returnsEmptyListOnException() {
        Long userId = 99L;
        when(roomService.findRoomsByUserId(userId)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<List<ContactResponse>> response = controller.getContacts(userId);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}
