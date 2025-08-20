package ufgfans.com.ufgfans.Controller;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Model.UsuarioOtp;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;
import ufgfans.com.ufgfans.Repository.UsuarioOtpRepository;
import ufgfans.com.ufgfans.Service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioOtpRepository usuarioOtpRepository;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Registro inicial ---
    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro"; 
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Usuario usuario, Model model) {
        // Validar si ya existe
        Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());
        if (existente.isPresent()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("error", "El correo ya está registrado.");
            return "registro";
        }

        usuario.setVerificado(false);
        usuarioRepository.save(usuario); // primero guardamos el usuario

        // Generar OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        UsuarioOtp usuarioOtp = new UsuarioOtp();
        usuarioOtp.setUsuario(usuario);
        usuarioOtp.setOtp(otp);
        usuarioOtp.setOtpExpiration(LocalDateTime.now().plusMinutes(10));

        usuarioOtpRepository.save(usuarioOtp);

        try {
            emailService.enviarOtp(usuario.getEmail(), otp);
            System.out.println("OTP generado para " + usuario.getEmail() + ": " + otp);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Hubo un problema enviando el correo. Intente nuevamente.");
            return "registro";
        }

        return "redirect:/verificar?email=" + usuario.getEmail();
    }

    // --- Verificación de OTP ---
    @GetMapping("/verificar")
    public String mostrarVerificacion(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verificar"; 
    }

    @PostMapping("/verificar")
    public String verificar(@RequestParam String email, @RequestParam String otp, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("mensaje", "Usuario no encontrado.");
            return "verificar";
        }

        Usuario u = usuarioOpt.get();

        // Buscar el OTP más reciente
        Optional<UsuarioOtp> otpOpt = usuarioOtpRepository.findTopByUsuarioOrderByCreadoEnDesc(u);

        if (otpOpt.isEmpty()) {
            model.addAttribute("mensaje", "No se encontró un código OTP. Registra de nuevo.");
            return "redirect:/registro";
        }

        UsuarioOtp ultimoOtp = otpOpt.get();

        // Verificar expiración
        if (ultimoOtp.getOtpExpiration().isBefore(LocalDateTime.now())) {
            usuarioRepository.delete(u); // eliminar usuario no confirmado
            model.addAttribute("mensaje", "El OTP expiró y tu cuenta fue eliminada. Regístrate de nuevo.");
            return "redirect:/registro";
        }

        // Verificar coincidencia
        if (ultimoOtp.getOtp().equals(otp)) {
            u.setVerificado(true);
            usuarioRepository.save(u);

            ultimoOtp.setUsado(true);
            usuarioOtpRepository.save(ultimoOtp);

            return "redirect:/completar-registro?email=" + email;
        } else {
            model.addAttribute("mensaje", "OTP incorrecto.");
            model.addAttribute("email", email);
            return "verificar";
        }
    }

    // --- Completar registro ---
    @GetMapping("/completar-registro")
    public String mostrarCompletarRegistro(@RequestParam String email, Model model) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return "redirect:/registro"; 
        }
        model.addAttribute("usuario", usuarioOpt.get());
        return "completar-registro";
    }

    @PostMapping("/completar-registro")
    public String completarRegistro(
            @ModelAttribute Usuario usuario,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        // Validar campos vacíos
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("mensajeError", "El nombre es obligatorio.");
            return "completar-registro";
        }

        if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("mensajeError", "La contraseña es obligatoria.");
            return "completar-registro";
        }

        if (usuario.getFechaNacimiento() == null) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("mensajeError", "La fecha de nacimiento es obligatoria.");
            return "completar-registro";
        }

        // Validar confirmación de contraseña
        if (!usuario.getPassword().equals(confirmPassword)) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("errorConfirmPassword", "Las contraseñas no coinciden.");
            return "completar-registro";
        }

        Optional<Usuario> usuarioBD = usuarioRepository.findById(usuario.getId());
        if (usuarioBD.isPresent()) {
            Usuario u = usuarioBD.get();
            u.setNombre(usuario.getNombre());

            // Encriptar contraseña
            String encodedPassword = passwordEncoder.encode(usuario.getPassword());
            u.setPassword(encodedPassword);

            u.setFechaNacimiento(usuario.getFechaNacimiento());
            usuarioRepository.save(u);

            model.addAttribute("mensajeExito", "Registro completado con éxito.");
            return "registro-exitoso";
        }

        model.addAttribute("mensajeError", "El usuario no existe. Intenta registrarte de nuevo.");
        return "completar-registro";
    }

}
