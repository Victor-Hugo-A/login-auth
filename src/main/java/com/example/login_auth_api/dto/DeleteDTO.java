package com.example.login_auth_api.dto;

public record DeleteDTO(String email) {
    public String getEmail() {
        return email;
    }
}
