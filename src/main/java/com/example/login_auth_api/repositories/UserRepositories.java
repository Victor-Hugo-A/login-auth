package com.example.login_auth_api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.login_auth_api.domain.user.User;

import java.util.Optional;

public interface UserRepositories extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);
}
