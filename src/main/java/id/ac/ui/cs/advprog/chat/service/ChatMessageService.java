package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.repository.ChatMessageRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatMessageService implements IChatMessageService {

    private final ChatMessageRepository repo;
    private final ApplicationEventPublisher publisher;

    public ChatMessageService(ChatMessageRepository repo,
                              ApplicationEventPublisher publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    /** Create */
    @Override
    public ChatMessage sendMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        message.setStatus("sent");
        ChatMessage saved = repo.save(message);
        publisher.publishEvent(new ChatMessageSentEvent(saved));
        return saved;
    }

    /** Read single */
    @Override
    public Optional<ChatMessage> getMessage(Long messageId) {
        return repo.findById(messageId);
    }

    /** Read all */
    @Override
    public List<ChatMessage> getAllMessages() {
        return repo.findAll();
    }

    /** Read history for a room */
    public List<ChatMessage> getMessagesByRoom(Long roomId) {
        return repo.findByRoomIdOrderByTimestampAsc(roomId);
    }

    /** Update */
    @Override
    public Optional<ChatMessage> editMessage(Long messageId,
                                             String newContent) {
        return repo.findById(messageId)
                .map(msg -> {
                    msg.setContent(newContent);
                    msg.setStatus("edited");
                    return repo.save(msg);
                });
    }

    /** “Delete” (soft delete) */
    @Override
    public Optional<ChatMessage> deleteMessage(Long messageId) {
        return repo.findById(messageId)
                .map(msg -> {
                    msg.setStatus("deleted");
                    return repo.save(msg);
                });
    }
}
