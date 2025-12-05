package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para actualizar usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {

    @NotNull(message="{msg.user.id.notNull}")
    private Long id;

    @NotBlank(message = "{msg.user.email.notEmpty}")
    @Size(min = 3, max = 50, message = "{msg.user.email.size}")
    private String email;

    @NotEmpty(message="{msg.user.passwordHash.notEmpty}")
    @Size(min=6,max=100,message="{msg.user.passwordHash.size}")
    private String passwordHash;

    @NotNull(message = "{msg.user.active.notNull}")
    private Boolean active;

    @NotNull(message = "{msg.user.accountNonLocked.notNull}")
    private Boolean accountNonLocked;

    @NotNull(message = "{msg.user.emailVerified.notNull}")
    private Boolean emailVerified;

    @NotNull(message = "{msg.user.mustChangePassword.notNull}")
    private Boolean mustChangePassword;

    private LocalDateTime lastPasswordChange; // Si se cambia la contraseña se actualiza
    private LocalDateTime passwordExpiresAt;  // Se recalcula automáticamente
    private Integer failedLoginAttempts;      // Se puede actualizar si es necesario

    @NotEmpty(message = "{msg.user.roles.notempty}")
    private Set<Long> roleIds = new HashSet<>();
}
