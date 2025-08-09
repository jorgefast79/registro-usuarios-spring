package ufgfans.com.ufgfans.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas, intenta de nuevo.");
            model.addAttribute("username", username);
            model.addAttribute("password", password);
        }

        return "login";  // templates/login.html
    }


    // Manejo de error 403 para evitar Whitelabel Error Page
    @GetMapping("/403")
    public String accesoDenegado(Model model) {
        model.addAttribute("mensaje", "No tienes permisos para acceder a esta página.");
        return "error/403"; // Debes crear templates/error/403.html
    }
}
