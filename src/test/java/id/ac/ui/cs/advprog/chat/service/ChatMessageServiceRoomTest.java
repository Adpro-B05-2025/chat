package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatMessageServiceRoomTest {

    @Mock
    private ChatMessageRepository repo;

    @InjectMocks
    private ChatMessageService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMessagesByRoom_returnsOrderedByTimestamp() {
        ChatMessage older = new ChatMessage();
        older.setRoomId(1L);
        older.setTimestamp(LocalDateTime.now().minusMinutes(10));

        ChatMessage newer = new ChatMessage();
        newer.setRoomId(1L);
        newer.setTimestamp(LocalDateTime.now());

        when(repo.findByRoomIdOrderByTimestampAsc(1L))
                .thenReturn(List.of(older, newer));

        List<ChatMessage> result = service.getMessagesByRoom(1L);

        assertEquals(2, result.size());
        assertSame(older, result.get(0));
        assertSame(newer, result.get(1));

        verify(repo).findByRoomIdOrderByTimestampAsc(1L);
    }
}