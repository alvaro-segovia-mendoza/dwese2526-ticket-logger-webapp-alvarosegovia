package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa un rol de usuario en el sistema.
 * Un rol define permisos y/o nivel de acceso para los usuarios asociados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "users") // Excluye la relación para evitar recursión
@ToString(exclude = "users") // Excluye la relación para evitar recursión
@Entity
@Table(name = "roles")
public class Role {

    /** Identificador único del rol */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre técnico del rol (ej. ADMIN, USER) */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /** Nombre visible del rol para interfaces y reportes */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** Descripción breve del rol y sus responsabilidades */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Conjunto de usuarios asociados a este rol.
     * Relación Many-to-Many inversa, cargada de forma perezosa.
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    /**
     * Constructor simplificado para crear un rol sin ID.
     *
     * @param name Nombre técnico del rol (ej. ADMIN)
     * @param displayName Nombre visible del rol
     * @param description Descripción del rol
     */
    public Role(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }
}
