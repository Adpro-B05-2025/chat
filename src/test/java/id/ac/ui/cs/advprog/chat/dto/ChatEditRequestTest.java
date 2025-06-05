package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChatEditRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatEditRequest request = new ChatEditRequest();
        request.setRoomId(1L);
        request.setId(2L);
        request.setNewContent("Updated message");

        assertEquals(1L, request.getRoomId());
        assertEquals(2L, request.getId());
        assertEquals("Updated message", request.getNewContent());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        ChatEditRequest request = new ChatEditRequest(3L, 4L, "Hello world");

        assertEquals(3L, request.getRoomId());
        assertEquals(4L, request.getId());
        assertEquals("Hello world", request.getNewContent());
    }
}
