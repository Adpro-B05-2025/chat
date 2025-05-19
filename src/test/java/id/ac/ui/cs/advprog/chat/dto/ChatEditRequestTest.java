// src/test/java/id/ac/ui/cs/advprog/chat/dto/ChatEditRequestTest.java
package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatEditRequestTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatEditRequest req = new ChatEditRequest();
        req.setId(123L);
        req.setNewContent("updated");

        assertEquals(123L, req.getId());
        assertEquals("updated", req.getNewContent());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        ChatEditRequest req = new ChatEditRequest(456L, "hello");
        assertEquals(456L, req.getId());
        assertEquals("hello", req.getNewContent());
    }
}
