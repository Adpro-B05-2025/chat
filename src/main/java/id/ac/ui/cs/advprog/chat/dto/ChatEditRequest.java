package id.ac.ui.cs.advprog.chat.dto;

public class ChatEditRequest {
    private Long roomId;
    private Long id;
    private String newContent;

    public ChatEditRequest() {
    }

    public ChatEditRequest(Long roomId, Long id, String newContent) {
        this.roomId = roomId;
        this.id = id;
        this.newContent = newContent;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}
