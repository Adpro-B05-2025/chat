package id.ac.ui.cs.advprog.chat.listener;

import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ChatAuditLoggerTest {

    private ChatAuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new ChatAuditLogger();
    }

    @Test
    void testOnMessageSent_logsToConsole() {
        // Redirect System.out to capture output
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Setup dummy message
        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setSenderId(10L);
        msg.setReceiverId(20L);
        msg.setContent("Hello");
        msg.setStatus("sent");
        msg.setTimestamp(LocalDateTime.now());

        // Send event
        ChatMessageSentEvent event = new ChatMessageSentEvent(msg);
        logger.onMessageSent(event);

        // Verify console output
        String output = outContent.toString().trim();
        assertTrue(output.contains("Message sent: Hello"));

        // Reset System.out
        System.setOut(System.out);
    }
}
