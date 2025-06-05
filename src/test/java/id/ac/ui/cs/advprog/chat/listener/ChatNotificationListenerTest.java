package id.ac.ui.cs.advprog.chat.listener;

import id.ac.ui.cs.advprog.chat.event.ChatMessageDeletedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageEditedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Mockito.*;

class ChatNotificationListenerTest {

    @Mock
    private SimpMessagingTemplate ws;

    @InjectMocks
    private ChatNotificationListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testOnSentPublishesToCorrectTopic() {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(1L);
        ChatMessageSentEvent event = new ChatMessageSentEvent(msg);

        listener.onSent(event);

        verify(ws).convertAndSend("/topic/chat.1.messages", msg);
    }

    @Test
    void testOnEditedPublishesToCorrectTopic() {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(2L);
        ChatMessageEditedEvent event = new ChatMessageEditedEvent(msg);

        listener.onEdited(event);

        verify(ws).convertAndSend("/topic/chat.2.updates", msg);
    }

    @Test
    void testOnDeletedPublishesToCorrectTopic() {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(3L);
        ChatMessageDeletedEvent event = new ChatMessageDeletedEvent(msg);

        listener.onDeleted(event);

        verify(ws).convertAndSend("/topic/chat.3.updates", msg);
    }
}
