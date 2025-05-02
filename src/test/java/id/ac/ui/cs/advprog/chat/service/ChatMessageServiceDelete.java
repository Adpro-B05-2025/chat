package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceDeleteTest {

    @Mock
    ChatMessageRepository repo;

    @InjectMocks
    ChatMessageService service;

    @Test
    void testDeleteMessage_shouldUpdateStatusToDeleted() {
        ChatMessage msg = new ChatMessage(99L, 1L, 2L, "To delete", "sent", LocalDateTime.now());
        when(repo.findById(99L)).thenReturn(Optional.of(msg));
        when(repo.save(any())).thenReturn(msg);

        Optional<ChatMessage> result = service.deleteMessage(99L);

        assertTrue(result.isPresent());
        assertEquals("deleted", result.get().getStatus());
    }
}
