package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class ChatControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IChatMessageService chatService;

    @InjectMocks
    private ChatController chatController;

    private ObjectMapper objectMapper;

    private ChatMessage dummyMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(chatController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        dummyMessage = new ChatMessage();
        dummyMessage.setId(1L);
        dummyMessage.setSenderId(10L);
        dummyMessage.setReceiverId(20L);
        dummyMessage.setContent("Hello");
        dummyMessage.setStatus("sent");
        dummyMessage.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testSendMessage_shouldReturnSavedMessage() throws Exception {
        SendMessageRequest req = new SendMessageRequest(10L, 20L, "Hello");

        when(chatService.sendMessage(any())).thenReturn(dummyMessage);

        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.senderId").value(10L))
                .andExpect(jsonPath("$.receiverId").value(20L))
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void testGetMessage_shouldReturnMessageById() throws Exception {
        when(chatService.getMessage(1L)).thenReturn(Optional.of(dummyMessage));

        mockMvc.perform(get("/api/chat/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("Hello"));
    }

    @Test
    void testGetAllMessages_shouldReturnList() throws Exception {
        when(chatService.getAllMessages()).thenReturn(List.of(dummyMessage));

        mockMvc.perform(get("/api/chat/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void testEditMessage_shouldReturnEditedMessage() throws Exception {
        dummyMessage.setContent("Updated");
        dummyMessage.setStatus("edited");

        when(chatService.editMessage(eq(1L), eq("Updated"))).thenReturn(Optional.of(dummyMessage));

        mockMvc.perform(put("/api/chat/messages/1")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated"))
                .andExpect(jsonPath("$.status").value("edited"));
    }

    @Test
    void testDeleteMessage_shouldReturnDeletedStatus() throws Exception {
        dummyMessage.setStatus("deleted");

        when(chatService.deleteMessage(1L)).thenReturn(Optional.of(dummyMessage));

        mockMvc.perform(delete("/api/chat/messages/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("deleted"));
    }

    @Test
    void testGetMessage_shouldThrowNotFound() throws Exception {
        when(chatService.getMessage(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/chat/messages/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEditMessage_shouldThrowNotFound() throws Exception {
        when(chatService.editMessage(eq(999L), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/chat/messages/999")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("doesn't matter"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteMessage_shouldThrowNotFound() throws Exception {
        when(chatService.deleteMessage(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/chat/messages/999"))
                .andExpect(status().isNotFound());
    }
}
