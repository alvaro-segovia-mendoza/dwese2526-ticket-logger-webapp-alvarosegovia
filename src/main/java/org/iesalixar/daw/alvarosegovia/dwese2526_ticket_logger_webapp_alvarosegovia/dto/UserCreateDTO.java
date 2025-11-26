package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para crear usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {

    private Long id; // Siempre null en creación

    @NotBlank(message = "{msg.user.username.notEmpty}")
    @Size(min = 3, max = 50, message = "{msg.user.username.size}")
    private String username;

    @NotBlank(message = "{msg.user.password.notEmpty}")
    @Size(min = 6, max = 100, message = "{msg.user.password.size}")
    private String passwordHash;

    @NotNull(message = "{msg.user.active.notNull}")
    private Boolean active;

    @NotNull(message = "{msg.user.accountNonLocked.notNull}")
    private Boolean accountNonLocked;

    @NotNull(message = "{msg.user.emailVerified.notNull}")
    private Boolean emailVerified;

    @NotNull(message = "{msg.user.mustChangePassword.notNull}")
    private Boolean mustChangePassword;

    private LocalDateTime lastPasswordChange; // Opcional, si no se asigna se pone ahora
    private LocalDateTime passwordExpiresAt; // Se calcula automáticamente
    private Integer failedLoginAttempts; // Inicialmente 0
}
