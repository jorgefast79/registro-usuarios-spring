package ufgfans.com.ufgfans.Repository;

import ufgfans.com.ufgfans.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    // Eliminar usuarios cuyo OTP ya expiró
    @Modifying
    @Transactional
    @Query("DELETE FROM Usuario u WHERE u.email = :email AND u.otpExpiration < CURRENT_TIMESTAMP")
    void deleteExpiredOtpByEmail(@Param("email") String email);
}


