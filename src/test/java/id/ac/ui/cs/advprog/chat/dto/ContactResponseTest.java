package id.ac.ui.cs.advprog.chat.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ContactResponseTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        ContactResponse cr = new ContactResponse();
        LocalDateTime now = LocalDateTime.now();

        cr.setContactId(1L);
        cr.setContactName("Dr. Caleb");
        cr.setRoomId(101L);
        cr.setLastMessage("See you soon");
        cr.setLastMessageTime(now);

        assertEquals(1L, cr.getContactId());
        assertEquals("Dr. Caleb", cr.getContactName());
        assertEquals(101L, cr.getRoomId());
        assertEquals("See you soon", cr.getLastMessage());
        assertEquals(now, cr.getLastMessageTime());
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        LocalDateTime timestamp = LocalDateTime.now();
        ContactResponse cr = new ContactResponse(2L, "Karolina", 202L, "Take care", timestamp);

        assertEquals(2L, cr.getContactId());
        assertEquals("Karolina", cr.getContactName());
        assertEquals(202L, cr.getRoomId());
        assertEquals("Take care", cr.getLastMessage());
        assertEquals(timestamp, cr.getLastMessageTime());
    }

    @Test
    void testToStringContainsAllFields() {
        LocalDateTime time = LocalDateTime.of(2025, 6, 13, 12, 0);
        ContactResponse cr = new ContactResponse(3L, "Caleb", 303L, "I'll wait", time);

        String str = cr.toString();
        assertTrue(str.contains("3"));
        assertTrue(str.contains("Caleb"));
        assertTrue(str.contains("303"));
        assertTrue(str.contains("I'll wait"));
        assertTrue(str.contains("2025"));
    }
}
