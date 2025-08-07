package com.example.login_auth_api.infra.security;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.example.login_auth_api.domain.user.User;

import jakarta.annotation.PostConstruct;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")

    private String secret;

    @PostConstruct
    public void printSecret() {
        System.out.println("JWT Secret: " + secret);
    }

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("login-auth-api")
                    .withSubject(user.getEmail())
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error generating JWT token");
        }
    }

    public String validateToken (String token) {
        try {
            // Remove o prefixo "Bearer " se existir
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // Configura o algoritmo de verificação
            Algorithm algorithm = Algorithm.HMAC256(secret);
            assert token != null;
            return JWT.require(algorithm)
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (AuthenticationException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusSeconds(3600).toInstant(ZoneOffset.ofHours(-3)); // Token valid for 1 hour
    }
}
