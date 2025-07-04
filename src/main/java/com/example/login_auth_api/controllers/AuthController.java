package com.example.login_auth_api.controllers;

import java.util.Map;
import java.util.Optional;

import com.example.login_auth_api.domain.user.User;
import com.example.login_auth_api.dto.LoginRequestDTO;
import com.example.login_auth_api.dto.RegisterRequestDTO;
import com.example.login_auth_api.dto.ResponseDTO;
import com.example.login_auth_api.repositories.UserRepositories;
import com.example.login_auth_api.infra.security.TokenService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepositories repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody LoginRequestDTO body) {
        // Sua lógica de autenticação aqui
        User user = this.repository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if(passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
        public ResponseEntity<ResponseDTO> register(@RequestBody RegisterRequestDTO body) {
            // Verifica se já existe um usuário com o mesmo email no banco de dados
            if (repository.findByEmail(body.email()).isPresent()) {
                return ResponseEntity
                .badRequest()
                .body(new ResponseDTO("Já existe um usuário cadastrado com esse EMAIL", null));
            }

            // Sua lógica de autenticação aqui
            Optional<User> user = this.repository.findByEmail(body.email());

            if(user.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            this.repository.save(newUser);

                String token = this.tokenService.generateToken(newUser);
                return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
            }
            return ResponseEntity.badRequest().build();
        }

        @DeleteMapping("/delete")
    public ResponseEntity<ResponseDTO> deleteUser(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> user = repository.findByEmail(email);
        if (user.isPresent()) {
            repository.delete(user.get());
            return ResponseEntity.ok(new ResponseDTO("Usuário deletado com sucesso!", null));
        } else {
            return ResponseEntity.badRequest().body(new ResponseDTO("Usuário não encontrado.", null));
        }
    }
}