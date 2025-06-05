package id.ac.ui.cs.advprog.chat.event;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageDeletedEventTest {

    @Test
    void testGetMessageReturnsCorrectSource() {
        ChatMessage message = new ChatMessage();
        message.setId(42L);
        message.setContent("[Message deleted]");

        ChatMessageDeletedEvent event = new ChatMessageDeletedEvent(message);

        assertSame(message, event.getMessage());
        assertEquals(42L, event.getMessage().getId());
        assertEquals("[Message deleted]", event.getMessage().getContent());
    }
}
