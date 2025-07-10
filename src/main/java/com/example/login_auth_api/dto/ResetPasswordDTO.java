package com.example.login_auth_api.dto;

public record ResetPasswordDTO(
    String token,
    String newPassword
) {}
