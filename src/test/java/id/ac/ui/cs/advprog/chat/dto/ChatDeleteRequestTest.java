package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatDeleteRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatDeleteRequest req = new ChatDeleteRequest();
        req.setRoomId(3L);
        req.setId(4L);

        assertEquals(3L, req.getRoomId());
        assertEquals(4L, req.getId());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        ChatDeleteRequest req = new ChatDeleteRequest(5L, 6L);
        assertEquals(5L, req.getRoomId());
        assertEquals(6L, req.getId());
    }
}