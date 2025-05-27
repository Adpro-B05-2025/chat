package id.ac.ui.cs.advprog.chat.event;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageSentEventTest {

    @Test
    void testGetMessageReturnsCorrectSource() {
        ChatMessage message = new ChatMessage();
        message.setId(123L);
        message.setContent("Hello world");

        ChatMessageSentEvent event = new ChatMessageSentEvent(message);

        assertSame(message, event.getMessage());
        assertEquals(123L, event.getMessage().getId());
        assertEquals("Hello world", event.getMessage().getContent());
    }
}
