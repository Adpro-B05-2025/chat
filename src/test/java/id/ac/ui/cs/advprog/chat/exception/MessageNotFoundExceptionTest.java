package id.ac.ui.cs.advprog.chat.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageNotFoundExceptionTest {

    @Test
    void testExceptionMessageFormat() {
        Long id = 42L;
        MessageNotFoundException ex = new MessageNotFoundException(id);
        assertEquals("Message not found with id: 42", ex.getMessage());
    }
}
