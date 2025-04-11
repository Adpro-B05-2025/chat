package com.adpro.chat.service;

import com.adpro.chat.model.ChatMessage;
import com.adpro.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceEditTest {

    @Mock
    ChatMessageRepository repo;

    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    ChatMessageService service;

    @Test
    void testEditMessage_shouldUpdateContentAndStatus() {
        ChatMessage existing = new ChatMessage(2L, 1L, 2L, "Old Content", "sent", LocalDateTime.now());
        when(repo.findById(2L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenReturn(existing);

        Optional<ChatMessage> result = service.editMessage(2L, "Updated Content");

        assertTrue(result.isPresent());
        assertEquals("Updated Content", result.get().getContent());
        assertEquals("edited", result.get().getStatus());
    }
}
