package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.UserProfile;
import org.springframework.stereotype.Repository;

/**
 * Implementación del DAO para la gestión de entidades {@link UserProfile}.
 * <p>
 * Esta clase utiliza JPA y el EntityManager para realizar operaciones CRUD.
 * Está anotada con {@link Repository} para ser detectada por Spring y con
 * {@link Transactional} para que todos sus métodos se ejecuten dentro de
 * una transacción por defecto.
 */
@Repository
@Transactional
public class UserProfileDAOImpl implements UserProfileDAO {

    /**
     * EntityManager inyectado automáticamente por el contenedor JPA.
     * Se usa para gestionar operaciones de persistencia sobre la entidad {@link UserProfile}.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Obtiene un perfil de usuario a partir del ID del usuario.
     * <p>
     * La transacción está marcada como de solo lectura ya que no se modifica el estado de la base de datos.
     *
     * @param userId ID del usuario al que pertenece el perfil.
     * @return el {@link UserProfile} correspondiente o {@code null} si no existe o si el ID es nulo.
     */
    @Override
    @Transactional(readOnly = true)
    public UserProfile getUserProfileByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return entityManager.find(UserProfile.class, userId);
    }

    /**
     * Guarda un nuevo perfil de usuario o actualiza uno existente.
     * <p>
     * Si el perfil no existe en la base de datos, se crea con {@code persist}.
     * Si ya existe, se actualiza mediante {@code merge}.
     *
     * @param userProfile objeto {@link UserProfile} a guardar o actualizar.
     */
    @Override
    public void saveOrUpdateUserProfile(UserProfile userProfile) {
        if (userProfile == null) {
            return;
        }

        if (userProfile.getId() == null || !existsUserProfileByUserId(userProfile.getId())) {
            entityManager.persist(userProfile);
        } else {
            entityManager.merge(userProfile);
        }
    }

    /**
     * Comprueba si existe un perfil de usuario en la base de datos a partir de su ID.
     * <p>
     * La transacción es de solo lectura para evitar operaciones de escritura accidentales.
     *
     * @param userId ID del usuario cuyo perfil se quiere verificar.
     * @return {@code true} si existe un perfil con dicho ID; {@code false} en caso contrario.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsUserProfileByUserId(Long userId) {
        if (userId == null) {
            return false;
        }

        String jpql = "SELECT COUNT(up) FROM UserProfile up WHERE up.id = :userId";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("userId", userId);

        Long count = query.getSingleResult();

        return count != null && count > 0;
    }

}
