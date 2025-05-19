package id.ac.ui.cs.advprog.chat.controller;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.repository.ChatMessageRepository;
import id.ac.ui.cs.advprog.chat.security.UserPrincipal;
import id.ac.ui.cs.advprog.chat.service.IChatMessageService;
import id.ac.ui.cs.advprog.chat.exception.MessageNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat/messages")
@CrossOrigin(origins = "*")
public class ChatController {

    private final IChatMessageService service;
    private final ChatMessageRepository repo;  // inject repository

    public ChatController(IChatMessageService service, ChatMessageRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping
    public ChatMessageResponse sendMessage(@RequestBody SendMessageRequest req) {
        UserPrincipal me = (UserPrincipal)
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long meId = me.getId();

        // override senderId sesuai token
        req.setSenderId(meId);

        // jika doctor → req.getReceiverId() adalah pacillianId,
        // cek pacillian sudah pernah buka chat:
        boolean isDoctor = me.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_CAREGIVER"));
        if (isDoctor) {
            Long pacillianId = req.getReceiverId();
            if (!repo.existsBySenderIdAndReceiverId(pacillianId, meId)) {
                throw new AccessDeniedException(
                        "Doctor hanya boleh membalas chat yang sudah dibuka pacillian");
            }
        }

        ChatMessage msg = new ChatMessage();
        msg.setSenderId(req.getSenderId());
        msg.setReceiverId(req.getReceiverId());
        msg.setContent(req.getContent());
        ChatMessage sent = service.sendMessage(msg);
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
