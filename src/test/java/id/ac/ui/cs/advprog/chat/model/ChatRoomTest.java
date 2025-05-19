package id.ac.ui.cs.advprog.chat.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChatRoomTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatRoom room = new ChatRoom();
        room.setId(100L);
        room.setPacilianId(10L);
        room.setDoctorId(20L);

        assertEquals(100L, room.getId());
        assertEquals(10L, room.getPacilianId());
        assertEquals(20L, room.getDoctorId());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        Long id = 1L;
        Long pac = 11L;
        Long doc = 22L;
        ChatRoom room = new ChatRoom(id, pac, doc);

        assertEquals(id, room.getId());
        assertEquals(pac, room.getPacilianId());
        assertEquals(doc, room.getDoctorId());
    }

    @Test
    void testConvenienceConstructor() {
        Long pac = 33L;
        Long doc = 44L;
        ChatRoom room = new ChatRoom(pac, doc);

        assertNull(room.getId(), "id belum di-set oleh JPA so harus null");
        assertEquals(pac, room.getPacilianId());
        assertEquals(doc, room.getDoctorId());
    }
}
