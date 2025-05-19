package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

class ChatDeleteRequestTest {

    @Test
    void testNoArgsConstructorAndSetter() {
        ChatDeleteRequest req = new ChatDeleteRequest();
        req.setId(789L);
        assertEquals(789L, req.getId());
    }

    @Test
    void testAllArgsConstructorAndGetter() {
        ChatDeleteRequest req = new ChatDeleteRequest(321L);
        assertEquals(321L, req.getId());
    }
}
