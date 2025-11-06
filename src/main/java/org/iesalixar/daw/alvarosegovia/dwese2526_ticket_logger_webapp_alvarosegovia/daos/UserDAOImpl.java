package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de {@link UserDAO} que gestiona las operaciones CRUD
 * sobre la tabla 'users' en la base de datos usando {@link JdbcTemplate}.
 * <p>
 * Esta clase incluye métodos para:
 * <ul>
 *     <li>Listar todos los usuarios</li>
 *     <li>Insertar un nuevo usuario</li>
 *     <li>Actualizar un usuario existente</li>
 *     <li>Eliminar un usuario por su ID</li>
 *     <li>Recuperar un usuario por su ID</li>
 *     <li>Verificar existencia de un usuario por nombre de usuario</li>
 * </ul>
 * <p>
 * Además, se encarga de gestionar automáticamente la expiración de la contraseña
 * calculando {@code passwordExpiresAt} a partir de {@code lastPasswordChange}.
 * <p>
 * Se utiliza {@link org.slf4j.Logger} para registrar operaciones importantes.
 */
@Repository
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);
    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor que inyecta {@link JdbcTemplate} para operaciones de base de datos.
     *
     * @param jdbcTemplate objeto {@link JdbcTemplate} para acceder a la base de datos
     */
    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lista todos los usuarios de la base de datos.
     *
     * @return Lista de usuarios
     */
    @Override
    public List<User> listAllUsers() {
        logger.info("Listing all users from the database.");
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
        logger.info("Retrieved {} users from the database.", users.size());
        return users;
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     * <p>
     * Si {@code lastPasswordChange} no está definido, se asigna la fecha/hora actual.
     * También calcula automáticamente {@code passwordExpiresAt} a partir de {@code lastPasswordChange}.
     *
     * @param user usuario a insertar
     */
    @Override
    public void insertUser(User user) {
        logger.info("Inserting user with username: {}", user.getUsername());

        // Si no tiene fecha de último cambio, la establecemos a ahora
        if (user.getLastPasswordChange() == null) {
            user.setLastPasswordChange(LocalDateTime.now());
            logger.debug("No lastPasswordChange provided; set to current date/time.");
        }

        // Calculamos passwordExpiresAt = lastPasswordChange + 3 meses
        user.setPasswordExpiresAt(user.getLastPasswordChange().plusMonths(3));
        logger.debug("Calculated passwordExpiresAt = {}", user.getPasswordExpiresAt());

        String sql = """
            INSERT INTO users (username, passwordHash, active, accountNonLocked,
                               lastPasswordChange, passwordExpiresAt, failedLoginAttempts,
                               emailVerified, mustChangePassword)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                user.isAccountNonLocked(),
                user.getLastPasswordChange(),
                user.getPasswordExpiresAt(),
                user.getFailedLoginAttempts(),
                user.isEmailVerified(),
                user.isMustChangePassword()
        );
        logger.info("Inserted user. Rows affected: {}", rowsAffected);
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     * <p>
     * Recalcula {@code passwordExpiresAt} si {@code lastPasswordChange} está presente.
     *
     * @param user usuario a actualizar
     */
    @Override
    public void updateUser(User user) {
        logger.info("Updating user with id: {}", user.getId());

        // Si el usuario tiene una fecha de cambio, recalculamos passwordExpiresAt
        if (user.getLastPasswordChange() != null) {
            user.setPasswordExpiresAt(user.getLastPasswordChange().plusMonths(3));
            logger.debug("Recalculated passwordExpiresAt = {}", user.getPasswordExpiresAt());
        }

        String sql = """
            UPDATE users
            SET username = ?, passwordHash = ?, active = ?, accountNonLocked = ?,
                lastPasswordChange = ?, passwordExpiresAt = ?, failedLoginAttempts = ?,
                emailVerified = ?, mustChangePassword = ?
            WHERE id = ?
            """;
        int rowsAffected = jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                user.isAccountNonLocked(),
                user.getLastPasswordChange(),
                user.getPasswordExpiresAt(),
                user.getFailedLoginAttempts(),
                user.isEmailVerified(),
                user.isMustChangePassword(),
                user.getId()
        );
        logger.info("Updated user. Rows affected: {}", rowsAffected);
    }

    /**
     * Elimina un usuario de la base de datos por su ID.
     *
     * @param id ID del usuario a eliminar
     */
    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleted user. Rows affected: {}", rowsAffected);
    }

    /**
     * Recupera un usuario por su ID.
     *
     * @param id ID del usuario a recuperar
     * @return usuario encontrado o {@code null} si no existe
     */
    @Override
    public User getUserById(Long id) {
        logger.info("Retrieving user by id: {}", id);
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
            logger.info("User retrieved: {}", user.getUsername());
            return user;
        } catch (Exception e) {
            logger.warn("No user found with id: {}", id);
            return null;
        }
    }

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado.
     *
     * @param username nombre de usuario a verificar
     * @return {@code true} si el usuario existe, {@code false} en caso contrario
     */
    @Override
    public boolean existsUserByUsername(String username) {
        logger.info("Checking if user with username '{}' exists", username);
        String sql = "SELECT COUNT(*) FROM users WHERE UPPER(username) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.toUpperCase());
        boolean exists = count != null && count > 0;
        logger.info("User with username '{}' exists: {}", username, exists);
        return exists;
    }

    /**
     * Verifica si existe un usuario con el nombre de usuario especificado,
     * excluyendo un usuario con un ID concreto.
     *
     * @param username nombre de usuario a verificar
     * @param id ID del usuario a excluir de la verificación
     * @return {@code true} si existe un usuario con el nombre de usuario (y no es el usuario con el ID dado), {@code false} de lo contrario
     */
    @Override
    public boolean existsUserByUsernameAndNotId(String username, Long id) {
        logger.info("Checking if user with username '{}' exists excluding id: {}", username, id);
        String sql = "SELECT COUNT(*) FROM users WHERE UPPER(username) = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.toUpperCase(), id);
        boolean exists = count != null && count > 0;
        logger.info("User with username '{}' exists excluding id {}: {}", username, id, exists);
        return exists;
    }
}
