package ufgfans.com.ufgfans.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Método original para enviar correos genéricos
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(to);
            mensaje.setSubject(subject);
            mensaje.setText(text);
            mailSender.send(mensaje);
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo a " + to);
            e.printStackTrace(); // Esto imprime el detalle del error en consola
        }
    }

    // Nuevo método específico para OTP
    public void enviarOtp(String destinatario, String otp) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject("Tu código OTP");
            mensaje.setText("Tu código de verificación es: " + otp);
            mailSender.send(mensaje);
            System.out.println("✅ OTP enviado correctamente a " + destinatario);
        } catch (Exception e) {
            System.err.println("❌ Error enviando OTP a " + destinatario);
            e.printStackTrace(); // Esto imprime la excepción completa
        }
    }
}
