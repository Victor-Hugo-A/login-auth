package com.example.login_auth_api.infra.security;

import com.example.login_auth_api.repositories.UserRepositories;
import com.example.login_auth_api.domain.user.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SecurityFilter extends OncePerRequestFilter {
    @Autowired
    TokenService tokenService;
    @Autowired
    UserRepositories userRepository;

@SuppressWarnings("null")
@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if(path.equals("/auth/login") || path.equals("/auth/register") || path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
        // Recupera o token JWT do cabeçalho Authorization
        var token = this.recoverToken(request);

        // Valida o token e obtém o login (email) do usuário
        var login = tokenService.validateToken(token);

        // Se o login for válido, busca o usuário no banco de dados
        if (login != null) {
            User user = userRepository.findByEmail(login).orElse(null);
        if (user != null) {
            // Cria uma lista de autoridades (roles) para o usuário
            var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

            // Cria o objeto de autenticação do Spring Security
            var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

            // Define o usuário autenticado no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            System.out.println("User not found for email: " + login);
            }
        } else {
            System.out.println("Token inválido ou expirado");
        }
        // Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    } catch (RuntimeException ex) {
        System.out.println("Erro de autenticação: " + ex.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Token inválido ou expirado");
    }
}

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}