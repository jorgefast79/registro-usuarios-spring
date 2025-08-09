package ufgfans.com.ufgfans.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;

import org.springframework.ui.Model;
import java.security.Principal;


@Controller
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        String email = principal.getName(); // email del usuario autenticado
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        model.addAttribute("nombre", usuario != null ? usuario.getNombre() : "Usuario");
        return "dashboard"; // vista dashboard.html
    }
}

