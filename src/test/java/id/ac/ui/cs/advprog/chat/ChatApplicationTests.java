package id.ac.ui.cs.advprog.chat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatApplicationTest {

    @Test
    void contextLoads() {
        ChatApplication.main(new String[] {});
    }

    @Test
    void testCorsConfigurerBean() {
        ChatApplication app = new ChatApplication();
        WebMvcConfigurer configurer = app.corsConfigurer();
        assertNotNull(configurer); // basic check
    }
}