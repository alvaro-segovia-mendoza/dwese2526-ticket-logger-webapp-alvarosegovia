package org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.iesalixar.daw.alvarosegovia.dwese2526_ticket_logger_webapp_alvarosegovia.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Implementación del DAO para la entidad Role.
 * Proporciona operaciones para listar y buscar roles en la base de datos.
 */
@Repository
@Transactional
public class RoleDAOImpl implements RoleDAO {

    private static final Logger logger = LoggerFactory.getLogger(RoleDAOImpl.class);

    @PersistenceContext
    private EntityManager em; // EntityManager para interactuar con la base de datos

    /**
     * Obtiene todos los roles disponibles en la base de datos, ordenados por nombre.
     *
     * @return Lista de roles.
     */
    @Override
    public List<Role> listAllRoles() {
        logger.info("Listing all roles from the database");

        // HQL para seleccionar todos los roles y ordenarlos por nombre
        String hql = "SELECT r FROM Role r ORDER BY r.name";

        List<Role> roles = em.createQuery(hql, Role.class).getResultList();

        logger.info("Retrieved {} roles from the database", roles.size());

        return roles;
    }

    /**
     * Encuentra roles cuyo ID esté dentro del conjunto proporcionado.
     * Retorna lista vacía si el conjunto es null o está vacío.
     *
     * @param ids Conjunto de IDs de roles a buscar.
     * @return Lista de roles que coinciden con los IDs.
     */
    @Override
    public List<Role> findByIds(Set<Long> ids) {
        // Validación de parámetros
        if (ids == null || ids.isEmpty()) {
            logger.info("findByIds called with null or empty ids. Returning empty list");
            return List.of(); // Retorna lista vacía
        }

        logger.info("Finding roles by ids: {}", ids);

        // HQL para buscar roles por IDs usando parámetro named
        String hql = "SELECT r FROM Role r WHERE r.id IN :ids";

        List<Role> roles = em.createQuery(hql, Role.class)
                .setParameter("ids", ids)
                .getResultList();

        logger.info("Found {} roles matching the given ids.", roles.size());

        return roles;
    }
}
