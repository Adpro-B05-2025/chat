package id.ac.ui.cs.advprog.chat.repository;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository repo;

    @Test
    void testFindByPacilianIdAndDoctorId_whenExists() {
        ChatRoom room = new ChatRoom(100L, 200L);
        repo.save(room);

        Optional<ChatRoom> result = repo.findByPacilianIdAndDoctorId(100L, 200L);
        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getPacilianId());
        assertEquals(200L, result.get().getDoctorId());
    }

    @Test
    void testFindByPacilianIdOrDoctorId_returnsAllMatching() {
        ChatRoom r1 = new ChatRoom(1L, 2L);
        ChatRoom r2 = new ChatRoom(3L, 1L);
        ChatRoom r3 = new ChatRoom(4L, 5L);
        repo.saveAll(List.of(r1, r2, r3));

        List<ChatRoom> rooms = repo.findByPacilianIdOrDoctorId(1L, 1L);
        assertEquals(2, rooms.size());
    }
}
