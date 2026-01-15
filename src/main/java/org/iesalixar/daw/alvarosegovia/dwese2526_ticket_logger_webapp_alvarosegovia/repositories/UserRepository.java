package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.repositories;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link User}.
 * <p>
 * Extiende {@link JpaRepository} para proporcionar operaciones CRUD
 * estándar y consultas basadas en convenciones de Spring Data.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Comprueba si existe un usuario con el email indicado.
     *
     * @param email email del usuario
     * @return {@code true} si existe un usuario con ese email
     */
    boolean existsByEmail(String email);

    /**
     * Recupera un usuario por su email.
     *
     * @param email email del usuario
     * @return un {@link Optional} con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Recupera usuarios paginados.
     *
     * @param pageable información de paginación y ordenación
     * @return página de usuarios
     */
    Page<User> findAll(Pageable pageable);
}
