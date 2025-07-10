package com.example.login_auth_api.repositories;

import com.example.login_auth_api.domain.user.PasswordResetToken;
import com.example.login_auth_api.domain.user.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepositories extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}  
