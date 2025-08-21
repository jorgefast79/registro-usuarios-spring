package ufgfans.com.ufgfans.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;

import org.springframework.ui.Model;
import java.security.Principal;


@Controller
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/dashboard")
    public String dashboard(
            Model model, 
            Principal principal,
            @RequestParam(value = "timeout", required = false) String timeout,
            @RequestParam(value = "expired", required = false) String expired
    ) {
        String email = principal.getName(); // email del usuario autenticado
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        model.addAttribute("nombre", usuario != null ? usuario.getNombre() : "Usuario");

        // Agregar mensajes según parámetros
        if (timeout != null) {
            model.addAttribute("mensaje", "Tu sesión ha expirado. Por favor, inicia sesión de nuevo.");
            model.addAttribute("tipoMensaje", "error");
        } else if (expired != null) {
            model.addAttribute("mensaje", "Otra sesión inició con tu usuario. Esta sesión se cerró.");
            model.addAttribute("tipoMensaje", "error");
        }

        return "dashboard"; // vista dashboard.html
    }
}

