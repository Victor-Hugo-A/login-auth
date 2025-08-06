package com.example.login_auth_api.exception;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErroResponse(String mensagem,
                           int status,
                           LocalDateTime timestamp) {
}
