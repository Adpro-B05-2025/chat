// src/main/java/id/ac/ui/cs/advprog/chat/model/ChatMessage.java
package id.ac.ui.cs.advprog.chat.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;       // ← baru
    private Long senderId;
    private Long receiverId;
    private String content;
    private String status;
    private LocalDateTime timestamp;

    public ChatMessage() {
    }

    public ChatMessage(Long id,
                       Long roomId,
                       Long senderId,
                       Long receiverId,
                       String content,
                       String status,
                       LocalDateTime timestamp) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getter/Setter untuk id
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // Getter/Setter untuk roomId
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}