package id.ac.ui.cs.advprog.chat.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatMessage msg = new ChatMessage();

        Long id = 1L;
        Long senderId = 10L;
        Long receiverId = 20L;
        String content = "Hello!";
        String status = "sent";
        LocalDateTime timestamp = LocalDateTime.now();

        msg.setId(id);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setStatus(status);
        msg.setTimestamp(timestamp);

        assertEquals(id, msg.getId());
        assertEquals(senderId, msg.getSenderId());
        assertEquals(receiverId, msg.getReceiverId());
        assertEquals(content, msg.getContent());
        assertEquals(status, msg.getStatus());
        assertEquals(timestamp, msg.getTimestamp());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        Long id = 2L;
        Long senderId = 30L;
        Long receiverId = 40L;
        String content = "Hi!";
        String status = "edited";
        LocalDateTime timestamp = LocalDateTime.now();

        ChatMessage msg = new ChatMessage(id, senderId, receiverId, content, status, timestamp);

        assertEquals(id, msg.getId());
        assertEquals(senderId, msg.getSenderId());
        assertEquals(receiverId, msg.getReceiverId());
        assertEquals(content, msg.getContent());
        assertEquals(status, msg.getStatus());
        assertEquals(timestamp, msg.getTimestamp());
    }
}