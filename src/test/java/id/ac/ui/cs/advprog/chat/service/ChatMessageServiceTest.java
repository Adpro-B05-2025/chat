package com.adpro.chat.service;

import com.adpro.chat.model.ChatMessage;
import com.adpro.chat.repository.ChatMessageRepository;
import com.adpro.chat.event.ChatMessageSentEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceSendTest {

    @Mock
    ChatMessageRepository repo;

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    ChatMessageService service;

    @Test
    void testSendMessage_shouldSaveToDatabaseAndTriggerEvent() {
        ChatMessage input = new ChatMessage(null, 1L, 2L, "Hello", null, null);
        ChatMessage output = new ChatMessage(10L, 1L, 2L, "Hello", "sent", LocalDateTime.now());

        when(repo.save(any())).thenReturn(output);

        ChatMessage result = service.sendMessage(input);

        assertNotNull(result.getId());
        assertEquals("sent", result.getStatus());
        verify(publisher).publishEvent(any(ChatMessageSentEvent.class));
    }
}
