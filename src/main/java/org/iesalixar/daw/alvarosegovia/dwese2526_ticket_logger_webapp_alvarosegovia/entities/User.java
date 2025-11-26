package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * La clase `User` representa una entidad que modela un user dentro de la base de datos.
 * Contiene variso campos: donde `id` es el identificador único de la región,
 * `code` es un código asociado a la región, y `name` es el nombre de la región.
 *
 * Las anotaciones de Lombok ayudan a reducir el código repetitivo al generar automáticamente
 * métodos comunes como getters, setters, constructores, y otros métodos estándar de los objetos.
 */
@Data
// Genera automáticamente los métodos:
// - Getters y setters para todos los campos.
// - equals(), hashCode(), toString() y canEqual().
// Esto mejora la legibilidad y evita código repetitivo.

@NoArgsConstructor
// Genera un constructor vacío (sin parámetros).
// Es necesario para frameworks como Hibernate o JPA,
// que requieren un constructor por defecto para instanciar objetos.

@AllArgsConstructor
// Genera un constructor que acepta todos los campos definidos en la clase.
// Ideal para crear instancias completamente inicializadas de la entidad.
@Entity // Marca esta clase como entidad JPA
@Table(name = "users") // Define el nombre de la tabla en la base de datos
public class User {

    // Identificador único del usuario (clave primaria en la base de datos)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de usuario utilizado para iniciar sesión
    @Column(name = "username", nullable = false, length = 50, unique = true)
    @NotEmpty(message = "{msg.user.username.notEmpty}")
    @Size(min = 3, max = 50, message = "{msg.user.username.size}")
    private String username;

    // Hash de la contraseña del usuario
    @Column(name = "password_hash", nullable = false)
    @NotEmpty(message = "{msg.user.passwordHash.notEmpty}")
    private String passwordHash;

    // Indica si la cuenta del usuario está activa
    @Column(name = "active", nullable = false)
    @NotNull(message = "{msg.user.active.notNull}")
    private Boolean active;

    // Indica si la cuenta está bloqueada
    @Column(name = "account_non_locked", nullable = false)
    @NotNull(message = "{msg.user.accountNonLocked.notNull}")
    private Boolean accountNonLocked;

    // Fecha y hora del último cambio de contraseña
    @Column(name = "last_password_change")
    @PastOrPresent(message = "{msg.user.lastPasswordChange.pastOrPresent}")
    private LocalDateTime lastPasswordChange;

    // Fecha y hora en la que expira la contraseña actual
    @Column(name = "password_expires_at")
    @FutureOrPresent(message = "{msg.user.passwordExpiresAt.futureOrPresent}")
    private LocalDateTime passwordExpiresAt;

    // Número de intentos fallidos de inicio de sesión
    @Column(name = "failed_login_attempts")
    @Min(value = 0, message = "{msg.user.failedLoginAttempts.min}")
    private Integer failedLoginAttempts;

    // Indica si el correo electrónico del usuario ha sido verificado
    @Column(name = "email_verified", nullable = false)
    @NotNull(message = "{msg.user.emailVerified.notNull}")
    private Boolean emailVerified;

    // Indica si el usuario debe cambiar la contraseña al iniciar sesión
    @Column(name = "must_change_password", nullable = false)
    @NotNull(message = "{msg.user.mustChangePassword.notNull}")
    private Boolean mustChangePassword;
}
