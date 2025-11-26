package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad User.
 * Se utiliza para transferir información de usuario entre capas,
 * sin exponer directamente la entidad ni datos sensibles innecesarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

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

