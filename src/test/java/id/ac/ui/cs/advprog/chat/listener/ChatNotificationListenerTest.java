// src/test/java/id/ac/ui/cs/advprog/chat/listener/ChatNotificationListenerTest.java
package id.ac.ui.cs.advprog.chat.listener;

import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageEditedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageDeletedEvent;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void onSentPublishesToRoomMessagesTopic() {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(123L);
        ChatMessageSentEvent ev = new ChatMessageSentEvent(msg);

        listener.onSent(ev);

        verify(ws).convertAndSend("/topic/chat.123.messages", msg);
    }

    @Test
    void onEditedPublishesToRoomUpdatesTopic() {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(456L);
        ChatMessageEditedEvent ev = new ChatMessageEditedEvent(msg);

        listener.onEdited(ev);

        verify(ws).convertAndSend("/topic/chat.456.updates", msg);
    }

    @Test
    void onDeletedPublishesToRoomUpdatesTopic() {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(789L);
        ChatMessageDeletedEvent ev = new ChatMessageDeletedEvent(msg);

        listener.onDeleted(ev);

        verify(ws).convertAndSend("/topic/chat.789.updates", msg);
    }
}