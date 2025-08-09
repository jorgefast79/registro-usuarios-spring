package ufgfans.com.ufgfans.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public void asociarClaveGoogleAuth(String email, String secret) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setSecretKey(secret);
        usuarioRepository.save(usuario);
    }
}

