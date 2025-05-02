package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageResponseTest {

    @Test
    void testChatMessageResponseFields() {
        LocalDateTime now = LocalDateTime.now();
        ChatMessageResponse response = new ChatMessageResponse(1L, 10L, 20L, "Message", "sent", now);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getSenderId());
        assertEquals(20L, response.getReceiverId());
        assertEquals("Message", response.getContent());
        assertEquals("sent", response.getStatus());
        assertEquals(now, response.getTimestamp());
    }
}
