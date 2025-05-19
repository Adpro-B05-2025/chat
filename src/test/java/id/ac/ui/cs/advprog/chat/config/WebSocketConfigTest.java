// src/test/java/id/ac/ui/cs/advprog/chat/config/WebSocketConfigTest.java
package id.ac.ui.cs.advprog.chat.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebSocketConfig.class)
class WebSocketConfigTest {

    @Autowired
    private WebSocketConfig webSocketConfig;

    @Test
    void webSocketConfigBeanLoads() {
        assertNotNull(webSocketConfig, "WebSocketConfig harus ter-load sebagai bean");
    }
}
