package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageResponseTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        Long id = 1L;
        Long senderId = 10L;
        Long receiverId = 20L;
        String content = "Hello";
        String status = "sent";
        LocalDateTime timestamp = LocalDateTime.now();

        ChatMessageResponse response = new ChatMessageResponse(id, senderId, receiverId, content, status, timestamp);

        assertEquals(id, response.getId());
        assertEquals(senderId, response.getSenderId());
        assertEquals(receiverId, response.getReceiverId());
        assertEquals(content, response.getContent());
        assertEquals(status, response.getStatus());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void testNoArgsConstructor() {
        ChatMessageResponse response = new ChatMessageResponse();
        assertNotNull(response); // validasi minimal untuk constructor kosong
    }
}