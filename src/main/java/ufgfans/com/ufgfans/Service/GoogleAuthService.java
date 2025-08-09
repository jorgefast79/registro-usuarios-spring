package ufgfans.com.ufgfans.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class GoogleAuthService {

    @Autowired
    private EmailService emailService;

    public GoogleAuthData generarClaveYQR(String email) {
        // Genera la clave secreta
        String secret = new GoogleAuthenticator().createCredentials().getKey();

        // Genera la URL para la app Google Authenticator
        String otpAuthURL = getOtpAuthURL("TuApp", email, secret);

        // Genera el QR code como base64
        String qrCodeImage = generateQRCodeBase64(otpAuthURL, 200, 200);

        return new GoogleAuthData(secret, qrCodeImage);
    }

    private String getOtpAuthURL(String appName, String email, String secret) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", appName, email, secret, appName);
    }

    private String generateQRCodeBase64(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(pngData);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error al generar código QR", e);
        }
    }

    public void sendNewCode(String email) {
        // 1. Generar nueva clave secreta
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secretKey = key.getKey();

        // 2. Obtener el código TOTP actual
        int code = gAuth.getTotpPassword(secretKey);

        // 3. Usar el método específico para OTP
        emailService.enviarOtp(email, String.valueOf(code));

        System.out.println("Nuevo código enviado a " + email);
    }

    public static class GoogleAuthData {
        private String secret;
        private String qrUrl; // Aquí guardarás el base64 con el prefijo data:image/png;base64,...

        public GoogleAuthData(String secret, String qrUrl) {
            this.secret = secret;
            this.qrUrl = qrUrl;
        }

        public String getSecret() {
            return secret;
        }

        public String getQrUrl() {
            return qrUrl;
        }
    }
}

