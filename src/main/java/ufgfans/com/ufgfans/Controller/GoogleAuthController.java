package ufgfans.com.ufgfans.Controller;

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
    public String mostrarFormularioSetup(@RequestParam String email, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        // Si aún no tiene clave secreta, se la generamos
        String secretKey = usuario.getSecretKey();
        if (secretKey == null || secretKey.isEmpty()) {
            GoogleAuthService.GoogleAuthData data = googleAuthService.generarClaveYQR(email);
            secretKey = data.getSecret();
            usuario.setSecretKey(secretKey);
            usuarioRepository.save(usuario);

            model.addAttribute("qrUrl", data.getQrUrl());
            model.addAttribute("secret", secretKey);
        } else {
            // Si ya tiene clave, solo mostramos el QR que corresponda
            GoogleAuthService.GoogleAuthData data = new GoogleAuthService.GoogleAuthData(secretKey, "");
            model.addAttribute("qrUrl", data.getQrUrl());
            model.addAttribute("secret", secretKey);
        }

        model.addAttribute("email", email);
        return "google-auth-setup"; // vista Thymeleaf para escanear QR
    }

    @GetMapping("/google-auth/validate")
    public String mostrarFormularioValidate(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "google-auth-validate";  // archivo thymeleaf google-auth-validate.html
    }

    @PostMapping("/google-auth/validate")
    public String validarCodigo(@RequestParam String email, @RequestParam String codigo, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        String secretKey = usuario.getSecretKey();
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean codigoValido = gAuth.authorize(secretKey, Integer.parseInt(codigo));

        if (codigoValido) {
            // Código correcto, redirige al dashboard o donde quieras
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Código inválido, inténtalo de nuevo");
            model.addAttribute("email", email);
            return "google-auth-validate";  // una vista para pedir el código, por ejemplo
        }
    }


    @PostMapping("/google-auth/setup")
    public String validarCodigoSetup(@RequestParam String email, @RequestParam String codigo, Model model) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();

        // Obtén el secreto (debería estar ya guardado en DB, sino no funcionará)
        String secretKey = usuario.getSecretKey();

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean codigoValido = gAuth.authorize(secretKey, Integer.parseInt(codigo));

        if (codigoValido) {
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Código inválido, inténtalo de nuevo");
            // Reenvía datos para que pueda volver a probar
            GoogleAuthService.GoogleAuthData data = new GoogleAuthService.GoogleAuthData(secretKey, "");
            model.addAttribute("qrUrl", data.getQrUrl());
            model.addAttribute("secret", secretKey);
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


