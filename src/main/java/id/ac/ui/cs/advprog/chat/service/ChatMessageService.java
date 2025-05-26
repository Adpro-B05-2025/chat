package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.event.ChatMessageDeletedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageEditedEvent;
import id.ac.ui.cs.advprog.chat.event.ChatMessageSentEvent;
import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import id.ac.ui.cs.advprog.chat.repository.ChatMessageRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
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
    private final Counter chatMessageSentCounter;
    private final Counter chatMessageEditedCounter;
    private final Counter chatMessageDeletedCounter;
    private final Timer messageProcessingTimer;

    public ChatMessageService(ChatMessageRepository repo, 
                             ApplicationEventPublisher publisher,
                             Counter chatMessageSentCounter,
                             Counter chatMessageEditedCounter,
                             Counter chatMessageDeletedCounter,
                             Timer messageProcessingTimer) {
        this.repo = repo;
        this.publisher = publisher;
        this.chatMessageSentCounter = chatMessageSentCounter;
        this.chatMessageEditedCounter = chatMessageEditedCounter;
        this.chatMessageDeletedCounter = chatMessageDeletedCounter;
        this.messageProcessingTimer = messageProcessingTimer;
    }

    @Override
    public ChatMessage sendMessage(ChatMessage msg) {
        return messageProcessingTimer.record(() -> {
            msg.setTimestamp(LocalDateTime.now());
            msg.setStatus("SENT");
            ChatMessage saved = repo.save(msg);
            publisher.publishEvent(new ChatMessageSentEvent(saved));
            chatMessageSentCounter.increment();
            return saved;
        });
    }

    /** Update (soft-edit) */
    @Override
    public Optional<ChatMessage> editMessage(Long id, String newContent) {
        return messageProcessingTimer.record(() -> {
            Optional<ChatMessage> opt = repo.findById(id);
            opt.ifPresent(msg -> {
                msg.setContent(newContent);
                msg.setStatus("EDITED");
                publisher.publishEvent(new ChatMessageEditedEvent(msg));
                chatMessageEditedCounter.increment();
            });
            return opt;
        });
    }

    /** Delete (soft-delete) */
    @Override
    public Optional<ChatMessage> deleteMessage(Long id) {
        return messageProcessingTimer.record(() -> {
            Optional<ChatMessage> opt = repo.findById(id);
            opt.ifPresent(msg -> {
                msg.setContent("[Message deleted]");
                msg.setStatus("DELETED");
                publisher.publishEvent(new ChatMessageDeletedEvent(msg));
                chatMessageDeletedCounter.increment();
            });
            return opt;
        });
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

    /** Read per-room */
    @Override
    public List<ChatMessage> getMessagesByRoom(Long roomId) {
        return repo.findByRoomIdOrderByTimestampAsc(roomId);
    }
}
