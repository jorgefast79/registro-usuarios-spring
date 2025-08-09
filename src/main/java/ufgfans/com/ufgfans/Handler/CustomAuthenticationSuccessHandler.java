package ufgfans.com.ufgfans.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println("onAuthenticationSuccess invoked for user: " + authentication.getName());
                                            
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        if (usuario.getSecretKey() == null || usuario.getSecretKey().isEmpty()) {
            response.sendRedirect("/google-auth/setup?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8));
        } else {
            response.sendRedirect("/google-auth/validate?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8));
        }
    }
}

