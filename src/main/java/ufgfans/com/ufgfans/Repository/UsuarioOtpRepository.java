package ufgfans.com.ufgfans.Repository;

import ufgfans.com.ufgfans.Model.Usuario;
import ufgfans.com.ufgfans.Model.UsuarioOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioOtpRepository extends JpaRepository<UsuarioOtp, Long> {
    Optional<UsuarioOtp> findTopByUsuarioOrderByCreadoEnDesc(Usuario usuario);

    // Buscar todos los OTP expirados
    List<UsuarioOtp> findByOtpExpirationBefore(LocalDateTime now);

    // Buscar todos los OTP usados o expirados
    List<UsuarioOtp> findByUsadoTrueOrOtpExpirationBefore(LocalDateTime now);
}
