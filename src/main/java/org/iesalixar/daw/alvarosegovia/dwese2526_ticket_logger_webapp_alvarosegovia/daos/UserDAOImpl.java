package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Region;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación de {@link UserDAO}.
 * Gestiona las operaciones CRUD sobre la tabla 'users'.
 */
@Repository // Anotación que marca esta clase como un componente que gestiona la persistencia.
@Transactional
// Indica que todos los métodos de esta clase deben ejecutarse dentro de una transacción.
// Si algún método lanza una excepción en tiempo de ejecución, la transacción se revierte automáticamente (rollback).
// Esto asegura la integridad de los datos: todas las operaciones dentro del método se confirman o se cancelan juntas.

public class UserDAOImpl implements UserDAO {

    // Logger para registrar eventos importantes en el DAO
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lista todos los usuarios de la base de datos.
     *
     * @return Lista de usuarios
     */
    @Override
    public List<User> listAllUsers() {
        logger.info("Listing all users from the database.");
        String hql = "SELECT u FROM User u";
        List<User> users = entityManager.createQuery(hql, User.class).getResultList();
        logger.info("Retrieved {} users from the database.", users.size());
        return users;
    }

    /**
     * Lista una página de usuarios ordenados por cualquier campo de la entidad User.
     *
     * @param page      página actual (0-based)
     * @param size      número de elementos por página
     * @param sortField campo por el que se ordenan los resultados
     * @param sortDir   dirección de ordenación ("asc" o "desc")
     * @return lista de usuarios para esta página
     */
    @Override
    public List<User> listUsersPage(int page, int size, String sortField, String sortDir) {
        logger.info("Listing users page={}, size={} from the database. Sort by {} {}", page, size, sortField, sortDir);

        int offset = page * size;

        // 1. Creación de Criteria
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> cq = cb.createQuery(User.class);
        Root<User> root = cq.from(User.class);

        // 2. Determina el campo de ordenación permitido (whitelist)
        Path<?> sortPath;
        switch (sortField) {
            case "id" -> sortPath = root.get("id");
            case "email" -> sortPath = root.get("email");
            case "active" -> sortPath = root.get("active");
            case "accountNonLocked" -> sortPath = root.get("accountNonLocked");
            case "lastPasswordChange" -> sortPath = root.get("lastPasswordChange");
            case "passwordExpiresAt" -> sortPath = root.get("passwordExpiresAt");
            case "failedLoginAttempts" -> sortPath = root.get("failedLoginAttempts");
            case "emailVerified" -> sortPath = root.get("emailVerified");
            case "mustChangePassword" -> sortPath = root.get("mustChangePassword");
            default -> {
                logger.warn("Unknown sortField '{}', defaulting to 'email'.", sortField);
                sortPath = root.get("email");
            }
        }

        // 3. Dirección de ordenación
        boolean descending = "desc".equalsIgnoreCase(sortDir);
        Order order = descending ? cb.desc(sortPath) : cb.asc(sortPath);

        // 4. Aplicar ordenación a la query
        cq.select(root).orderBy(order);

        // 5. Crear TypedQuery, aplicar paginación y ejecutar
        return entityManager.createQuery(cq)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }


    /**
     * Devuelve el número total de usuarios.
     *
     * @return total de usuarios
     */
    @Override
    public long countUsers() {
        String hql = "SELECT COUNT(u) FROM User u";
        Long total = entityManager.createQuery(hql, Long.class).getSingleResult();
        return (total != null) ? total : 0L;
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
        logger.info("Inserting user with email: {}", user.getEmail());

        // Si no tiene fecha de último cambio, la establecemos a ahora
        if (user.getLastPasswordChange() == null) {
            user.setLastPasswordChange(LocalDateTime.now());
            logger.debug("No lastPasswordChange provided; set to current date/time.");
        }

        // Calculamos passwordExpiresAt = lastPasswordChange + 3 meses
        user.setPasswordExpiresAt(user.getLastPasswordChange().plusMonths(3));
        logger.debug("Calculated passwordExpiresAt = {}", user.getPasswordExpiresAt());

        // Persistimos el usuario usando JPA/Hibernate
        entityManager.persist(user);

        logger.info("Inserted user successfully with ID: {}", user.getId());
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

        // Actualizamos la entidad usando JPA/Hibernate
        entityManager.merge(user);

        logger.info("Updated user successfully with id: {}", user.getId());
    }

    /**
     * Elimina un usuario de la base de datos por su ID.
     *
     * @param id ID del usuario a eliminar
     */
    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);

        // Buscar el usuario en la base de datos
        User user = entityManager.find(User.class, id);
        if (user != null) {
            // Eliminar si existe
            entityManager.remove(user);
            logger.info("Deleted user with id: {}", id);
        } else {
            logger.warn("User with id: {} not found.", id);
        }
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

        User user = entityManager.find(User.class, id);
        if (user != null) {
            logger.info("User retrieved: {}", user.getEmail());
        } else {
            logger.warn("No user found with id: {}", id);
        }

        return user;
    }



    /**
     * Obtiene un usuario por su dirección de correo electrónico.
     *
     * @param email Email del usuario a recuperar
     * @return la entidad {@link User} correspondiente al email
     */
    @Override
    public User getUserByEmail(String email) {
        if (email == null) return null;

        String jpql = "SELECT u FROM User u WHERE u.email =:email";
        return entityManager.createQuery(jpql, User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    
    /**
     * Verifica si existe un usuario con el nombre de usuario especificado,
     * excluyendo un usuario con un ID concreto.
     *
     * @param email nombre de usuario a verificar
     * @param id ID del usuario a excluir de la verificación
     * @return {@code true} si existe un usuario con el nombre de usuario (y no es el usuario con el ID dado), {@code false} de lo contrario
     */
    @Override
    public boolean existsUserByEmailAndNotId(String email, Long id) {
        logger.info("Checking if user with email '{}' exists excluding id: {}", email, id);

        String hql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.email) = :email AND u.id != :id";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("email", email.toUpperCase())
                .setParameter("id", id)
                .getSingleResult();

        boolean exists = count != null && count > 0;
        logger.info("User with email '{}' exists excluding id {}: {}", email, id, exists);
        return exists;
    }

    /**
     * Verifica si existe un usuario con el email indicando (ignorando mayúsculas/minúsculas).
     * @param email nombre de usuario a comprobar.
     * @param true si existe, false en caso contrario.
     */
    @Override
    public boolean existsUserByEmail(String email) {
        logger.info("Checking if user with email: {} exists", email);
        String hql = "SELECT COUNT(u) FROM User u WHERE UPPER(u.email) =: email";
        Long count = entityManager.createQuery(hql, Long.class)
                .setParameter("email", email.toUpperCase())
                .getSingleResult();
        boolean exists = count != null && count > 0;
        logger.info("User with email: {} exists: {}", email, exists);
        return exists;
    }
}
