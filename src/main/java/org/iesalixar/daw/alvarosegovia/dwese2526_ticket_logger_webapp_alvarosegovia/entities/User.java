package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities;

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
public class User {

    // Identificador único del usuario (clave primaria en la base de datos).
    private Long id;

    // Nombre de usuario utilizado para iniciar sesión.
    // Debe ser único en el sistema.
    private String username;

    // Hash de la contraseña del usuario.
    // Por razones de seguridad, nunca se almacena la contraseña en texto plano.
    private String passwordHash;

    // Indica si la cuenta del usuario está activa.
    // Si es false, el usuario no puede autenticarse.
    private boolean active;

    // Indica si la cuenta está bloqueada.
    // Si es false, el usuario debe esperar a que un administrador desbloquee su cuenta.
    private boolean accountNonLocked;

    // Fecha y hora del último cambio de contraseña.
    private LocalDateTime lastPasswordChange;

    // Fecha y hora en la que expira la contraseña actual.
    // Normalmente se calcula sumando un periodo (por ejemplo, 3 meses) a la fecha del último cambio.
    private LocalDateTime passwordExpiresAt;

    // Número de intentos fallidos de inicio de sesión.
    // Se utiliza para aplicar políticas de bloqueo de cuenta.
    private Integer failedLoginAttempts;

    // Indica si el correo electrónico del usuario ha sido verificado.
    private boolean emailVerified;

    // Indica si el usuario debe cambiar la contraseña al iniciar sesión.
    private boolean mustChangePassword;
}
