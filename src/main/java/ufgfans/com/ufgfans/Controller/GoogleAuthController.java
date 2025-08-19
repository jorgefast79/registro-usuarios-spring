package ufgfans.com.ufgfans.Controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Service.GoogleAuthService;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;

@Controller
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/google-auth/setup")
    public String mostrarSetup(@RequestParam String email, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        if (usuario.getSecretKey() == null || usuario.getSecretKey().isEmpty()) {
            // Primera vez: generar clave y QR
            GoogleAuthService.GoogleAuthData data = googleAuthService.generarClaveYQR(email);
            usuario.setSecretKey(data.getSecret());
            usuarioRepository.save(usuario);

            model.addAttribute("qrUrl", data.getQrUrl());
            model.addAttribute("secret", data.getSecret());
            model.addAttribute("email", email);
            return "google-auth-setup";
        } else {
            // Ya tiene secret → pedir código directamente
            model.addAttribute("email", email);
            return "google-auth-validate";
        }
    }

    @GetMapping("/google-auth/validate")
    public String mostrarFormularioValidate(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "google-auth-validate";
    }

    @PostMapping("/google-auth/validate")
    public String validarCodigo(@RequestParam String email, @RequestParam String codigo, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        // 1. Verificar si está bloqueado
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            model.addAttribute("error", "Cuenta bloqueada hasta " + usuario.getBloqueadoHasta().toString());
            model.addAttribute("email", email);
            return "google-auth-validate";
        }

        // 2. Validar código
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean codigoValido = false;
        try {
            codigoValido = gAuth.authorize(usuario.getSecretKey(), Integer.parseInt(codigo));
        } catch (NumberFormatException e) {
            codigoValido = false;
        }

        if (codigoValido) {
            // ✅ Resetear contador de fallos
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);

            return "redirect:/dashboard";
        } else {
            // ❌ Incrementar intentos
            int fallos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(fallos);

            if (fallos >= 5) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(5));
                usuario.setIntentosFallidos(0); // reset al llegar al límite
                model.addAttribute("error", "Cuenta bloqueada por 5 minutos debido a múltiples intentos fallidos");
            } else {
                model.addAttribute("error", "Código inválido, intento " + fallos + " de 5");
            }

            usuarioRepository.save(usuario);

            model.addAttribute("email", email);
            return "google-auth-validate";
        }
    }

    @PostMapping("/google-auth/setup")
    public String validarCodigoSetup(@RequestParam String email, @RequestParam String codigo, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        if (usuario.getSecretKey() == null || usuario.getSecretKey().isEmpty()) {
            return "redirect:/google-auth/setup?email=" + email;
        }

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean codigoValido = false;
        try {
            codigoValido = gAuth.authorize(usuario.getSecretKey(), Integer.parseInt(codigo));
        } catch (NumberFormatException e) {
            codigoValido = false;
        }

        if (codigoValido) {
            // Ya no necesitamos marcar "twoFactorEnabled", el secretKey ya existe
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Código inválido, inténtalo de nuevo");
            GoogleAuthService.GoogleAuthData data = new GoogleAuthService.GoogleAuthData(usuario.getSecretKey(), "");
            model.addAttribute("qrUrl", data.getQrUrl());
            model.addAttribute("secret", usuario.getSecretKey());
            model.addAttribute("email", email);
            return "google-auth-setup";
        }
    }

    @PostMapping("/google-auth/resend")
    public String resendCode(@RequestParam String email, Model model) {
        // Lógica para reenviar el código al email
        googleAuthService.sendNewCode(email);

        model.addAttribute("email", email);
        model.addAttribute("message", "Se ha enviado un nuevo código a tu correo.");
        return "google-auth-validate";
    }


    // También crearás endpoint para /google-auth/validate que hace lo mismo pero sin generar QR,
    // solo pedir código TOTP para usuarios ya con secret.
}


