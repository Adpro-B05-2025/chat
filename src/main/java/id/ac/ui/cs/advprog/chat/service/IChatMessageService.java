package id.ac.ui.cs.advprog.chat.service;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;

import java.util.List;
import java.util.Optional;

public interface IChatMessageService {

    /** Create */
    ChatMessage sendMessage(ChatMessage message);

    /** Read single */
    Optional<ChatMessage> getMessage(Long messageId);

    /** Read all */
    List<ChatMessage> getAllMessages();

    /** Read history for a room */
    List<ChatMessage> getMessagesByRoom(Long roomId);

    /** Update */
    Optional<ChatMessage> editMessage(Long messageId, String newContent);

    /** “Delete” (soft delete) */
    Optional<ChatMessage> deleteMessage(Long messageId);
}
