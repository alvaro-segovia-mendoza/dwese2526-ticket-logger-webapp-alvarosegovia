package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO para crear usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {

    private Long id; // Siempre null en creación

    @NotBlank(message="{msg.user.email.notEmpty}")
    @Size(min=3,max=50,message="{msg.user.email.size}")
    private String email;

    @NotEmpty(message="{msg.user.passwordHash.notEmpty}")
    @Size(min=6,max=100,message="{msg.user.passwordHash.size}")
    private String passwordHash;

    @NotNull(message="{msg.user.active.notNull}")
    private Boolean active;

    @NotNull(message="{msg.user.accountNonLocked.notNull}")
    private Boolean accountNonLocked;

    @NotNull(message="{msg.user.emailVerified.notNull}")
    private Boolean emailVerified;

    @NotNull(message="{msg.user.mustChangePassword.notNull}")
    private Boolean mustChangePassword;

    @PastOrPresent(message="{msg.user.lastPasswordChange.pastOrPresent}")
    private LocalDateTime lastPasswordChange;

    @FutureOrPresent(message="{msg.user.passwordExpiresAt.futureOrPresent}")
    private LocalDateTime passwordExpiresAt;

    @Min(value=0,message="{msg.user.failedLoginAttempts.min}")
    private Integer failedLoginAttempts;

    @NotEmpty(message = "{msg.user.roles.notempty}")
    private Set<Long> roleIds = new HashSet<>();

}
