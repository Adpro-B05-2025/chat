package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatDeleteRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatDeleteRequest req = new ChatDeleteRequest();
        req.setRoomId(7L);
        req.setId(42L);

        assertEquals(7L, req.getRoomId());
        assertEquals(42L, req.getId());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        ChatDeleteRequest req = new ChatDeleteRequest(8L, 99L);

        assertEquals(8L, req.getRoomId());
        assertEquals(99L, req.getId());
    }
}
