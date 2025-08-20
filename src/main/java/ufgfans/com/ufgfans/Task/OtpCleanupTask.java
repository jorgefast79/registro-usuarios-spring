package ufgfans.com.ufgfans.Task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ufgfans.com.ufgfans.Repository.UsuarioOtpRepository;
import ufgfans.com.ufgfans.Model.UsuarioOtp;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OtpCleanupTask {

    private final UsuarioOtpRepository usuarioOtpRepository;

    public OtpCleanupTask(UsuarioOtpRepository usuarioOtpRepository) {
        this.usuarioOtpRepository = usuarioOtpRepository;
    }

    // Se ejecuta cada hora (puedes ajustar la expresión cron)
    @Scheduled(cron = "0 0 * * * *")
    public void cleanExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();

        List<UsuarioOtp> expirados = usuarioOtpRepository.findByUsadoTrueOrOtpExpirationBefore(now);

        if (!expirados.isEmpty()) {
            usuarioOtpRepository.deleteAll(expirados);
            System.out.println("🧹 Limpieza OTP: eliminados " + expirados.size() + " registros.");
        }
    }
}
