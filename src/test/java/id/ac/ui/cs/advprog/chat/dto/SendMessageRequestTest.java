package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SendMessageRequestTest {

    @Test
    void testSendMessageRequestFields() {
        SendMessageRequest request = new SendMessageRequest(1L, 2L, "Hello");
        assertEquals(1L, request.getSenderId());
        assertEquals(2L, request.getReceiverId());
        assertEquals("Hello", request.getContent());
    }
}
