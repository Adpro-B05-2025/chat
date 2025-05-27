package id.ac.ui.cs.advprog.chat.repository;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatMessageRepositoryTest {

    @Autowired
    private ChatMessageRepository repo;

    @Test
    void testExistsBySenderIdAndReceiverId() {
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(1L);
        msg.setReceiverId(2L);
        msg.setContent("Hello");
        msg.setStatus("SENT");
        msg.setTimestamp(LocalDateTime.now());
        repo.save(msg);

        boolean exists = repo.existsBySenderIdAndReceiverId(1L, 2L);
        assertTrue(exists);
    }

    @Test
    void testFindByRoomIdOrderByTimestampAsc() {
        ChatMessage m1 = new ChatMessage();
        m1.setRoomId(10L);
        m1.setSenderId(1L);
        m1.setReceiverId(2L);
        m1.setContent("First");
        m1.setStatus("SENT");
        m1.setTimestamp(LocalDateTime.now().minusMinutes(5));

        ChatMessage m2 = new ChatMessage();
        m2.setRoomId(10L);
        m2.setSenderId(1L);
        m2.setReceiverId(2L);
        m2.setContent("Second");
        m2.setStatus("SENT");
        m2.setTimestamp(LocalDateTime.now());

        repo.saveAll(List.of(m2, m1));

        List<ChatMessage> messages = repo.findByRoomIdOrderByTimestampAsc(10L);
        assertEquals(2, messages.size());
        assertEquals("First", messages.get(0).getContent());
        assertEquals("Second", messages.get(1).getContent());
    }
}
