package id.ac.ui.cs.advprog.chat.repository;

import id.ac.ui.cs.advprog.chat.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {}
