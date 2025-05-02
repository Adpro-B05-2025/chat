package id.ac.ui.cs.advprog.chat.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IChatMessageServiceTest {

    @Autowired
    private IChatMessageService chatService;

    @Test
    void contextLoads_andIsImplementation() {
        assertNotNull(chatService, "Service harus ter-inject oleh Spring");
        assertTrue(chatService instanceof ChatMessageService,
                "Implementasi default harus ChatMessageService");
    }
}
