package com.example.login_auth_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping
    public ResponseEntity <String> getUser(){
        // Implementar lógica para obter detalhes do usuário
        return ResponseEntity.ok("sucesso!");
    }
}
