package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.model.ChatRoom;
import id.ac.ui.cs.advprog.chat.repository.ChatRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository repo;

    private ChatRoomService svc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        svc = new ChatRoomService(repo);
    }

    @Test
    void createIfNotExists_returnsExistingRoom() {
        ChatRoom existing = new ChatRoom(1L, 10L, 20L);
        when(repo.findByPacilianIdAndDoctorId(10L,20L))
                .thenReturn(Optional.of(existing));

        ChatRoom result = svc.createIfNotExists(10L,20L);

        assertSame(existing, result);
        verify(repo, never()).save(any());
    }

    @Test
    void createIfNotExists_savesNewRoomWhenAbsent() {
        when(repo.findByPacilianIdAndDoctorId(11L,22L))
                .thenReturn(Optional.empty());
        ArgumentCaptor<ChatRoom> cap = ArgumentCaptor.forClass(ChatRoom.class);
        when(repo.save(cap.capture())).thenAnswer(inv -> {
            ChatRoom r = inv.getArgument(0);
            r.setId(99L);
            return r;
        });

        ChatRoom result = svc.createIfNotExists(11L,22L);

        assertEquals(99L, result.getId());
        assertEquals(11L, result.getPacilianId());
        assertEquals(22L, result.getDoctorId());
    }

    @Test
    void find_returnsEmptyIfNotFound() {
        when(repo.findById(5L)).thenReturn(Optional.empty());
        assertTrue(svc.find(5L).isEmpty());
    }

    @Test
    void find_returnsRoomIfExists() {
        ChatRoom room = new ChatRoom(5L, 33L, 44L);
        when(repo.findById(5L)).thenReturn(Optional.of(room));
        Optional<ChatRoom> opt = svc.find(5L);
        assertTrue(opt.isPresent());
        assertEquals(room, opt.get());
    }
}
