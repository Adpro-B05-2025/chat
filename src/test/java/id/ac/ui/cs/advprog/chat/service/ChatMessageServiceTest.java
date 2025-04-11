package com.adpro.chat.service;

import com.adpro.chat.model.ChatMessage;
import com.adpro.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageServiceTest {

    @Test
    void testSendMessage_shouldSaveToDatabase() {
        ChatMessageRepository repo = mock(ChatMessageRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        ChatMessageService service = new ChatMessageService(repo, eventPublisher);

        ChatMessage input = new ChatMessage(null, 1L, 2L, "Halo", null, null);
        ChatMessage saved = new ChatMessage(100L, 1L, 2L, "Halo", "sent", LocalDateTime.now());

        when(repo.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessage result = service.sendMessage(input);

        assertNotNull(result.getId());
        assertEquals("sent", result.getStatus());
        verify(repo, times(1)).save(any(ChatMessage.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }
}
