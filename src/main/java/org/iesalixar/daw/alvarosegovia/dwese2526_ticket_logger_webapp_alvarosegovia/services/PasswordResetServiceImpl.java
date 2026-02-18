package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.services;


import jakarta.transaction.Transactional;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.PasswordResetToken;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.PasswordResetTokenRepository;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;



/**
 * Servicio de recuperación de contraseña basado en tokens de un solo uso.
 * <p>
 * Genera y persiste tokens hasheados (no en claro), envía el enlace por email y valida/caduca
 * el token para actualizar la contraseña de forma segura.
 * </p>
 */
@Service
public class PasswordResetServiceImpl implements PasswordResetService {


    /** Tamaño del token aleatorio en bytes (32 bytes = 256 bits). */
    private static final int TOKEN_BYTES = 32;


    /** Tiempo de vida del token (minutos) antes de caducar. */
    private static final int TOKEN_TTL_MINUTES = 45;


    /** Política de caducidad de contraseña (días) tras un reset. */
    private static final int PASSWORD_EXPIRY_DAYS = 90;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordResetTokenRepository tokenRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private MailService mailService;


    @Autowired
    private AppUrlService appUrlService;


    /**
     * Solicita un restablecimiento de contraseña para un email.
     * <p>
     * No revela si el email existe (anti-enumeración). Si existe:
     * invalida tokens previos, crea uno nuevo y envía enlace por correo.
     * </p>
     *
     * @param email     email del usuario
     * @param requestIp IP del solicitante (auditoría)
     * @param userAgent User-Agent del solicitante (auditoría)
     */
    @Transactional
    @Override
    public void requestPasswordReset(String email, String requestIp, String userAgent) {


        Locale locale = LocaleContextHolder.getLocale();
        LocalDateTime now = LocalDateTime.now();


        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);


        // Respuesta “igual”: si no existe, no hacemos nada visible.
        if (user != null) {


            // Invalida tokens anteriores para que solo haya uno válido.
            tokenRepository.invalidateAllActiveTokensForUser(user.getId(), now);


            // Genera token aleatorio y guarda solo su hash.
            String rawToken = generateSecureToken();
            String tokenHash = sha256Hex(rawToken);


            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setTokenHash(tokenHash);
            prt.setCreatedAt(now);
            prt.setExpiresAt(now.plusMinutes(TOKEN_TTL_MINUTES));
            prt.setRequestIp(requestIp);
            prt.setUserAgent(safeTruncate(userAgent, 255));
            tokenRepository.save(prt);


            // Construye URL pública y envía mail vía plantilla (genérico).
            String resetUrl = appUrlService.buildResetUrl(rawToken);


            Map<String, Object> vars = Map.of(
                    "resetUrl", resetUrl,
                    "ttlMinutes", TOKEN_TTL_MINUTES
            );


            // subjectKey i18n + template Thymeleaf (ejemplo)
            mailService.sendTemplate(
                    user.getEmail(),
                    "mail.passwordreset.subject",
                    "mail/password-reset", // resources/templates/mail/password-reset.html
                    vars,
                    locale
            );
        }


    }


    /**
     * Restablece la contraseña usando un token de recuperación.
     *
     * @param rawToken    token recibido desde el enlace (en claro)
     * @param newPassword nueva contraseña
     */
    @Transactional
    @Override
    public void resetPassword(String rawToken, String newPassword) {


        LocalDateTime now = LocalDateTime.now();
        String tokenHash = sha256Hex(rawToken);


        PasswordResetToken token = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));


        if (token.isUsed() || token.isExpired()) {
            throw new IllegalArgumentException("Invalid or expired token");
        }


        User user = token.getUser();


        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(now);
        user.setPasswordExpiresAt(now.plusDays(PASSWORD_EXPIRY_DAYS));
        user.setMustChangePassword(Boolean.FALSE);
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(Boolean.TRUE);


        token.setUsedAt(now);


        userRepository.save(user);
        tokenRepository.save(token);
    }


    /** Genera un token aleatorio seguro apto para URL (Base64 URL-safe sin padding). */
    private String generateSecureToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    /** Calcula el hash SHA-256 (hex) del token para almacenarlo/consultarlo en BD. */
    private String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }


    /** Recorta un texto a un máximo para evitar problemas de longitud de columna. */
    private String safeTruncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

