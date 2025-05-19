package id.ac.ui.cs.advprog.chat.dto;

public class ChatEditRequest {
    private Long id;
    private String newContent;

    public ChatEditRequest() {
    }

    public ChatEditRequest(Long id, String newContent) {
        this.id = id;
        this.newContent = newContent;
    }

    public Long getId() {
        return id;
    }

    public String getNewContent() {
        return newContent;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNewContent(String newContent) {
        this.newContent = newContent;
    }
}
