package com.notekeeper.notekeeper.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private UserDTO user;
    private boolean requiresTwoFactor; // Add 2FA flag

    public LoginResponse(boolean success, String message, String token, UserDTO user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
        this.requiresTwoFactor = false; // Default to false
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
    
    public boolean isRequiresTwoFactor() {
        return requiresTwoFactor;
    }
    
    public void setRequiresTwoFactor(boolean requiresTwoFactor) {
        this.requiresTwoFactor = requiresTwoFactor;
    }
}