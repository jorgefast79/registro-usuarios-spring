package ufgfans.com.ufgfans.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Model.UsuarioOtp;
import ufgfans.com.ufgfans.Repository.UsuarioRepository;
import ufgfans.com.ufgfans.Repository.UsuarioOtpRepository;

@Service
public class OtpService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioOtpRepository usuarioOtpRepository;

    // Generar un nuevo OTP (con validación de duplicados / expiración)
    public String generateOtp(String email) {
        Optional<Usuario> existingUser = usuarioRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            throw new RuntimeException("El usuario no existe.");
        }

        Usuario usuario = existingUser.get();

        // Buscar el último OTP del usuario
        Optional<UsuarioOtp> lastOtpOpt = usuarioOtpRepository.findTopByUsuarioOrderByCreadoEnDesc(usuario);

        if (lastOtpOpt.isPresent()) {
            UsuarioOtp lastOtp = lastOtpOpt.get();

            // Si aún no expiró, no dejamos generar uno nuevo
            if (lastOtp.getOtpExpiration().isAfter(LocalDateTime.now()) && !lastOtp.isUsado()) {
                throw new RuntimeException("Ya tienes un OTP válido, revisa tu correo.");
            }
        }

        // Generar nuevo OTP
        String otp = generateRandomOtp();

        UsuarioOtp nuevoOtp = new UsuarioOtp();
        nuevoOtp.setUsuario(usuario);
        nuevoOtp.setOtp(otp);
        nuevoOtp.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        nuevoOtp.setUsado(false);

        usuarioOtpRepository.save(nuevoOtp);

        return otp;
    }

    // Validar un OTP
    public boolean validateOtp(String email, String otpIngresado) {
        Optional<Usuario> existingUser = usuarioRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            return false;
        }

        Usuario usuario = existingUser.get();

        // Buscar el último OTP generado
        Optional<UsuarioOtp> lastOtpOpt = usuarioOtpRepository.findTopByUsuarioOrderByCreadoEnDesc(usuario);

        if (lastOtpOpt.isEmpty()) {
            return false;
        }

        UsuarioOtp lastOtp = lastOtpOpt.get();

        // Validar
        if (lastOtp.getOtpExpiration().isBefore(LocalDateTime.now())) {
            return false; // Expirado
        }

        if (!lastOtp.getOtp().equals(otpIngresado)) {
            return false; // Incorrecto
        }

        // Si es válido, lo marcamos como usado
        lastOtp.setUsado(true);
        usuarioOtpRepository.save(lastOtp);

        return true;
    }

    // Generador aleatorio de OTP de 6 dígitos
    private String generateRandomOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
