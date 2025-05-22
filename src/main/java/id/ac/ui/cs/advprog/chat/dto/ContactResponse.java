package id.ac.ui.cs.advprog.chat.dto;

import java.time.LocalDateTime;

public class ContactResponse {
    private Long contactId;
    private String contactName;
    private Long roomId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;

    // Constructors
    public ContactResponse() {}

    public ContactResponse(Long contactId, String contactName, Long roomId,
                           String lastMessage, LocalDateTime lastMessageTime) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.roomId = roomId;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }

    // Getters and Setters
    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    @Override
    public String toString() {
        return "ContactResponse{" +
                "contactId=" + contactId +
                ", contactName='" + contactName + '\'' +
                ", roomId=" + roomId +
                ", lastMessage='" + lastMessage + '\'' +
                ", lastMessageTime=" + lastMessageTime +
                '}';
    }
}