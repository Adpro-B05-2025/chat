package id.ac.ui.cs.advprog.chat.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.repository.ChatRoomRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository repo;

    @Mock
    private Counter counter;

    @Mock
    private Timer timer;

    @InjectMocks
    private ChatRoomService svc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Intercept all record(Supplier<T>) calls and just execute the supplier directly
        lenient().when(timer.record(any(Supplier.class))).thenAnswer(inv -> {
            Supplier<?> supplier = inv.getArgument(0);
            return supplier.get();
        });
    }

    @Test
    void createIfNotExists_returnsExistingRoom() {
        ChatRoom existing = new ChatRoom(1L, 10L, 20L);
        when(repo.findByPacilianIdAndDoctorId(10L, 20L)).thenReturn(Optional.of(existing));

        ChatRoom result = svc.createIfNotExists(10L, 20L);

        assertSame(existing, result);
        verify(repo, never()).save(any());
    }

    @Test
    void createIfNotExists_savesNewRoomWhenAbsent() {
        when(repo.findByPacilianIdAndDoctorId(11L, 22L)).thenReturn(Optional.empty());

        ChatRoom saved = new ChatRoom(2L, 11L, 22L);
        when(repo.save(any(ChatRoom.class))).thenReturn(saved);

        ChatRoom result = svc.createIfNotExists(11L, 22L);

        assertEquals(saved.getId(), result.getId());
        verify(counter).increment();
    }

    @Test
    void find_returnsEmptyIfNotFound() {
        when(repo.findById(5L)).thenReturn(Optional.empty());

        Optional<ChatRoom> result = svc.find(5L);

        assertTrue(result.isEmpty());
    }

    @Test
    void find_returnsRoomIfExists() {
        ChatRoom room = new ChatRoom(5L, 33L, 44L);
        when(repo.findById(5L)).thenReturn(Optional.of(room));

        Optional<ChatRoom> result = svc.find(5L);

        assertTrue(result.isPresent());
        assertEquals(room, result.get());
    }

    @Test
    void findRoomsByUserId_returnsAllRoomsWhereUserIsParticipant() {
        ChatRoom room1 = new ChatRoom(1L, 100L, 200L);
        ChatRoom room2 = new ChatRoom(2L, 200L, 100L);
        when(repo.findByPacilianIdOrDoctorId(100L, 100L)).thenReturn(List.of(room1, room2));

        List<ChatRoom> rooms = svc.findRoomsByUserId(100L);

        assertEquals(2, rooms.size());
    }
}
