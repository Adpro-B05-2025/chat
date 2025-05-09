package id.ac.ui.cs.advprog.chat.dto;

import java.util.List;

public class TokenValidationResponse {
    private boolean valid;
    private Long userId;
    private String username;
    private List<String> roles;

    // Getters
    public boolean isValid() {
        return valid;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    // Setters
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}