package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatEditRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatEditRequest req = new ChatEditRequest();
        req.setRoomId(1L);
        req.setId(2L);
        req.setNewContent("updated");

        assertEquals(1L, req.getRoomId());
        assertEquals(2L, req.getId());
        assertEquals("updated", req.getNewContent());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        ChatEditRequest req = new ChatEditRequest(10L, 20L, "hello");
        assertEquals(10L, req.getRoomId());
        assertEquals(20L, req.getId());
        assertEquals("hello", req.getNewContent());
    }
}
