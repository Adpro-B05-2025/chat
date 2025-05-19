package id.ac.ui.cs.advprog.chat.repository;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    /**
     * Find all messages in the given room, ordered by timestamp ascending.
     */
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(Long roomId);
}
