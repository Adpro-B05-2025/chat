package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.event.ChatMessageDeletedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageEditedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository repo;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private ChatMessageService service;

    private ChatMessage msg;

    @BeforeEach
    void setUp() {
        msg = new ChatMessage();
        msg.setId(1L);
        msg.setSenderId(100L);
        msg.setReceiverId(200L);
        msg.setContent("hiya");
        // status & timestamp will be set in sendMessage()
    }

    @Test
    void testSendMessage_publishesEventAndReturnsSaved() {
        // arrange
        when(repo.save(any())).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        // act
        ChatMessage sent = service.sendMessage(msg);

        // assert
        assertEquals("sent", sent.getStatus());
        assertNotNull(sent.getTimestamp());
        assertEquals(1L, sent.getId());
        verify(publisher).publishEvent(argThat(e ->
                e instanceof ChatMessageSentEvent &&
                        ((ChatMessageSentEvent) e).getMessage().getId().equals(1L)
        ));
    }

    @Test
    void testGetMessage_whenExists() {
        when(repo.findById(1L)).thenReturn(Optional.of(msg));

        Optional<ChatMessage> result = service.getMessage(1L);
        assertTrue(result.isPresent());
        assertEquals(msg, result.get());
    }

    @Test
    void testGetAllMessages_returnsList() {
        List<ChatMessage> list = Arrays.asList(msg, new ChatMessage());
        when(repo.findAll()).thenReturn(list);

        List<ChatMessage> result = service.getAllMessages();
        assertEquals(2, result.size());
        assertEquals(list, result);
    }

    @Test
    void testEditMessage_updatesContentAndStatus() {
        when(repo.findById(1L)).thenReturn(Optional.of(msg));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<ChatMessage> edited = service.editMessage(1L, "new");
        assertTrue(edited.isPresent());
        assertEquals("new", edited.get().getContent());
        assertEquals("edited", edited.get().getStatus());
    }

    @Test
    void testDeleteMessage_setsStatusDeleted() {
        when(repo.findById(1L)).thenReturn(Optional.of(msg));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Optional<ChatMessage> deleted = service.deleteMessage(1L);
        assertTrue(deleted.isPresent());
        assertEquals("deleted", deleted.get().getStatus());
    }

    @Test
    void testEditMessagePublishesEditedEvent() {
        // arrange
        msg.setContent("old");
        when(repo.findById(2L)).thenReturn(Optional.of(msg));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // act
        Optional<ChatMessage> maybe = service.editMessage(2L, "updated");

        // assert
        assertTrue(maybe.isPresent());
        ChatMessage saved = maybe.get();
        verify(publisher).publishEvent(argThat(evt ->
                evt instanceof ChatMessageEditedEvent &&
                        ((ChatMessageEditedEvent) evt).getMessage().equals(saved)
        ));
    }

    @Test
    void testDeleteMessagePublishesDeletedEvent() {
        // arrange
        when(repo.findById(3L)).thenReturn(Optional.of(msg));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // act
        Optional<ChatMessage> maybe = service.deleteMessage(3L);

        // assert
        assertTrue(maybe.isPresent());
        ChatMessage saved = maybe.get();
        verify(publisher).publishEvent(argThat(evt ->
                evt instanceof ChatMessageDeletedEvent &&
                        ((ChatMessageDeletedEvent) evt).getMessage().equals(saved)
        ));
    }
}
