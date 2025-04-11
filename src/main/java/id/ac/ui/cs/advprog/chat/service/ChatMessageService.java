package com.adpro.chat.service;

import com.adpro.chat.model.ChatMessage;
import com.adpro.chat.repository.ChatMessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.adpro.chat.event.ChatMessageSentEvent;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class ChatMessageService {

    private final ChatMessageRepository repo;
    private final ApplicationEventPublisher publisher;

    public ChatMessageService(ChatMessageRepository repo, ApplicationEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    public ChatMessage sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("sent");
        ChatMessage saved = repo.save(message);
        publisher.publishEvent(new ChatMessageSentEvent(saved));
        return saved;
    }

    public Optional<ChatMessage> editMessage(Long messageId, String newContent) {
        return repo.findById(messageId).map(msg -> {
            msg.setContent(newContent);
            msg.setStatus("edited");
            return repo.save(msg);
        });
    }
}
