package com.example.login_auth_api.controllers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.example.login_auth_api.domain.user.User;
import com.example.login_auth_api.dto.*;
import com.example.login_auth_api.domain.user.PasswordResetToken;
import com.example.login_auth_api.repositories.PasswordResetTokenRepositories;
import com.example.login_auth_api.repositories.UserRepositories;
import com.example.login_auth_api.services.EmailService;
import com.example.login_auth_api.infra.security.TokenService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepositories repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final PasswordResetTokenRepositories PasswordResetTokenRepository;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@RequestBody LoginRequestDTO body) {
        // Sua lógica de autenticação aqui
        User user = this.repository.findByEmail(body.email()).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        if(passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@RequestBody RegisterRequestDTO body) {
        // Validação de senha (mínimo 6 caracteres)
        if (body.password().length() < 6) {
            return ResponseEntity.badRequest().body(new ResponseDTO("O password necessita ter 6 caracteres", null));
        }

        // Verifica se já existe um usuário com o mesmo email no banco de dados
        if (repository.findByEmail(body.email()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(new ResponseDTO("Já existe um usuário cadastrado com esse EMAIL", null));
        }

        try {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            this.repository.save(newUser);

            String token = this.tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ResponseDTO("Erro ao registrar usuário", null ));
        }
    }

    @PostMapping("/forgot")
    public ResponseEntity<ResponseDTO> forgotPassword(@RequestBody Map<String, String> request) {

        try {
            String email = request.get("email");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResponseDTO("O e-mail é obrigatório", null));
            }

            Optional<User> userOptional = this.repository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String token = UUID.randomUUID().toString();

                PasswordResetToken resetTokenEntity = new PasswordResetToken(
                        token,
                        user,
                        LocalDateTime.now().plusHours(24) // Expirando em 24h
                );
                PasswordResetTokenRepository.save(resetTokenEntity);

                String resetLink = "http://localhost:4200/forgot?token=" + token;
                emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            }
            return ResponseEntity.ok(new ResponseDTO("Se o email estiver cadastrado, você receberá um link de recuperação", null));
        } catch (Exception e) {
            log.error("Erro ao processar recuperação de senha", e);
            return ResponseEntity.internalServerError()
                    .body(new ResponseDTO("Erro ao processar sua solicitação", null));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ResponseDTO> resetPassword(@RequestBody ResetPasswordDTO request) {
        try {
            PasswordResetToken resetToken = PasswordResetTokenRepository.findByToken(request.token())
                    .orElseThrow(() -> new RuntimeException("Token inválido"));

            if (resetToken.getExpTime().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Token expirado");
            }

            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            repository.save(user);

            PasswordResetTokenRepository.delete(resetToken);

            return ResponseEntity.ok()
                    .body(new ResponseDTO("Senha redefinida com sucesso", null));

        } catch (Exception e) {
            log.error("Erro ao redefinir senha", e);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO(e.getMessage(), null));
        }
    }


    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDTO> deleteUser(@RequestBody DeleteDTO body) {
        String email = body.getEmail();
        Optional<User> user = repository.findByEmail(email);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("Usuário não encontrado", null));
        }
         try {
             repository.delete(user.get());
             return ResponseEntity.ok(new ResponseDTO("Usuário deletado com sucesso!", null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ResponseDTO("Erro ao deletar usuário", null));
        }
    }
}