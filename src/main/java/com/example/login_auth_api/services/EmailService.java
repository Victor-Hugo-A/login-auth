package com.example.login_auth_api.services;

import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService { 
    private final JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Redefinição de Senha");
        message.setText("Para redefinir sua senha, clique no link abaixo:\n" + resetLink + 
                      "\n\nO link expirará em 24 horas." +
                      "\n\nSe você não solicitou esta redefinição, ignore este e-mail.");

        mailSender.send(message);
    }

}
