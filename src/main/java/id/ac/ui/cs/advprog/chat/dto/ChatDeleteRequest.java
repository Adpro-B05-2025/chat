package id.ac.ui.cs.advprog.chat.dto;

public class ChatDeleteRequest {
    private Long id;

    public ChatDeleteRequest() {
    }

    public ChatDeleteRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
