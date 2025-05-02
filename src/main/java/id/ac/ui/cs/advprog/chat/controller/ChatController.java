package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.dto.SendMessageRequest;
import id.ac.ui.cs.advprog.chat.dto.ChatMessageResponse;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import id.ac.ui.cs.advprog.chat.exception.MessageNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat/messages")
@CrossOrigin(origins = "*")
public class ChatController {

    private final IChatMessageService service;

    public ChatController(IChatMessageService service) {
        this.service = service;
    }

    @PostMapping
    public ChatMessageResponse sendMessage(@RequestBody SendMessageRequest request) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(request.getSenderId());
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        ChatMessage sent = service.sendMessage(message);
        return toResponse(sent);
    }

    @GetMapping("/{id}")
    public ChatMessageResponse getMessage(@PathVariable Long id) {
        ChatMessage message = service.getMessage(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
        return toResponse(message);
    }

    @GetMapping
    public List<ChatMessageResponse> getAllMessages() {
        return service.getAllMessages().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public ChatMessageResponse editMessage(@PathVariable Long id,
                                           @RequestBody String newContent) {
        ChatMessage updated = service.editMessage(id, newContent)
                .orElseThrow(() -> new MessageNotFoundException(id));
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    public ChatMessageResponse deleteMessage(@PathVariable Long id) {
        ChatMessage deleted = service.deleteMessage(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
        return toResponse(deleted);
    }

    private ChatMessageResponse toResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getId(),
                msg.getSenderId(),
                msg.getReceiverId(),
                msg.getContent(),
                msg.getStatus(),
                msg.getTimestamp()
        );
    }
}
