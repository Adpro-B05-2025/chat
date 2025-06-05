package id.ac.ui.cs.advprog.chat.event;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageEditedEventTest {

    @Test
    void testGetMessageReturnsCorrectSource() {
        ChatMessage message = new ChatMessage();
        message.setId(99L);
        message.setContent("Edited content");

        ChatMessageEditedEvent event = new ChatMessageEditedEvent(message);

        assertSame(message, event.getMessage());
        assertEquals(99L, event.getMessage().getId());
        assertEquals("Edited content", event.getMessage().getContent());
    }
}
