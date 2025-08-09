package ufgfans.com.ufgfans.Controller;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;
import ufgfans.com.ufgfans.Service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Random;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro"; 
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Usuario usuario, Model model) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        usuario.setOtp(otp);
        usuario.setVerificado(false);
        usuarioRepository.save(usuario);

        emailService.enviarOtp(usuario.getEmail(), otp);
        System.out.println("OTP generado para " + usuario.getEmail() + ": " + otp);

        return "redirect:/verificar?email=" + usuario.getEmail();
    }

    // Mostrar formulario para ingresar OTP
    @GetMapping("/verificar")
    public String mostrarVerificacion(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verificar"; 
    }

    // Verificar OTP
    @PostMapping("/verificar")
    public String verificar(@RequestParam String email, @RequestParam String otp, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getOtp().equals(otp)) {
            Usuario u = usuarioOpt.get();
            u.setVerificado(true);
            usuarioRepository.save(u);
            // Redirigir para completar datos
            return "redirect:/completar-registro?email=" + email;
        } else {
            model.addAttribute("mensaje", "OTP incorrecto.");
            model.addAttribute("email", email);
            return "verificar";
        }
    }

    // Mostrar formulario para completar registro (nombre, password, fecha)
    @GetMapping("/completar-registro")
    public String mostrarCompletarRegistro(@RequestParam String email, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/registro"; // si no existe, redirigir a registro
        }
        model.addAttribute("usuario", usuarioOpt.get());
        return "completar-registro";
    }

    // Guardar datos adicionales
    @PostMapping("/completar-registro")
    public String completarRegistro(@ModelAttribute Usuario usuario, Model model) {
        Optional<Usuario> usuarioBD = usuarioRepository.findById(usuario.getId());
        if (usuarioBD.isPresent()) {
            Usuario u = usuarioBD.get();
            u.setNombre(usuario.getNombre());

            // Encriptar la contraseña antes de guardarla
            String encodedPassword = passwordEncoder.encode(usuario.getPassword());
            u.setPassword(encodedPassword);

            u.setFechaNacimiento(usuario.getFechaNacimiento());
            usuarioRepository.save(u);

            // Aquí solo retornas la vista "registro-exitoso"
            return "registro-exitoso";
        }
        return "redirect:/registro";
    }

}

