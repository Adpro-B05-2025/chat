// src/test/java/id/ac/ui/cs/advprog/chat/listener/ChatNotificationListenerTest.java
package id.ac.ui.cs.advprog.chat.listener;

import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageEditedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageDeletedEvent;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ChatNotificationListenerTest {

    @Mock
    private SimpMessagingTemplate ws;

    private ChatNotificationListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new ChatNotificationListener(ws);
    }

    @Test
    void onSentPublishesToMessagesTopic() {
        ChatMessage msg = new ChatMessage();
        ChatMessageSentEvent ev = new ChatMessageSentEvent(msg);

        listener.onSent(ev);

        verify(ws).convertAndSend("/topic/messages", msg);
    }

    @Test
    void onEditedPublishesToUpdatesTopic() {
        ChatMessage msg = new ChatMessage();
        ChatMessageEditedEvent ev = new ChatMessageEditedEvent(msg);

        listener.onEdited(ev);

        verify(ws).convertAndSend("/topic/updates", msg);
    }

    @Test
    void onDeletedPublishesToUpdatesTopic() {
        ChatMessage msg = new ChatMessage();
        ChatMessageDeletedEvent ev = new ChatMessageDeletedEvent(msg);

        listener.onDeleted(ev);

        verify(ws).convertAndSend("/topic/updates", msg);
    }
}
