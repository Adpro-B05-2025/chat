package id.ac.ui.cs.advprog.chat.dto;

public class ChatDeleteRequest {
    private Long roomId;
    private Long id;

    public ChatDeleteRequest() {
    }

    public ChatDeleteRequest(Long roomId, Long id) {
        this.roomId = roomId;
        this.id = id;
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
}
