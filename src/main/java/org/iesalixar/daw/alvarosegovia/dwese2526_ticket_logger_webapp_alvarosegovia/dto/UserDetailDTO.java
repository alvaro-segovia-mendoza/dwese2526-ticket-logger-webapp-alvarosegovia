package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de detalle para Usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailDTO {

    private Long id;

    private String username;

    private String passwordHash;
    // Indicadores de estado de la cuenta
    private Boolean active;
    private Boolean accountNonLocked;
    private Boolean emailVerified;
    private Boolean mustChangePassword;

    // Fechas de control de la contraseña
    private LocalDateTime lastPasswordChange;
    private LocalDateTime passwordExpiresAt;

    // Número de intentos fallidos de inicio de sesión
    private Integer failedLoginAttempts;
}
